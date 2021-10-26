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
package cn.taketoday.beans.factory;

import cn.taketoday.beans.FactoryBean;
import cn.taketoday.beans.IgnoreDuplicates;
import cn.taketoday.context.annotation.MissingBean;
import cn.taketoday.core.annotation.MergedAnnotation;
import cn.taketoday.core.annotation.MergedAnnotations;
import cn.taketoday.core.annotation.MergedAnnotations.SearchStrategy;
import cn.taketoday.lang.Assert;
import cn.taketoday.lang.Nullable;
import cn.taketoday.lang.Prototype;
import cn.taketoday.logging.Logger;
import cn.taketoday.logging.LoggerFactory;
import cn.taketoday.util.CollectionUtils;
import cn.taketoday.util.ExceptionUtils;
import cn.taketoday.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Standard {@link BeanFactory} implementation
 *
 * @author TODAY 2019-03-23 15:00
 */
public class StandardBeanFactory
        extends AbstractAutowireCapableBeanFactory implements ConfigurableBeanFactory, BeanDefinitionRegistry {

  private static final Logger log = LoggerFactory.getLogger(StandardBeanFactory.class);

  /**
   * @since 2.1.7 Preventing repeated initialization of beans(Prevent duplicate
   * initialization) , Prevent Cycle Dependency
   */
  private final HashSet<String> currentInitializingBeanName = new HashSet<>();

  /** Whether to allow re-registration of a different definition with the same name. */
  private boolean allowBeanDefinitionOverriding = true;

  /** Map of bean definition objects, keyed by bean name */
  private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);

  /** List of bean definition names, in registration order. */
  private final ArrayList<String> beanDefinitionNames = new ArrayList<>(256);

  /**
   * Preventing Cycle Dependency expected {@link Prototype} beans
   */
  @Override
  public Object initializeBean(final Object bean, final BeanDefinition def) {
    if (def.isPrototype()) {
      return super.initializeBean(bean, def);
    }

    final String name = def.getName();
    if (currentInitializingBeanName.contains(name)) {
      return bean;
    }
    currentInitializingBeanName.add(name);
    final Object initializingBean = super.initializeBean(bean, def);
    currentInitializingBeanName.remove(name);
    return initializingBean;
  }

  /**
   * Register {@link FactoryBeanDefinition} to the {@link BeanFactory}
   *
   * @param oldBeanName Target old bean name
   * @param factoryDef {@link FactoryBean} Bean definition
   */
  protected void registerFactoryBean(final String oldBeanName, final BeanDefinition factoryDef) {

    final FactoryBeanDefinition<?> def = //
            factoryDef instanceof FactoryBeanDefinition
                    ? (FactoryBeanDefinition<?>) factoryDef
                    : new FactoryBeanDefinition<>(factoryDef, this);

    registerBeanDefinition(oldBeanName, def);
  }

  //---------------------------------------------------------------------
  // Implementation of BeanDefinitionRegistry interface @since 4.0
  //---------------------------------------------------------------------

  @Override
  public Map<String, BeanDefinition> getBeanDefinitions() {
    return beanDefinitionMap;
  }

  @Override
  public void registerBeanDefinition(BeanDefinition def) {
    beanDefinitionMap.put(def.getName(), def);
  }

  @Override
  public void removeBeanDefinition(String beanName) {
    beanDefinitionMap.remove(beanName);
  }

  @Override
  public BeanDefinition getBeanDefinition(String beanName) {
    return beanDefinitionMap.get(beanName);
  }

  @Override
  public BeanDefinition getBeanDefinition(Class<?> beanClass) {
    BeanDefinition def = getBeanDefinition(createBeanName(beanClass));
    if (def != null) {
      if (isAssignableTo(def, beanClass)) {
        return def;
      }
    }

    for (BeanDefinition definition : beanDefinitionMap.values()) {
      if (isAssignableTo(definition, beanClass)) {
        return def;
      }
    }
    return null;
  }

  private boolean isAssignableTo(BeanDefinition definition, Class<?> beanClass) {
    if (definition.hasBeanClass()) {
      return definition.isAssignableTo(beanClass);
    }
    else {
      Class<?> candidateClass = resolveBeanClass(definition);
      return candidateClass != null && beanClass.isAssignableFrom(candidateClass);
    }
  }

  @Override
  public boolean containsBeanDefinition(Class<?> type) {
    return containsBeanDefinition(type, false);
  }

  @Override
  public boolean containsBeanDefinition(Class<?> type, boolean equals) {
    // TODO optimise lookup performance
    Predicate<BeanDefinition> predicate = getPredicate(type, equals);
    BeanDefinition def = getBeanDefinition(createBeanName(type));
    if (def != null && predicate.test(def)) {
      return true;
    }

    for (BeanDefinition beanDef : getBeanDefinitions().values()) {
      if (predicate.test(beanDef)) {
        return true;
      }
    }
    return false;
  }

  private Predicate<BeanDefinition> getPredicate(Class<?> type, boolean equals) {
    return equals
            ? beanDef -> type == beanDef.getBeanClass()
            : beanDef -> type.isAssignableFrom(beanDef.getBeanClass());
  }

  @Override
  public boolean containsBeanDefinition(String beanName, Class<?> type) {
    return containsBeanDefinition(beanName) && containsBeanDefinition(type);
  }

  @Override
  public boolean containsBeanDefinition(String beanName) {
    return beanDefinitionMap.containsKey(beanName);
  }

  @Override
  public String[] getBeanDefinitionNames() {
    return StringUtils.toStringArray(beanDefinitionNames);
  }

  @Override
  public Iterator<String> getBeanNamesIterator() {
    return beanDefinitionMap.keySet().iterator();
  }

  @Override
  public int getBeanDefinitionCount() {
    return beanDefinitionMap.size();
  }

  @Override
  public boolean isBeanNameInUse(String beanName) {
    return beanDefinitionMap.containsKey(beanName);
  }

  @Override
  public Iterator<BeanDefinition> iterator() {
    return beanDefinitionMap.values().iterator();
  }

  /**
   * Set whether it should be allowed to override bean definitions by registering
   * a different definition with the same name, automatically replacing the former.
   * If not, an exception will be thrown. This also applies to overriding aliases.
   * <p>Default is "true".
   *
   * @see #registerBeanDefinition
   * @since 4.0
   */
  public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
    this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
  }

  /**
   * Return whether it should be allowed to override bean definitions by registering
   * a different definition with the same name, automatically replacing the former.
   *
   * @since 4.0
   */
  @Override
  public boolean isAllowBeanDefinitionOverriding() {
    return this.allowBeanDefinitionOverriding;
  }

  @Override
  public void registerBeanDefinition(String beanName, BeanDefinition def) {
    if (FactoryBean.class.isAssignableFrom(def.getBeanClass())) { // process FactoryBean
      registerFactoryBean(beanName, def);
    }
    else {
      register(beanName, def);
      postProcessRegisterBeanDefinition(def);
    }
  }

  /**
   * Register bean definition with given name
   *
   * @param name Bean name
   * @param def Bean definition
   * @throws BeanDefinitionStoreException If can't store bean
   */
  @Nullable
  public BeanDefinition register(String name, BeanDefinition def) {
    def = transformBeanDefinition(name, def);
    if (def == null) {
      return null;
    }

    def.validate();
    String nameToUse = name;
    BeanDefinition existBeanDef = getBeanDefinition(name);

    if (existBeanDef != null && !def.hasAttribute(MissingBean.MissingBeanMetadata)) {
      // has same name
      String beanClassName = def.getBeanClassName();
      if (Objects.equals(existBeanDef.getBeanClassName(), beanClassName)
              && existBeanDef.isAnnotationPresent(IgnoreDuplicates.class)) { // @since 3.0.2
        return null; // ignore registering
      }

      if (!isAllowBeanDefinitionOverriding()) {
        throw new BeanDefinitionOverrideException(name, def, existBeanDef);
      }
      else if (existBeanDef.getRole() < def.getRole()) {
        // e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
        if (log.isInfoEnabled()) {
          log.info("Overriding user-defined bean definition for bean '" + name +
                  "' with a framework-generated bean definition: replacing [" +
                  existBeanDef + "] with [" + def + "]");
        }
      }

      log.info("=====================|repeat bean definition START|=====================");
      log.info("There is already a bean called: [{}], its bean definition: [{}].", name, existBeanDef);
      if (Objects.equals(existBeanDef.getBeanClassName(), beanClassName)) {
        log.warn("They have same bean class: [{}]. We will override it.", beanClassName);
      }
      else {
        nameToUse = beanClassName;
        def.setName(nameToUse);
        log.warn("Current bean class: [{}]. You are supposed to change your bean name creator or bean name.",
                beanClassName);
        log.warn("Current bean definition: [{}] will be registed as: [{}].", def, nameToUse);
      }
      log.info("======================|END|======================");
    }

    try {
      this.beanDefinitionMap.put(nameToUse, def);
      postProcessRegisterBeanDefinition(def);
      return def;
    }
    catch (Throwable ex) {
      ex = ExceptionUtils.unwrapThrowable(ex);
      throw new BeanDefinitionStoreException(
              "An Exception Occurred When Register Bean Definition: [" + def + "]", ex);
    }
  }

  /**
   * @since 3.0
   */
  protected BeanDefinition transformBeanDefinition(String name, BeanDefinition def) {
    Class<?> beanClass = def.getBeanClass();

    BeanDefinition missedDef = null;
    if (containsBeanDefinition(name)) {
      missedDef = getBeanDefinition(name);
    }
    else if (containsBeanDefinition(beanClass)) {
      missedDef = getBeanDefinition(beanClass);
    }

    if (missedDef != null
            && missedDef.hasAttribute(MissingBean.MissingBeanMetadata)) {
      // Have a corresponding missed bean
      // copy all state
      def.copyFrom(missedDef);
      def.setName(name); // fix bean name update error
    }
    // nothing
    return def;
  }

  /**
   * Process after register {@link BeanDefinition}
   *
   * @param targetDef Target {@link BeanDefinition}
   */
  protected void postProcessRegisterBeanDefinition(BeanDefinition targetDef) {

  }

  //---------------------------------------------------------------------
  // Implementation of BeanFactory interface
  //---------------------------------------------------------------------

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getBean(Class<T> requiredType) {
    return (T) doGetBeanForType(requiredType);
  }

  /**
   * Get bean for required type
   *
   * @param requiredType Bean type
   * @since 2.1.2
   */
  protected <T> Object doGetBeanForType(Class<T> requiredType) {
    for (Map.Entry<String, BeanDefinition> entry : getBeanDefinitions().entrySet()) {
      if (entry.getValue().isAssignableTo(requiredType)) {
        Object bean = getBean(entry.getValue());
        if (bean != null) {
          return bean;
        }
      }
    }
    // fix
    for (Object entry : getSingletons().values()) {
      if (requiredType.isAssignableFrom(entry.getClass())) {
        return entry;
      }
    }
    return null;
  }

  @Override
  public Map<String, Object> getBeansOfAnnotation(
          Class<? extends Annotation> annotationType, boolean includeNonSingletons) {
    Assert.notNull(annotationType, "annotationType must not be null");

    Set<String> beanNames = getBeanNamesForAnnotation(annotationType);
    Map<String, Object> result = CollectionUtils.newLinkedHashMap(beanNames.size());
    for (String beanName : beanNames) {
      Object beanInstance = getBean(beanName);
      result.put(beanName, beanInstance);
    }
    return result;
  }

  @Override
  public Set<String> getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
    Assert.notNull(annotationType, "annotationType must not be null");
    LinkedHashSet<String> names = new LinkedHashSet<>();

    for (String beanName : beanDefinitionNames) {
      BeanDefinition bd = beanDefinitionMap.get(beanName);
      if (bd != null && !bd.isAbstract() && getMergedAnnotationOnBean(beanName, annotationType).isPresent()) {
        names.add(beanName);
      }
    }

    for (String beanName : getSingletonNames()) {
      if (!names.contains(beanName) && getMergedAnnotationOnBean(beanName, annotationType).isPresent()) {
        names.add(beanName);
      }
    }

    return names;
  }

  @Override
  public <A extends Annotation> A getAnnotationOnBean(String beanName, Class<A> annotationType) {
    return getMergedAnnotationOnBean(beanName, annotationType)
            .synthesize(MergedAnnotation::isPresent).orElse(null);
  }

  @Override
  public <A extends Annotation> MergedAnnotation<A> getMergedAnnotationOnBean(
          String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
    return findMergedAnnotationOnBean(beanName, annotationType);
  }

  private <A extends Annotation> MergedAnnotation<A> findMergedAnnotationOnBean(
          String beanName, Class<A> annotationType) {

    Class<?> beanType = getType(beanName);
    if (beanType != null) {
      MergedAnnotation<A> annotation =
              MergedAnnotations.from(beanType, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
      if (annotation.isPresent()) {
        return annotation;
      }
    }

    if (containsBeanDefinition(beanName)) {
      BeanDefinition definition = beanDefinitionMap.get(beanName);
      if (definition instanceof FactoryMethodBeanDefinition) {
        // Check annotations declared on factory method, if any.
        Method factoryMethod = ((FactoryMethodBeanDefinition) definition).getFactoryMethod();
        if (factoryMethod != null) {
          MergedAnnotation<A> annotation =
                  MergedAnnotations.from(factoryMethod, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
          if (annotation.isPresent()) {
            return annotation;
          }
        }
      }

      // Check raw bean class, e.g. in case of a proxy.
      if (definition.hasBeanClass()) {
        Class<?> beanClass = definition.getBeanClass();
        if (beanClass != beanType) {
          MergedAnnotation<A> annotation =
                  MergedAnnotations.from(beanClass, SearchStrategy.TYPE_HIERARCHY).get(annotationType);
          if (annotation.isPresent()) {
            return annotation;
          }
        }
      }
    }
    return MergedAnnotation.missing();
  }

  //---------------------------------------------------------------------
  // Implementation of ConfigurableBeanFactory interface @since 4.0
  //---------------------------------------------------------------------

  @Override
  public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
    super.copyConfigurationFrom(otherFactory);
    if (otherFactory instanceof StandardBeanFactory) {
      this.allowBeanDefinitionOverriding = ((StandardBeanFactory) otherFactory).allowBeanDefinitionOverriding;
    }
  }

  @Override
  public void removeBean(String name) {
    removeBeanDefinition(name);
    super.removeBean(name);
  }

}
