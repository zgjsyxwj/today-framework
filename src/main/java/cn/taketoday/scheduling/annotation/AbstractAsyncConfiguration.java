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

package cn.taketoday.scheduling.annotation;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import cn.taketoday.aop.support.interceptor.AsyncUncaughtExceptionHandler;
import cn.taketoday.context.aware.ImportAware;
import cn.taketoday.core.annotation.MergedAnnotation;
import cn.taketoday.core.type.AnnotationMetadata;
import cn.taketoday.lang.Autowired;
import cn.taketoday.context.annotation.Configuration;
import cn.taketoday.lang.Nullable;
import cn.taketoday.util.CollectionUtils;

/**
 * Abstract base {@code Configuration} class providing common structure for enabling
 * asynchronous method execution capability.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @see EnableAsync
 * @since 4.0
 */
@Configuration(proxyBeanMethods = false)
public abstract class AbstractAsyncConfiguration implements ImportAware {

  @Nullable
  protected MergedAnnotation<EnableAsync> enableAsync;

  @Nullable
  protected Supplier<Executor> executor;

  @Nullable
  protected Supplier<AsyncUncaughtExceptionHandler> exceptionHandler;

  @Override
  public void setImportMetadata(AnnotationMetadata importMetadata) {
    this.enableAsync = importMetadata.getAnnotations().get(EnableAsync.class);
    if (!enableAsync.isPresent()) {
      throw new IllegalArgumentException(
              "@EnableAsync is not present on importing class " + importMetadata.getClassName());
    }
  }

  /**
   * Collect any {@link AsyncConfigurer} beans through autowiring.
   */
  @Autowired(required = false)
  void setConfigurers(Collection<AsyncConfigurer> configurers) {
    if (CollectionUtils.isEmpty(configurers)) {
      return;
    }
    if (configurers.size() > 1) {
      throw new IllegalStateException("Only one AsyncConfigurer may exist");
    }
    AsyncConfigurer configurer = configurers.iterator().next();
    this.executor = configurer::getAsyncExecutor;
    this.exceptionHandler = configurer::getAsyncUncaughtExceptionHandler;
  }

}
