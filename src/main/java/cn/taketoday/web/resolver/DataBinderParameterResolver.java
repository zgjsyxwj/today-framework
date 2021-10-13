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
package cn.taketoday.web.resolver;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.taketoday.beans.factory.PropertyValue;
import cn.taketoday.beans.support.DataBinder;
import cn.taketoday.core.MultiValueMap;
import cn.taketoday.core.TypeDescriptor;
import cn.taketoday.core.annotation.AnnotationUtils;
import cn.taketoday.core.conversion.ConversionService;
import cn.taketoday.core.conversion.ConversionServiceAware;
import cn.taketoday.core.conversion.support.DefaultConversionService;
import cn.taketoday.lang.Assert;
import cn.taketoday.util.ClassUtils;
import cn.taketoday.util.CollectionUtils;
import cn.taketoday.util.ObjectUtils;
import cn.taketoday.util.ReflectionUtils;
import cn.taketoday.util.StringUtils;
import cn.taketoday.web.RequestContext;
import cn.taketoday.web.WebUtils;
import cn.taketoday.web.annotation.RequestBody;
import cn.taketoday.web.annotation.RequestParam;
import cn.taketoday.web.handler.MethodParameter;
import cn.taketoday.web.multipart.MultipartFile;

/**
 * Resolve Bean
 *
 * <p>
 * supports annotated-property-resolvers if set the ParameterResolvers,
 * this feature is that request-params Bean property is annotated with meta-annotation RequestParam
 * just like this:
 * <pre>
 *
 * &#64Data
 * static class RequestParams {
 *   &#64RequestHeader("X-Header")
 *   private String header;
 *
 *   &#64RequestHeader("Accept-Encoding")
 *   private String acceptEncoding;
 *
 *   private String name;
 *
 *   &#64SessionAttribute("session")
 *   private String session;
 *
 *   &#64CookieValue
 *   private String cookie;
 *
 *   &#64CookieValue("Authorization")
 *   private String authorization;
 *
 * }
 *
 * &#64GET("/binder")
 * public RequestParams binder(RequestParams params, WebSession session) {
 *   session.setAttribute("session", params.toString());
 *   return params;
 * }
 *
 * // result:
 * {
 *     "header": "test",
 *     "acceptEncoding": "gzip, deflate, br",
 *     "name": "TODAY",
 *     "session": "TestController.RequestParams(header=test, acceptEncoding=gzip, deflate, br, name=TODAY, session=null, cookie=TODAY, authorization=bb79c2b9-3b18-4947-af58-257d0ff89c3d)",
 *     "cookie": "TODAY",
 *     "authorization": "2549afee-f385-4f6e-876b-e99facc8e4ce"
 * }
 * </pre>
 * </p>
 *
 * @author TODAY 2019-07-13 01:11
 */
public class DataBinderParameterResolver
        implements ParameterResolvingStrategy, ConversionServiceAware {
  public static final String ANNOTATED_RESOLVERS_KEY = AnnotatedPropertyResolver.class.getName() + "-annotated-property-resolvers";

  private ConversionService conversionService = DefaultConversionService.getSharedInstance();

  private ParameterResolvingRegistry registry;

  public DataBinderParameterResolver() { }

  public DataBinderParameterResolver(ParameterResolvingRegistry resolvers) {
    this();
    this.registry = resolvers;
  }

  public DataBinderParameterResolver(ConversionService conversionService) {
    this();
    setConversionService(conversionService);
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    if (!parameter.isAnnotationPresent(RequestBody.class) // @since 3.0.3 #17
            && !ClassUtils.isSimpleType(parameter.getParameterClass())) {
      setAttribute(parameter, registry);
      return true;
    }
    return false;
  }

  /**
   * @since 4.0
   */
  static void setAttribute(MethodParameter parameter, ParameterResolvingRegistry registry) {
    if (registry != null) {
      // supports annotated-property-resolvers
      ArrayList<AnnotatedPropertyResolver> resolverList = new ArrayList<>();
      Class<?> parameterClass = parameter.getParameterClass();

      ReflectionUtils.doWithFields(parameterClass, field -> {
        if (AnnotationUtils.isPresent(field, RequestParam.class)) {
          resolverList.add(new AnnotatedPropertyResolver(parameter, field, registry));
        }
      });
      parameter.setAttribute(ANNOTATED_RESOLVERS_KEY, resolverList);
    }
  }

  /**
   * @return Pojo parameter
   */
  @Override
  public Object resolveParameter(
          final RequestContext context, final MethodParameter parameter) throws Throwable {
    final Class<?> parameterClass = parameter.getParameterClass();
    final DataBinder dataBinder = new DataBinder(parameterClass, conversionService);

    final Map<String, String[]> parameters = context.getParameters();
    for (final Map.Entry<String, String[]> entry : parameters.entrySet()) {
      final String[] value = entry.getValue();
      if (ObjectUtils.isNotEmpty(value)) {
        if (value.length == 1) {
          dataBinder.addPropertyValue(entry.getKey(), value[0]);
        }
        else {
          dataBinder.addPropertyValue(entry.getKey(), value);
        }
      }
    }

    if (WebUtils.isMultipart(context)) {
      // Multipart
      final MultiValueMap<String, MultipartFile> multipartFiles = context.multipartFiles();
      if (CollectionUtils.isNotEmpty(multipartFiles)) {
        for (final Map.Entry<String, List<MultipartFile>> entry : multipartFiles.entrySet()) {
          final List<MultipartFile> files = entry.getValue();
          if (files.size() == 1) {
            dataBinder.addPropertyValue(entry.getKey(), files.get(0));
          }
          else {
            dataBinder.addPropertyValue(entry.getKey(), files);
          }
        }
      }
    }
    // #30 Support annotation-supported in the form of DataBinder
    resolveAnnotatedProperty(context, parameter, dataBinder);

    return dataBinder.bind();
  }

  /**
   * @since 4.0
   */
  static void resolveAnnotatedProperty(
          RequestContext context, MethodParameter parameter, DataBinder dataBinder) throws Throwable {
    Object attribute = parameter.getAttribute(ANNOTATED_RESOLVERS_KEY);
    if (attribute instanceof List) {
      @SuppressWarnings("unchecked")
      List<AnnotatedPropertyResolver> resolvers = (List<AnnotatedPropertyResolver>) attribute;
      for (final AnnotatedPropertyResolver resolver : resolvers) {
        PropertyValue propertyValue = resolver.resolve(context);
        dataBinder.addPropertyValue(propertyValue);
      }
    }
  }

  /**
   * @since 4.0
   */
  public void setRegistry(ParameterResolvingRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void setConversionService(ConversionService conversionService) {
    Assert.notNull(conversionService, "conversionService must not be null");
    this.conversionService = conversionService;
  }

  public ConversionService getConversionService() {
    return conversionService;
  }

  /**
   * @since 4.0
   */
  static final class AnnotatedPropertyResolver {

    final String propertyName;
    final ParameterResolvingStrategy resolver;
    final AnnotationBinderParameter parameter;

    /**
     * @throws IllegalStateException
     *         If there isn't a suitable resolver
     */
    AnnotatedPropertyResolver(MethodParameter other, Field field, ParameterResolvingRegistry registry) {
      this.propertyName = field.getName();// TODO BeanMetadata#getPropertyName
      this.parameter = new AnnotationBinderParameter(other, field);
      this.resolver = registry.obtainResolvingStrategy(this.parameter);
    }

    public PropertyValue resolve(RequestContext context) throws Throwable {
      Object value = resolver.resolveParameter(context, parameter);
      return new PropertyValue(propertyName, value);
    }

  }

  /**
   * @since 4.0
   */
  static final class AnnotationBinderParameter extends MethodParameter {
    private final Field field;
    private final Class<?> parameterClass;

    public AnnotationBinderParameter(MethodParameter other, Field field) {
      super(other);
      this.parameterClass = field.getType();
      this.field = field;
      initRequestParam(field);
      if (StringUtils.isEmpty(getName())) {
        setName(field.getName());
      }
    }

    @Override
    public Class<?> getParameterClass() {
      return parameterClass;
    }

    @Override
    public AnnotatedElement getAnnotationSource() {
      return field;
    }

    @Override
    protected TypeDescriptor createTypeDescriptor() {
      return TypeDescriptor.fromField(field);
    }

  }
}
