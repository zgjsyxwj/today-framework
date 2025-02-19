/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2022 All Rights Reserved.
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

package cn.taketoday.web.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cn.taketoday.beans.factory.BeanFactoryUtils;
import cn.taketoday.beans.factory.annotation.DisableAllDependencyInjection;
import cn.taketoday.beans.factory.annotation.Qualifier;
import cn.taketoday.beans.factory.support.BeanDefinition;
import cn.taketoday.context.ApplicationContext;
import cn.taketoday.context.annotation.Lazy;
import cn.taketoday.context.annotation.Props;
import cn.taketoday.context.annotation.Role;
import cn.taketoday.context.aware.ApplicationContextAware;
import cn.taketoday.context.condition.ConditionalOnMissingBean;
import cn.taketoday.core.Ordered;
import cn.taketoday.http.CorsConfiguration;
import cn.taketoday.http.MediaType;
import cn.taketoday.http.converter.AllEncompassingFormHttpMessageConverter;
import cn.taketoday.http.converter.ByteArrayHttpMessageConverter;
import cn.taketoday.http.converter.HttpMessageConverter;
import cn.taketoday.http.converter.ResourceHttpMessageConverter;
import cn.taketoday.http.converter.ResourceRegionHttpMessageConverter;
import cn.taketoday.http.converter.StringHttpMessageConverter;
import cn.taketoday.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import cn.taketoday.http.converter.feed.AtomFeedHttpMessageConverter;
import cn.taketoday.http.converter.feed.RssChannelHttpMessageConverter;
import cn.taketoday.http.converter.json.GsonHttpMessageConverter;
import cn.taketoday.http.converter.json.Jackson2ObjectMapperBuilder;
import cn.taketoday.http.converter.json.JsonbHttpMessageConverter;
import cn.taketoday.http.converter.json.MappingJackson2HttpMessageConverter;
import cn.taketoday.http.converter.smile.MappingJackson2SmileHttpMessageConverter;
import cn.taketoday.lang.Assert;
import cn.taketoday.lang.Component;
import cn.taketoday.lang.Nullable;
import cn.taketoday.logging.Logger;
import cn.taketoday.logging.LoggerFactory;
import cn.taketoday.util.ClassUtils;
import cn.taketoday.web.ReturnValueHandler;
import cn.taketoday.web.ServletDetector;
import cn.taketoday.web.WebApplicationContext;
import cn.taketoday.web.accept.ContentNegotiationManager;
import cn.taketoday.web.handler.FunctionRequestAdapter;
import cn.taketoday.web.handler.HandlerExceptionHandler;
import cn.taketoday.web.handler.NotFoundRequestAdapter;
import cn.taketoday.web.handler.ReturnValueHandlerManager;
import cn.taketoday.web.handler.method.ControllerAdviceBean;
import cn.taketoday.web.handler.method.DefaultExceptionHandler;
import cn.taketoday.web.handler.method.JsonViewRequestBodyAdvice;
import cn.taketoday.web.handler.method.JsonViewResponseBodyAdvice;
import cn.taketoday.web.handler.method.RequestBodyAdvice;
import cn.taketoday.web.handler.method.ResponseBodyAdvice;
import cn.taketoday.web.multipart.MultipartConfiguration;
import cn.taketoday.web.registry.AbstractHandlerRegistry;
import cn.taketoday.web.registry.FunctionHandlerRegistry;
import cn.taketoday.web.registry.HandlerRegistry;
import cn.taketoday.web.registry.annotation.RequestPathMappingHandlerRegistry;
import cn.taketoday.web.resolver.ParameterResolvingRegistry;
import cn.taketoday.web.resolver.ParameterResolvingStrategy;
import cn.taketoday.web.resource.ResourceUrlProvider;
import cn.taketoday.web.servlet.ServletViewResolverComposite;
import cn.taketoday.web.servlet.WebServletApplicationContext;
import cn.taketoday.web.servlet.view.InternalResourceViewResolver;
import cn.taketoday.web.view.RedirectModelManager;
import cn.taketoday.web.view.ViewResolver;
import cn.taketoday.web.view.ViewResolverComposite;
import cn.taketoday.web.view.ViewReturnValueHandler;
import cn.taketoday.web.view.template.DefaultTemplateViewResolver;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/1/27 23:43
 */
@DisableAllDependencyInjection
public class WebMvcConfigurationSupport implements ApplicationContextAware {
  protected final Logger log = LoggerFactory.getLogger(getClass());

  private static final boolean gsonPresent = isPresent("com.google.gson.Gson");
  private static final boolean jsonbPresent = isPresent("jakarta.json.bind.Jsonb");
  private static final boolean romePresent = isPresent("com.rometools.rome.feed.WireFeed");
  private static final boolean jackson2Present = isPresent("com.fasterxml.jackson.databind.ObjectMapper")
          && isPresent("com.fasterxml.jackson.core.JsonGenerator");
  private static final boolean jackson2SmilePresent = isPresent("com.fasterxml.jackson.dataformat.smile.SmileFactory");
  private static final boolean jackson2CborPresent = isPresent("com.fasterxml.jackson.dataformat.cbor.CBORFactory");

  private final List<Object> requestResponseBodyAdvice = new ArrayList<>();

  @Nullable
  private ContentNegotiationManager contentNegotiationManager;

  @Nullable
  private PathMatchConfigurer pathMatchConfigurer;

  @Nullable
  private List<HttpMessageConverter<?>> messageConverters;

  @Nullable
  private ApplicationContext applicationContext;

  @Nullable
  private Map<String, CorsConfiguration> corsConfigurations;

  @Nullable
  private List<Object> interceptors;

  @Nullable
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public void setApplicationContext(@Nullable ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    initControllerAdviceCache();
  }

  private void initControllerAdviceCache() {
    if (getApplicationContext() == null) {
      return;
    }

    List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(getApplicationContext());
    ArrayList<Object> requestResponseBodyAdviceBeans = new ArrayList<>();
    for (ControllerAdviceBean adviceBean : adviceBeans) {
      Class<?> beanType = adviceBean.getBeanType();
      if (beanType == null) {
        throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
      }
      if (RequestBodyAdvice.class.isAssignableFrom(beanType) || ResponseBodyAdvice.class.isAssignableFrom(beanType)) {
        requestResponseBodyAdviceBeans.add(adviceBean);
      }
    }

    if (!requestResponseBodyAdviceBeans.isEmpty()) {
      this.requestResponseBodyAdvice.addAll(0, requestResponseBodyAdviceBeans);
    }

    if (jackson2Present) {
      requestResponseBodyAdvice.add(new JsonViewRequestBodyAdvice());
      requestResponseBodyAdvice.add(new JsonViewResponseBodyAdvice());
    }

  }

  //---------------------------------------------------------------------
  // HttpMessageConverter
  //---------------------------------------------------------------------

  /**
   * Provides access to the shared {@link HttpMessageConverter HttpMessageConverters}
   * used by the {@link cn.taketoday.web.resolver.ParameterResolvingStrategy} and the
   * {@link ReturnValueHandlerManager}.
   * <p>This method cannot be overridden; use {@link #configureMessageConverters} instead.
   * Also see {@link #addDefaultHttpMessageConverters} for adding default message converters.
   */
  public final List<HttpMessageConverter<?>> getMessageConverters() {
    if (this.messageConverters == null) {
      this.messageConverters = new ArrayList<>();
      configureMessageConverters(this.messageConverters);
      if (this.messageConverters.isEmpty()) {
        addDefaultHttpMessageConverters(this.messageConverters);
      }
      extendMessageConverters(this.messageConverters);
    }
    return this.messageConverters;
  }

  /**
   * Override this method to add custom {@link HttpMessageConverter HttpMessageConverters}
   * to use with the {@link cn.taketoday.web.resolver.ParameterResolvingStrategy} and the
   * {@link ReturnValueHandlerManager}.
   * <p>Adding converters to the list turns off the default converters that would
   * otherwise be registered by default. Also see {@link #addDefaultHttpMessageConverters}
   * for adding default message converters.
   *
   * @param converters a list to add message converters to (initially an empty list)
   * @since 4.0
   */
  protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) { }

  /**
   * Override this method to extend or modify the list of converters after it has
   * been configured. This may be useful for example to allow default converters
   * to be registered and then insert a custom converter through this method.
   *
   * @param converters the list of configured converters to extend
   * @since 4.0
   */
  protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) { }

  /**
   * Adds a set of default HttpMessageConverter instances to the given list.
   * Subclasses can call this method from {@link #configureMessageConverters}.
   *
   * @param messageConverters the list to add the default message converters to
   * @since 4.0
   */
  protected final void addDefaultHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
    messageConverters.add(new ByteArrayHttpMessageConverter());
    messageConverters.add(new StringHttpMessageConverter());
    messageConverters.add(new ResourceHttpMessageConverter());
    messageConverters.add(new ResourceRegionHttpMessageConverter());
    messageConverters.add(new AllEncompassingFormHttpMessageConverter());

    if (romePresent) {
      messageConverters.add(new AtomFeedHttpMessageConverter());
      messageConverters.add(new RssChannelHttpMessageConverter());
    }

    ApplicationContext applicationContext = getApplicationContext();

    if (jackson2Present) {
      Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
      if (applicationContext != null) {
        builder.applicationContext(applicationContext);
      }
      messageConverters.add(new MappingJackson2HttpMessageConverter(builder.build()));
    }
    else if (gsonPresent) {
      messageConverters.add(new GsonHttpMessageConverter());
    }
    else if (jsonbPresent) {
      messageConverters.add(new JsonbHttpMessageConverter());
    }

    if (jackson2SmilePresent) {
      Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.smile();
      if (applicationContext != null) {
        builder.applicationContext(applicationContext);
      }
      messageConverters.add(new MappingJackson2SmileHttpMessageConverter(builder.build()));
    }
    if (jackson2CborPresent) {
      Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.cbor();
      if (applicationContext != null) {
        builder.applicationContext(applicationContext);
      }
      messageConverters.add(new MappingJackson2CborHttpMessageConverter(builder.build()));
    }
  }

  //---------------------------------------------------------------------
  // ContentNegotiation
  //---------------------------------------------------------------------

  /**
   * Return a {@link ContentNegotiationManager} instance to use to determine
   * requested {@linkplain MediaType media types} in a given request.
   */
  @Component
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public ContentNegotiationManager contentNegotiationManager() {
    if (this.contentNegotiationManager == null) {
      ContentNegotiationConfigurer configurer = new ContentNegotiationConfigurer();
      configurer.mediaTypes(getDefaultMediaTypes());
      configureContentNegotiation(configurer);
      this.contentNegotiationManager = configurer.buildContentNegotiationManager();
    }
    return this.contentNegotiationManager;
  }

  protected Map<String, MediaType> getDefaultMediaTypes() {
    Map<String, MediaType> map = new HashMap<>(4);
    if (romePresent) {
      map.put("atom", MediaType.APPLICATION_ATOM_XML);
      map.put("rss", MediaType.APPLICATION_RSS_XML);
    }
    if (jackson2Present || gsonPresent || jsonbPresent) {
      map.put("json", MediaType.APPLICATION_JSON);
    }
    if (jackson2SmilePresent) {
      map.put("smile", MediaType.valueOf("application/x-jackson-smile"));
    }
    if (jackson2CborPresent) {
      map.put("cbor", MediaType.APPLICATION_CBOR);
    }
    return map;
  }

  /**
   * Override this method to configure content negotiation.
   */
  protected void configureContentNegotiation(ContentNegotiationConfigurer configurer) { }

  //---------------------------------------------------------------------
  // PathMatchConfigurer
  //---------------------------------------------------------------------

  /**
   * Callback for building the {@link PathMatchConfigurer}.
   * Delegates to {@link #configurePathMatch}.
   */
  protected PathMatchConfigurer getPathMatchConfigurer() {
    if (this.pathMatchConfigurer == null) {
      this.pathMatchConfigurer = new PathMatchConfigurer();
      configurePathMatch(this.pathMatchConfigurer);
    }
    return this.pathMatchConfigurer;
  }

  /**
   * Override this method to configure path matching options.
   *
   * @see PathMatchConfigurer
   */
  protected void configurePathMatch(PathMatchConfigurer configurer) { }

  /**
   * Register a {@link ViewResolverComposite} that contains a chain of view resolvers
   * to use for view resolution.
   * By default this resolver is ordered at 0 unless content negotiation view
   * resolution is used in which case the order is raised to
   * {@link cn.taketoday.core.Ordered#HIGHEST_PRECEDENCE Ordered.HIGHEST_PRECEDENCE}.
   * <p>If no other resolvers are configured,
   * {@link ViewResolverComposite#resolveViewName(String, Locale)} returns null in order
   * to allow other potential {@link ViewResolver} beans to resolve views.
   */
  @Component
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public ViewResolver webViewResolver(ContentNegotiationManager contentNegotiationManager) {
    ViewResolverRegistry registry =
            new ViewResolverRegistry(contentNegotiationManager, applicationContext);
    configureViewResolvers(registry);

    List<ViewResolver> viewResolvers = registry.getViewResolvers();
    if (viewResolvers.isEmpty() && applicationContext != null) {
      Set<String> names = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
              applicationContext, ViewResolver.class, true, false);
      if (names.size() == 1) {
        if (ServletDetector.isPresent()) {
          viewResolvers.add(new InternalResourceViewResolver());
        }
        else {
          // add default
          DefaultTemplateViewResolver viewResolver = new DefaultTemplateViewResolver();
          viewResolver.setResourceLoader(applicationContext);
          viewResolvers.add(viewResolver);
        }
      }
    }

    ViewResolverComposite composite;
    if (ServletDetector.isPresent()) {
      composite = new ServletViewResolverComposite();
    }
    else {
      composite = new ViewResolverComposite();
    }

    composite.setOrder(registry.getOrder());
    composite.setViewResolvers(viewResolvers);
    if (applicationContext != null) {
      composite.setApplicationContext(applicationContext);
    }

    if (ServletDetector.isPresent() && applicationContext instanceof WebServletApplicationContext servletApp) {
      ServletViewResolverComposite viewResolverComposite = (ServletViewResolverComposite) composite;
      viewResolverComposite.setServletContext(servletApp.getServletContext());
    }

    return composite;
  }

  /**
   * Override this method to configure view resolution.
   *
   * @see ViewResolverRegistry
   */
  protected void configureViewResolvers(ViewResolverRegistry registry) { }

  /**
   * default {@link ReturnValueHandler} registry
   */
  @Component
  @ConditionalOnMissingBean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  ReturnValueHandlerManager returnValueHandlers(
          @Nullable RedirectModelManager redirectModelManager,
          @Qualifier("webViewResolver") ViewResolver webViewResolver) {

    ReturnValueHandlerManager handlers = new ReturnValueHandlerManager(getMessageConverters());

    handlers.setApplicationContext(applicationContext);
    handlers.setRedirectModelManager(redirectModelManager);
    handlers.setViewResolver(webViewResolver);

    ViewReturnValueHandler handler = new ViewReturnValueHandler(webViewResolver);
    handler.setModelManager(redirectModelManager);

    handlers.setViewReturnValueHandler(handler);
    handlers.addRequestResponseBodyAdvice(requestResponseBodyAdvice);
    handlers.registerDefaultHandlers();

    return handlers;
  }

  /**
   * default {@link ParameterResolvingStrategy} registry
   */
  @Component
  @ConditionalOnMissingBean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  ParameterResolvingRegistry parameterResolvingRegistry(
          WebApplicationContext context, MultipartConfiguration multipartConfig) {

    ParameterResolvingRegistry registry = new ParameterResolvingRegistry();
    registry.setApplicationContext(context);
    registry.setMultipartConfig(multipartConfig);
    registry.registerDefaultStrategies();
    registry.setMessageConverters(getMessageConverters());
    registry.addRequestResponseBodyAdvice(requestResponseBodyAdvice);

    return registry;
  }

  /**
   * default {@link MultipartConfiguration} bean
   */
  @Lazy
  @Component
  @Props(prefix = "multipart.")
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  @ConditionalOnMissingBean(MultipartConfiguration.class)
  MultipartConfiguration multipartConfiguration() {
    return new MultipartConfiguration();
  }

  /**
   * default {@link NotFoundRequestAdapter} to handle request-url not found
   */
  @Component
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  @ConditionalOnMissingBean(NotFoundRequestAdapter.class)
  NotFoundRequestAdapter notFoundRequestAdapter() {
    return new NotFoundRequestAdapter();
  }

  /**
   * default {@link HandlerExceptionHandler}
   */
  @Component
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  @ConditionalOnMissingBean(HandlerExceptionHandler.class)
  DefaultExceptionHandler defaultExceptionHandler() {
    return new DefaultExceptionHandler();
  }

  // HandlerRegistry

  /**
   * core {@link cn.taketoday.web.registry.HandlerRegistry} to register handler
   */
  @Component
  @ConditionalOnMissingBean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  RequestPathMappingHandlerRegistry requestPathMappingHandlerRegistry() {
    RequestPathMappingHandlerRegistry registry = new RequestPathMappingHandlerRegistry();
    PathMatchConfigurer configurer = getPathMatchConfigurer();

    Boolean useTrailingSlashMatch = configurer.isUseTrailingSlashMatch();
    if (useTrailingSlashMatch != null) {
      registry.setUseTrailingSlashMatch(useTrailingSlashMatch);
    }

    Boolean useCaseSensitiveMatch = configurer.isUseCaseSensitiveMatch();
    if (useCaseSensitiveMatch != null) {
      registry.setUseCaseSensitiveMatch(useCaseSensitiveMatch);
    }

    return registry;
  }

  /**
   * Return a handler mapping ordered at Integer.MAX_VALUE-1 with mapped
   * resource handlers. To configure resource handling, override
   * {@link #addResourceHandlers}.
   */
  @Nullable
  @Component
  public HandlerRegistry resourceHandlerRegistry(
          @Nullable ContentNegotiationManager contentNegotiationManager, ResourceUrlProvider resourceUrlProvider) {

    Assert.state(this.applicationContext != null, "No ApplicationContext set");
    PathMatchConfigurer pathConfig = getPathMatchConfigurer();

    ResourceHandlerRegistry registry = new ResourceHandlerRegistry(this.applicationContext, contentNegotiationManager);
    addResourceHandlers(registry);

    AbstractHandlerRegistry handlerRegistry = registry.getHandlerMapping();
    if (handlerRegistry == null) {
      return null;
    }

    handlerRegistry.setCorsConfigurations(getCorsConfigurations());
    handlerRegistry.setInterceptors(getInterceptors(resourceUrlProvider));
    handlerRegistry.setUseCaseSensitiveMatch(Boolean.TRUE.equals(pathConfig.isUseCaseSensitiveMatch()));
    handlerRegistry.setUseTrailingSlashMatch(Boolean.TRUE.equals(pathConfig.isUseTrailingSlashMatch()));

    return handlerRegistry;
  }

  @Component
  public HandlerRegistry webFunctionHandlerRegistry() {
    FunctionHandlerRegistry functionHandlerRegistry = new FunctionHandlerRegistry();
    functionHandlerRegistry.setApplicationContext(getApplicationContext());
    functionHandlerRegistry.setCorsConfigurations(getCorsConfigurations());

    configureFunctionHandler(functionHandlerRegistry);
    return functionHandlerRegistry;
  }

  protected void configureFunctionHandler(FunctionHandlerRegistry functionHandlerRegistry) { }

  @Component
  FunctionRequestAdapter functionRequestAdapter() {
    return new FunctionRequestAdapter(Ordered.HIGHEST_PRECEDENCE + 1);
  }

  /**
   * Override this method to add resource handlers for serving static resources.
   *
   * @see ResourceHandlerRegistry
   */
  protected void addResourceHandlers(ResourceHandlerRegistry registry) { }

  /**
   * A {@link ResourceUrlProvider} bean for use with the MVC dispatcher.
   */
  @Component
  public ResourceUrlProvider mvcResourceUrlProvider() {
    return new ResourceUrlProvider();
  }

  /**
   * Return the registered {@link CorsConfiguration} objects,
   * keyed by path pattern.
   */
  protected final Map<String, CorsConfiguration> getCorsConfigurations() {
    if (this.corsConfigurations == null) {
      CorsRegistry registry = new CorsRegistry();
      addCorsMappings(registry);
      this.corsConfigurations = registry.getCorsConfigurations();
    }
    return this.corsConfigurations;
  }

  /**
   * Override this method to configure cross origin requests processing.
   *
   * @see CorsRegistry
   */
  protected void addCorsMappings(CorsRegistry registry) { }

  /**
   * Provide access to the shared handler interceptors used to configure
   * {@link HandlerRegistry} instances with.
   * <p>This method cannot be overridden; use {@link #addInterceptors} instead.
   */
  protected final Object[] getInterceptors(ResourceUrlProvider mvcResourceUrlProvider) {
    if (this.interceptors == null) {
      InterceptorRegistry registry = new InterceptorRegistry();
      addInterceptors(registry);
      this.interceptors = registry.getInterceptors();
    }
    return this.interceptors.toArray();
  }

  /**
   * Override this method to add Framework MVC interceptors for
   * pre- and post-processing of controller invocation.
   *
   * @see InterceptorRegistry
   */
  protected void addInterceptors(InterceptorRegistry registry) {
  }

  static boolean isPresent(String name) {
    ClassLoader classLoader = WebMvcAutoConfiguration.class.getClassLoader();
    return ClassUtils.isPresent(name, classLoader);
  }

}
