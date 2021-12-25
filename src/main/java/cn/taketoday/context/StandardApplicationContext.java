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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.taketoday.context;

import java.io.IOException;
import java.util.List;

import cn.taketoday.beans.factory.support.BeanDefinition;
import cn.taketoday.beans.factory.BeanDefinitionRegistry;
import cn.taketoday.beans.factory.BeanNamePopulator;
import cn.taketoday.beans.factory.support.ConfigurableBeanFactory;
import cn.taketoday.beans.factory.support.StandardBeanFactory;
import cn.taketoday.context.annotation.AnnotationBeanNamePopulator;
import cn.taketoday.context.annotation.AnnotationConfigUtils;
import cn.taketoday.context.annotation.Configuration;
import cn.taketoday.context.annotation.ConfigurationClassPostProcessor;
import cn.taketoday.context.annotation.FullyQualifiedAnnotationBeanNamePopulator;
import cn.taketoday.context.loader.AnnotatedBeanDefinitionReader;
import cn.taketoday.context.loader.BeanDefinitionLoader;
import cn.taketoday.context.loader.DefinitionLoadingContext;
import cn.taketoday.context.loader.ScanningBeanDefinitionReader;
import cn.taketoday.core.env.ConfigurableEnvironment;
import cn.taketoday.lang.Assert;
import cn.taketoday.lang.Nullable;
import cn.taketoday.lang.TodayStrategies;
import cn.taketoday.util.StringUtils;

/**
 * Standard {@link ApplicationContext}
 *
 * like Spring's AnnotationConfigApplicationContext
 *
 * @author TODAY 2018-09-06 13:47
 */
public class StandardApplicationContext
        extends DefaultApplicationContext implements ConfigurableApplicationContext, BeanDefinitionRegistry, AnnotationConfigRegistry {

  @Nullable
  private String propertiesLocation;

  private DefinitionLoadingContext loadingContext;
  private ScanningBeanDefinitionReader scanningReader;

  /**
   * Default Constructor
   */
  public StandardApplicationContext() { }

  /**
   * Construct with {@link StandardBeanFactory}
   *
   * @param beanFactory {@link StandardBeanFactory} instance
   */
  public StandardApplicationContext(StandardBeanFactory beanFactory) {
    super(beanFactory);
  }

  /**
   * Create a new StandardApplicationContext with the given parent.
   *
   * @param parent the parent application context
   * @see #registerBeanDefinition(String, BeanDefinition)
   * @see #refresh()
   */
  public StandardApplicationContext(@Nullable ApplicationContext parent) {
    setParent(parent);
  }

  /**
   * Create a new DefaultApplicationContext with the given StandardBeanFactory.
   *
   * @param beanFactory the StandardBeanFactory instance to use for this context
   * @param parent the parent application context
   * @see #registerBeanDefinition(String, BeanDefinition)
   * @see #refresh()
   */
  public StandardApplicationContext(StandardBeanFactory beanFactory, ApplicationContext parent) {
    this(beanFactory);
    setParent(parent);
  }

  /**
   * Set given properties location
   *
   * @param propertiesLocation a file or a di rectory to scan
   */
  public StandardApplicationContext(String propertiesLocation) {
    setPropertiesLocation(propertiesLocation);
  }

  /**
   * Start with given class set
   *
   * @param components one or more component classes,
   * e.g. {@link Configuration @Configuration} classes
   * @see #refresh()
   * @see #register(Class[])
   */
  public StandardApplicationContext(Class<?>... components) {
    register(components);
    refresh();
  }

  /**
   * Start context with given properties location and base scan packages
   *
   * @param propertiesLocation a file or a directory contains
   * @param basePackages scan classes from packages
   * @see #refresh()
   */
  public StandardApplicationContext(String propertiesLocation, String... basePackages) {
    setPropertiesLocation(propertiesLocation);
    scan(basePackages);
    refresh();
  }

  public void setPropertiesLocation(@Nullable String propertiesLocation) {
    this.propertiesLocation = propertiesLocation;
  }

  @Nullable
  public String getPropertiesLocation() {
    return propertiesLocation;
  }

  //---------------------------------------------------------------------
  // Implementation of AbstractApplicationContext
  //---------------------------------------------------------------------

  @Override
  protected void resetCommonCaches() {
    super.resetCommonCaches();
    if (loadingContext != null) {
      loadingContext.clearCache();
    }
  }

  @Override
  public void prepareBeanFactory(ConfigurableBeanFactory beanFactory) {
    super.prepareBeanFactory(beanFactory);
    List<BeanDefinitionLoader> strategies = TodayStrategies.getDetector().getStrategies(
            BeanDefinitionLoader.class, beanFactory);

    DefinitionLoadingContext loadingContext = loadingContext();
    for (BeanDefinitionLoader loader : strategies) {
      loader.loadBeanDefinitions(loadingContext);
    }
  }

  @Override
  protected void postProcessBeanFactory(ConfigurableBeanFactory beanFactory) {
    if (!containsBeanDefinition(AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      BeanDefinition def = new BeanDefinition(
              AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME,
              ConfigurationClassPostProcessor.class);
      def.setConstructorArgs(loadingContext);
      registerBeanDefinition(def);
    }

    AnnotationConfigUtils.registerAnnotationConfigProcessors(loadingContext.getRegistry());
  }

  @Override
  protected void initPropertySources(ConfigurableEnvironment environment) {
    super.initPropertySources(environment);
    try {
      ApplicationPropertySourcesProcessor processor = new ApplicationPropertySourcesProcessor(this);
      if (StringUtils.hasText(propertiesLocation)) {
        processor.setPropertiesLocation(propertiesLocation);
      }
      processor.postProcessEnvironment();

      // prepare properties
      TodayStrategies detector = TodayStrategies.getDetector();
      List<EnvironmentPostProcessor> postProcessors = detector.getStrategies(
              EnvironmentPostProcessor.class, getBeanFactory());
      for (EnvironmentPostProcessor postProcessor : postProcessors) {
        postProcessor.postProcessEnvironment(environment, this);
      }
    }
    catch (IOException e) {
      throw new ApplicationContextException("Environment properties loading failed", e);
    }
  }

  @Override
  public void register(Class<?>... components) {
    getBeanDefinitionReader().registerBean(components);
  }

  @Override
  public void scan(String... basePackages) {
    scanningReader().scan(basePackages);
  }

  /**
   * Provide a custom {@link BeanNamePopulator} for use with {@link AnnotatedBeanDefinitionReader}
   * and/or {@link DefinitionLoadingContext}, if any.
   * <p>Default is {@link AnnotationBeanNamePopulator}.
   * <p>Any call to this method must occur prior to calls to {@link #register(Class...)}
   * and/or {@link #scan(String...)}.
   *
   * @see AnnotatedBeanDefinitionReader#setBeanNameGenerator
   * @see DefinitionLoadingContext#setBeanNameGenerator
   * @see AnnotationBeanNamePopulator
   * @see FullyQualifiedAnnotationBeanNamePopulator
   */
  public void setBeanNameGenerator(BeanNamePopulator beanNamePopulator) {
    Assert.notNull(beanNamePopulator, "BeanNameGenerator is required");
    loadingContext().setBeanNameGenerator(beanNamePopulator);
    getBeanDefinitionReader().setBeanNameGenerator(beanNamePopulator);

    getBeanFactory().registerSingleton(
            AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNamePopulator);
  }

  private ScanningBeanDefinitionReader scanningReader() {
    if (scanningReader == null) {
      scanningReader = new ScanningBeanDefinitionReader(loadingContext());
    }
    return scanningReader;
  }

  private DefinitionLoadingContext loadingContext() {
    if (loadingContext == null) {
      loadingContext = new DefinitionLoadingContext(beanFactory, this);
    }
    return loadingContext;
  }
}
