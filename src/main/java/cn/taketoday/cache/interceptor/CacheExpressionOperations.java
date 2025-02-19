/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2021 All Rights Reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.cache.interceptor;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import cn.taketoday.cache.support.DefaultCacheKey;
import cn.taketoday.cache.annotation.CacheConfig;
import cn.taketoday.cache.annotation.CacheConfiguration;
import cn.taketoday.context.expression.CachedExpressionEvaluator;
import cn.taketoday.core.annotation.AnnotationUtils;
import cn.taketoday.core.annotation.MergedAnnotation;
import cn.taketoday.core.annotation.MergedAnnotations;
import cn.taketoday.expression.ValueExpression;
import cn.taketoday.lang.Constant;
import cn.taketoday.lang.Nullable;
import cn.taketoday.util.ConcurrentReferenceHashMap;
import cn.taketoday.util.StringUtils;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang 2021/11/23 13:39</a>
 * @since 4.0
 */
public class CacheExpressionOperations extends CachedExpressionEvaluator {

  static final ConcurrentReferenceHashMap<MethodKey, CacheConfiguration> CACHE_OPERATION = new ConcurrentReferenceHashMap<>(128);

  private final Map<ExpressionKey, ValueExpression> keyCache = new ConcurrentHashMap<>(64);
  private final Map<ExpressionKey, ValueExpression> unlessCache = new ConcurrentHashMap<>(64);
  private final Map<ExpressionKey, ValueExpression> conditionCache = new ConcurrentHashMap<>(64);

  static final Function<MethodKey, CacheConfiguration> CACHE_OPERATION_FUNCTION = target -> {

    Method method = target.targetMethod;
    Class<? extends Annotation> annClass = target.annotationClass;

    // Find target method [annClass] AnnotationAttributes
    Class<?> declaringClass = method.getDeclaringClass();
    MergedAnnotation<? extends Annotation> annotation = MergedAnnotations.from(method).get(annClass);
    if (!annotation.isPresent()) {
      annotation = MergedAnnotations.from(declaringClass).get(annClass);
      if (!annotation.isPresent()) {
        throw new IllegalStateException("Unexpected exception has occurred, may be it's a bug");
      }
    }

    CacheConfiguration configuration =
            AnnotationUtils.injectAttributes(annotation, annClass, new CacheConfiguration(annClass));

    CacheConfig cacheConfig = AnnotationUtils.getAnnotation(declaringClass, CacheConfig.class);
    if (cacheConfig != null) {
      configuration.mergeCacheConfigAttributes(cacheConfig);
    }
    return configuration;
  };

  /**
   * Resolve {@link Annotation} from given {@link Annotation} {@link Class}
   *
   * @return {@link Annotation} instance
   */
  public CacheConfiguration getConfig(MethodKey methodKey) {
    return CACHE_OPERATION.computeIfAbsent(methodKey, CACHE_OPERATION_FUNCTION);
  }

  /**
   * Create a key for the target method
   *
   * @param key Key expression
   * @param ctx Cache el ctx
   * @param invocation Target Method Invocation
   * @return Cache key
   */
  public Object createKey(
          @Nullable String key, CacheEvaluationContext ctx, MethodInvocation invocation) {
    if (StringUtils.isEmpty(key)) {
      return new DefaultCacheKey(invocation.getArguments());
    }
    return getExpression(keyCache, ctx.getMethodKey(), key)
            .getValue(ctx);
  }

  /**
   * Test condition Expression
   *
   * @param condition condition expression
   * @param context Cache EL Context
   * @return returns If pass the condition
   */
  public boolean passCondition(@Nullable String condition, CacheEvaluationContext context) {
    return StringUtils.isEmpty(condition) || //if its empty returns true
            getExpression(conditionCache, context.getMethodKey(), condition)
                    .getValue(context, boolean.class);
  }

  /**
   * Test unless Expression
   *
   * @param unless unless express
   * @param result method return value
   * @param context Cache el context
   */
  public boolean allowPutCache(
          @Nullable String unless, Object result, CacheEvaluationContext context) {
    if (StringUtils.isNotEmpty(unless)) {
      context.setVariable(Constant.KEY_RESULT, result);
      return !getExpression(unlessCache, context.getMethodKey(), unless).getValue(context, Boolean.class);
    }
    return true;
  }

  public CacheEvaluationContext prepareContext(MethodKey methodKey, MethodInvocation invocation) {
    CacheEvaluationContext context = new CacheEvaluationContext(
            invocation, invocation.getMethod(), invocation.getArguments(), parameterNameDiscoverer, methodKey);

    // ${root.target} for target instance ${root.method}
    context.setVariable(Constant.KEY_ROOT, invocation);
    return context;
  }

}
