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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import cn.taketoday.context.annotation.MissingBean;
import cn.taketoday.core.OrderComparator;
import cn.taketoday.core.OrderSourceProvider;
import cn.taketoday.core.Ordered;
import cn.taketoday.core.ResolvableType;
import cn.taketoday.core.annotation.AnnotationAwareOrderComparator;
import cn.taketoday.core.annotation.MergedAnnotation;
import cn.taketoday.lang.Assert;
import cn.taketoday.lang.Nullable;
import cn.taketoday.logging.LogMessage;
import cn.taketoday.logging.Logger;
import cn.taketoday.logging.LoggerFactory;
import cn.taketoday.util.CollectionUtils;
import cn.taketoday.util.StringUtils;

/**
 * Standard {@link BeanFactory} implementation
 *
 * @author TODAY 2019-03-23 15:00
 */
public class StandardBeanFactory
        extends AbstractAutowireCapableBeanFactory implements ConfigurableBeanFactory, BeanDefinitionRegistry {

  private static final Logger log = LoggerFactory.getLogger(StandardBeanFactory.class);

  /** Whether to allow re-registration of a different definition with the same name. */
  private boolean allowBeanDefinitionOverriding = true;

  /** Map of bean definition objects, keyed by bean name */
  private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);

  /** List of bean definition names, in registration order. */
  private final LinkedHashSet<String> beanDefinitionNames = new LinkedHashSet<>(256);

  /** List of names of manually registered singletons, in registration order. */
  private final LinkedHashSet<String> manualSingletonNames = new LinkedHashSet<>(16);

  /** Whether to allow eager class loading even for lazy-init beans. */
  private boolean allowEagerClassLoading = true;

  /** Optional OrderComparator for dependency Lists and arrays. */
  @Nullable
  private Comparator<Object> dependencyComparator;

  /**
   * Create a new StandardBeanFactory.
   */
  public StandardBeanFactory() {
    super();
  }

  /**
   * Create a new StandardBeanFactory with the given parent.
   *
   * @param parentBeanFactory the parent BeanFactory
   */
  public StandardBeanFactory(@Nullable BeanFactory parentBeanFactory) {
    super(parentBeanFactory);
  }

  //---------------------------------------------------------------------
  // Implementation of DefaultSingletonBeanRegistry
  //---------------------------------------------------------------------

  @Override
  public void registerSingleton(String name, Object singleton) {
    super.registerSingleton(name, singleton);
    manualSingletonNames.add(name);
  }

  @Override
  public void removeSingleton(String name) {
    super.removeSingleton(name);
    manualSingletonNames.remove(name);
  }

  @Override
  public void destroySingletons() {
    super.destroySingletons();
    manualSingletonNames.clear();
  }

  @Override
  public void destroySingleton(String beanName) {
    super.destroySingleton(beanName);
    manualSingletonNames.remove(beanName);
  }

  /**
   * Only allows alias overriding if bean definition overriding is allowed.
   */
  @Override
  protected boolean allowAliasOverriding() {
    return isAllowBeanDefinitionOverriding();
  }

  /**
   * Also checks for an alias overriding a bean definition of the same name.
   */
  @Override
  protected void checkForAliasCircle(String name, String alias) {
    super.checkForAliasCircle(name, alias);
    if (!isAllowBeanDefinitionOverriding() && containsBeanDefinition(alias)) {
      throw new IllegalStateException("Cannot register alias '" + alias +
              "' for name '" + name + "': Alias would override bean definition '" + alias + "'");
    }
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
    registerBeanDefinition(def.getName(), def);
  }

  @Override
  public void registerBeanDefinition(String beanName, BeanDefinition def) {
    if (def.getName() == null) {
      def.setName(beanName);
    }
    def = transformBeanDefinition(beanName, def);
    if (def == null) {
      return;
    }
    try {
      def.validate();
    }
    catch (BeanDefinitionValidationException ex) {
      throw new BeanDefinitionStoreException("Validation of bean definition '" + def + "' failed", ex);
    }

    BeanDefinition existBeanDef = getBeanDefinition(beanName);
    if (existBeanDef != null && !def.hasAttribute(MissingBean.MissingBeanMetadata)) {
      if (!isAllowBeanDefinitionOverriding()) {
        throw new BeanDefinitionOverrideException(beanName, def, existBeanDef);
      }
      else if (existBeanDef.getRole() < def.getRole()) {
        // e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
        if (log.isInfoEnabled()) {
          log.info("Overriding user-defined bean definition " +
                  "for bean '{}' with a framework-generated bean " +
                  "definition: replacing [{}] with [{}]", beanName, existBeanDef, def);
        }
      }
      else if (!def.equals(existBeanDef)) {
        if (log.isDebugEnabled()) {
          log.debug("Overriding bean definition for bean '" + beanName +
                  "' with a different definition: replacing [" + existBeanDef +
                  "] with [" + def + "]");
        }
      }
      else {
        if (log.isTraceEnabled()) {
          log.trace("Overriding bean definition for bean '" + beanName +
                  "' with an equivalent definition: replacing [" + existBeanDef +
                  "] with [" + def + "]");
        }
      }
      this.beanDefinitionMap.put(beanName, def);
    }
    else {
      beanDefinitionMap.put(beanName, def);
      beanDefinitionNames.add(beanName);
    }

    if (def.hasAliases()) {
      for (String alias : def.getAliases()) {
        registerAlias(beanName, alias);
      }
    }
    if (existBeanDef != null || containsSingleton(beanName)) {
      resetBeanDefinition(beanName);
    }
  }

  /**
   * @since 3.0
   */
  protected BeanDefinition transformBeanDefinition(String name, BeanDefinition def) {
    BeanDefinition missedDef = null;
    if (containsBeanDefinition(name)) {
      missedDef = getBeanDefinition(name);
    }

    if (missedDef != null && missedDef.hasAttribute(MissingBean.MissingBeanMetadata)) {
      // Have a corresponding missed bean
      // copy all state
      def.copyFrom(missedDef);
      def.setName(name); // fix bean name update error
    }
    // nothing
    return def;
  }

  @Override
  public void removeBeanDefinition(String beanName) {
    Assert.hasText(beanName, "'beanName' must not be empty");
    BeanDefinition removed = beanDefinitionMap.remove(beanName);
    if (removed == null) {
      log.trace("No bean named '{}' found in {}", beanName, this);
    }
    beanDefinitionNames.remove(beanName);
    resetBeanDefinition(beanName);
  }

  @Override
  public BeanDefinition getBeanDefinition(String beanName) {
    return beanDefinitionMap.get(beanName);
  }

  @Override
  public BeanDefinition getBeanDefinition(Class<?> requiredType) {
    Set<String> candidateNames = getBeanNamesForType(requiredType, true, false);
    int size = candidateNames.size();
    if (size == 1) {
      return getBeanDefinition(candidateNames.iterator().next());
    }
    else if (size > 1) {
      String primaryCandidate = getPrimaryCandidate(candidateNames, requiredType);
      if (primaryCandidate != null) {
        return getBeanDefinition(primaryCandidate);
      }
      // fall
      throw new NoUniqueBeanDefinitionException(requiredType, candidateNames);
    }
    return null;
  }

  @Override
  public boolean containsBeanDefinition(Class<?> type) {
    return getBeanDefinition(type) != null;
  }

  @Override
  public boolean containsBeanDefinition(Class<?> type, boolean equals) {
    if (equals) {
      for (String name : getBeanNamesForType(type, true, false)) {
        Class<?> type1 = getType(name);
        if (type1 == type) {
          return true;
        }
      }
      return false;
    }
    return getBeanDefinition(type) != null;
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

  //---------------------------------------------------------------------
  // Implementation of BeanFactory interface
  //---------------------------------------------------------------------

  @Override
  public <T> T getBean(Class<T> requiredType) throws BeansException {
    return getBean(requiredType, (Object[]) null);
  }

  @Override
  public <T> T getBean(Class<T> requiredType, @Nullable Object... args) throws BeansException {
    Assert.notNull(requiredType, "Required type must not be null");
    return resolveBean(ResolvableType.fromRawClass(requiredType), args, false);
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private <T> T resolveBean(
          ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) {
    NamedBeanHolder<T> namedBean = resolveNamedBean(requiredType, args, nonUniqueAsNull);
    if (namedBean != null) {
      return namedBean.getBeanInstance();
    }
    Object dependency = resolveFromObjectFactories(requiredType.resolve());
    if (dependency != null) {
      return (T) dependency;
    }

    BeanFactory parent = getParentBeanFactory();
    if (parent instanceof StandardBeanFactory) {
      return ((StandardBeanFactory) parent).resolveBean(requiredType, args, nonUniqueAsNull);
    }
    else if (parent != null) {
      ObjectSupplier<T> parentProvider = parent.getObjectSupplier(requiredType);
      if (args != null) {
        return parentProvider.get(args);
      }
      else {
        return (nonUniqueAsNull ? parentProvider.getIfUnique() : parentProvider.getIfAvailable());
      }

    }
    return null;
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private <T> NamedBeanHolder<T> resolveNamedBean(
          ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) throws BeansException {
    Assert.notNull(requiredType, "Required type must not be null");
    Set<String> candidateNames = getBeanNamesForType(requiredType);
    int size = candidateNames.size();
    if (size == 1) {
      return resolveNamedBean(candidateNames.iterator().next(), requiredType, args);
    }
    else if (size > 1) {
      String primaryCandidate = determinePrimaryCandidate(candidateNames, requiredType.toClass());
      if (primaryCandidate == null) {
        Map<String, Object> candidates = CollectionUtils.newLinkedHashMap(size);
        for (String beanName : candidateNames) {
          if (containsSingleton(beanName) && args == null) {
            Object beanInstance = getBean(beanName);
            candidates.put(beanName, beanInstance);
          }
          else {
            candidates.put(beanName, getType(beanName));
          }
        }
        primaryCandidate = determineHighestPriorityCandidate(candidates, requiredType.toClass());
        if (primaryCandidate != null) {
          Object beanInstance = candidates.get(primaryCandidate);
          if (beanInstance == null) {
            return null;
          }
          if (beanInstance instanceof Class) {
            return resolveNamedBean(primaryCandidate, requiredType, args);
          }
          return new NamedBeanHolder<>(primaryCandidate, (T) beanInstance);
        }
      }
      if (primaryCandidate != null) {
        return resolveNamedBean(primaryCandidate, requiredType, args);
      }

      // fall
      if (!nonUniqueAsNull) {
        throw new NoUniqueBeanDefinitionException(requiredType, candidateNames);
      }
    }

    return null;
  }

  @Override
  public String getPrimaryCandidate(Set<String> candidateNames, Class<?> requiredType) {
    String primaryCandidate = determinePrimaryCandidate(candidateNames, requiredType);
    if (primaryCandidate == null) {
      Map<String, Object> candidates = CollectionUtils.newLinkedHashMap(candidateNames.size());
      for (String beanName : candidateNames) {
        if (containsSingleton(beanName)) {
          Object beanInstance = getBean(beanName);
          candidates.put(beanName, beanInstance);
        }
        else {
          candidates.put(beanName, getType(beanName));
        }
      }
      primaryCandidate = determineHighestPriorityCandidate(candidates, requiredType);
    }
    return primaryCandidate;
  }

  @Nullable
  private <T> NamedBeanHolder<T> resolveNamedBean(
          String beanName, ResolvableType requiredType, Object[] args) throws BeansException {
    Object bean = doGetBean(beanName, null, args, false);
    if (bean == null) {
      return null;
    }
    return new NamedBeanHolder<>(beanName, adaptBeanInstance(beanName, bean, requiredType.toClass()));
  }

  /**
   * Determine the primary candidate in the given set of beans.
   *
   * @param candidates a set of candidate names
   * @param requiredType the target dependency type to match against
   * @return the name of the primary candidate, or {@code null} if none found
   * @see #isPrimary(String)
   */
  @Nullable
  protected String determinePrimaryCandidate(Set<String> candidates, Class<?> requiredType) {
    String primaryBeanName = null;
    for (String candidateBeanName : candidates) {
      if (isPrimary(candidateBeanName)) {
        if (primaryBeanName != null) {
          boolean candidateLocal = containsBeanDefinition(candidateBeanName);
          boolean primaryLocal = containsBeanDefinition(primaryBeanName);
          if (candidateLocal && primaryLocal) {
            throw new NoUniqueBeanDefinitionException(
                    requiredType, candidates.size(),
                    "more than one 'primary' bean found among candidates: " + candidates);
          }
          else if (candidateLocal) {
            primaryBeanName = candidateBeanName;
          }
        }
        else {
          primaryBeanName = candidateBeanName;
        }
      }
    }
    return primaryBeanName;
  }

  /**
   * Determine the candidate with the highest priority in the given set of beans.
   * <p>Based on {@code @jakarta.annotation.Priority}. As defined by the related
   * {@link Ordered} interface, the lowest value has the highest priority.
   *
   * @param candidates a set of candidate names
   * @param requiredType the target dependency type to match against
   * @return the name of the candidate with the highest priority,
   * or {@code null} if none found
   * @see #getPriority(Object)
   */
  @Nullable
  protected String determineHighestPriorityCandidate(Map<String, Object> candidates, Class<?> requiredType) {
    String highestPriorityBeanName = null;
    Integer highestPriority = null;
    for (Map.Entry<String, Object> entry : candidates.entrySet()) {
      String candidateBeanName = entry.getKey();
      Object beanInstance = entry.getValue();
      if (beanInstance != null) {
        Integer candidatePriority = getPriority(beanInstance);
        if (candidatePriority != null) {
          if (highestPriorityBeanName != null) {
            if (candidatePriority.equals(highestPriority)) {
              throw new NoUniqueBeanDefinitionException(
                      requiredType, candidates.size(),
                      "Multiple beans found with the same priority ('" + highestPriority +
                              "') among candidates: " + candidates.keySet());
            }
            else if (candidatePriority < highestPriority) {
              highestPriorityBeanName = candidateBeanName;
              highestPriority = candidatePriority;
            }
          }
          else {
            highestPriorityBeanName = candidateBeanName;
            highestPriority = candidatePriority;
          }
        }
      }
    }
    return highestPriorityBeanName;
  }

  /**
   * Return whether the bean definition for the given bean name has been
   * marked as a primary bean.
   *
   * @param beanName the name of the bean
   * @return whether the given bean qualifies as primary
   */
  protected boolean isPrimary(String beanName) {
    if (containsBeanDefinition(beanName)) {
      return beanDefinitionMap.get(beanName).isPrimary();
    }
    BeanFactory parent = getParentBeanFactory();
    return (parent instanceof StandardBeanFactory &&
            ((StandardBeanFactory) parent).isPrimary(beanName));
  }

  /**
   * Return the priority assigned for the given bean instance by
   * the {@code jakarta.annotation.Priority} annotation.
   *
   * @param beanInstance the bean instance to check (can be {@code null})
   * @return the priority assigned to that bean or {@code null} if none is set
   */
  @Nullable
  protected Integer getPriority(Object beanInstance) {
    return AnnotationAwareOrderComparator.INSTANCE.getPriority(beanInstance);
  }

  @Override
  public <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException {
    Assert.notNull(requiredType, "Required type must not be null");
    NamedBeanHolder<T> namedBean = resolveNamedBean(ResolvableType.fromClass(requiredType), null, false);
    if (namedBean != null) {
      return namedBean;
    }
    BeanFactory parent = getParentBeanFactory();
    if (parent instanceof AutowireCapableBeanFactory) {
      return ((AutowireCapableBeanFactory) parent).resolveNamedBean(requiredType);
    }
    throw new NoSuchBeanDefinitionException(requiredType);
  }

  //---------------------------------------------------------------------
  // Listing Get operations for type-lookup
  //---------------------------------------------------------------------

  @Override
  public <T> ObjectSupplier<T> getObjectSupplier(Class<T> requiredType) {
    return getObjectSupplier(requiredType, true);
  }

  @Override
  public <T> ObjectSupplier<T> getObjectSupplier(ResolvableType requiredType) {
    return getObjectSupplier(requiredType, true);
  }

  @Override
  public <T> ObjectSupplier<T> getObjectSupplier(Class<T> requiredType, boolean allowEagerInit) {
    Assert.notNull(requiredType, "Required type must not be null");
    return getObjectSupplier(ResolvableType.fromRawClass(requiredType), allowEagerInit);
  }

  @Override
  public <T> ObjectSupplier<T> getObjectSupplier(ResolvableType requiredType, boolean allowEagerInit) {
    return new BeanObjectSupplier<>() {
      @Override
      public T get() throws BeansException {
        T resolved = resolveBean(requiredType, null, false);
        if (resolved == null) {
          throw new NoSuchBeanDefinitionException(requiredType);
        }
        return resolved;
      }

      @Override
      public T get(Object... args) throws BeansException {
        T resolved = resolveBean(requiredType, args, false);
        if (resolved == null) {
          throw new NoSuchBeanDefinitionException(requiredType);
        }
        return resolved;
      }

      @Override
      @Nullable
      public T getIfAvailable() throws BeansException {
        return resolveBean(requiredType, null, false);
      }

      @Override
      public void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
        T dependency = getIfAvailable();
        if (dependency != null) {
          dependencyConsumer.accept(dependency);
        }
      }

      @Override
      @Nullable
      public T getIfUnique() throws BeansException {
        return resolveBean(requiredType, null, true);
      }

      @Override
      public void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
        T dependency = getIfUnique();
        if (dependency != null) {
          dependencyConsumer.accept(dependency);
        }
      }

      @SuppressWarnings("unchecked")
      @Override
      public Stream<T> stream() {
        return getBeanNamesForTypedStream(requiredType, allowEagerInit)
                .stream()
                .map(name -> (T) getBean(name))
                .filter(Objects::nonNull);
      }

      @SuppressWarnings("unchecked")
      @Override
      public Stream<T> orderedStream() {
        Set<String> beanNames = getBeanNamesForTypedStream(requiredType, allowEagerInit);
        if (beanNames.isEmpty()) {
          return Stream.empty();
        }
        Map<String, T> matchingBeans = CollectionUtils.newLinkedHashMap(beanNames.size());
        for (String beanName : beanNames) {
          Object beanInstance = getBean(beanName);
          if (beanInstance != null) {
            matchingBeans.put(beanName, (T) beanInstance);
          }
        }
        Stream<T> stream = matchingBeans.values().stream();
        return stream.sorted(adaptOrderComparator(matchingBeans));
      }
    };
  }

  private Set<String> getBeanNamesForTypedStream(ResolvableType requiredType, boolean allowEagerInit) {
    return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType, true, allowEagerInit);
  }

  private Comparator<Object> adaptOrderComparator(Map<String, ?> matchingBeans) {
    Comparator<Object> dependencyComparator = getDependencyComparator();
    OrderComparator comparator = dependencyComparator instanceof OrderComparator
                                 ? (OrderComparator) dependencyComparator : OrderComparator.INSTANCE;
    return comparator.withSourceProvider(createFactoryAwareOrderSourceProvider(matchingBeans));
  }

  private OrderSourceProvider createFactoryAwareOrderSourceProvider(Map<String, ?> beans) {
    return new FactoryAwareOrderSourceProvider(this, beans);
  }

  @Override
  public <T> Map<String, T> getBeansOfType(
          Class<T> requiredType, boolean includeNonSingletons, boolean allowEagerInit) {
    return getBeansOfType(ResolvableType.fromRawClass(requiredType), includeNonSingletons, allowEagerInit);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Map<String, T> getBeansOfType(
          @Nullable ResolvableType requiredType, boolean includeNonSingletons, boolean allowEagerInit) {
    Set<String> beanNames = getBeanNamesForType(requiredType, includeNonSingletons, allowEagerInit);
    Map<String, T> beans = CollectionUtils.newLinkedHashMap(beanNames.size());
    for (String beanName : beanNames) {
      try {
        Object beanInstance = getBean(beanName);
        if (beanInstance != null) {
          beans.put(beanName, (T) beanInstance);
        }
      }
      catch (BeanCreationException ex) {
        Throwable rootCause = ex.getMostSpecificCause();
        if (rootCause instanceof BeanCurrentlyInCreationException bce) {
          String exBeanName = bce.getBeanName();
          if (exBeanName != null && isCurrentlyInCreation(exBeanName)) {
            if (log.isTraceEnabled()) {
              log.trace("Ignoring match to currently created bean '{}': ",
                      exBeanName, ex.getMessage());
            }
            onSuppressedException(ex);
            // Ignore: indicates a circular reference when autowiring constructors.
            // We want to find matches other than the currently created bean itself.
            continue;
          }
        }
        throw ex;
      }
    }
    return beans;
  }

  // getBeanNamesOfType

  @Override
  public Set<String> getBeanNamesForType(Class<?> requiredType, boolean includeNonSingletons) {
    return getBeanNamesForType(requiredType, true, includeNonSingletons);
  }

  @Override
  public Set<String> getBeanNamesForType(
          Class<?> requiredType, boolean includeNonSingletons, boolean allowEagerInit) {
    return getBeanNamesForType(ResolvableType.fromRawClass(requiredType), includeNonSingletons, allowEagerInit);
  }

  @Override
  public Set<String> getBeanNamesForType(ResolvableType type) {
    return getBeanNamesForType(type, true, true);
  }

  @Override
  public Set<String> getBeanNamesForType(
          ResolvableType requiredType, boolean includeNonSingletons, boolean allowEagerInit) {
    LinkedHashSet<String> beanNames = new LinkedHashSet<>();

    // 1. Check all bean definitions.
    for (String beanName : beanDefinitionNames) {
      // Only consider bean as eligible if the bean name is not defined as alias for some other bean.
      if (!isAlias(beanName)) {
        try {
          BeanDefinition definition = beanDefinitionMap.get(beanName);
          // Only check bean definition if it is complete.
          if (allowEagerInit || allowCheck(definition)) {
            boolean matchFound = false;
            boolean allowFactoryBeanInit = allowEagerInit || containsSingleton(beanName);
            if (isFactoryBean(definition)) {
              if (includeNonSingletons || (allowFactoryBeanInit && isSingleton(beanName))) {
                matchFound = isTypeMatch(beanName, requiredType, allowFactoryBeanInit);
              }
              if (!matchFound) {
                // In case of FactoryBean, try to match FactoryBean instance itself next.
                beanName = FACTORY_BEAN_PREFIX + beanName;
                matchFound = isTypeMatch(beanName, requiredType, allowFactoryBeanInit);
              }
            }
            else {
              if (includeNonSingletons || isSingleton(beanName)) {
                matchFound = isTypeMatch(beanName, requiredType, allowFactoryBeanInit);
              }
            }
            if (matchFound) {
              beanNames.add(beanName);
            }
          }
        }
        catch (BeanClassLoadFailedException | BeanDefinitionStoreException ex) {
          if (allowEagerInit) {
            throw ex;
          }
          // Probably a placeholder: let's ignore it for type matching purposes.
          LogMessage message =
                  (ex instanceof BeanClassLoadFailedException
                   ? LogMessage.format("Ignoring bean class loading failure for bean '%s'", beanName)
                   : LogMessage.format("Ignoring unresolvable metadata in bean definition '%s'", beanName));
          log.trace(message, ex);
          // Register exception, in case the bean was accidentally unresolvable.
          onSuppressedException(ex);
        }
        catch (NoSuchBeanDefinitionException ex) {
          // Bean definition got removed while we were iterating -> ignore.
        }
      }
    }

    // 2. Check manually registered singletons too.
    for (String beanName : this.manualSingletonNames) {
      if (beanNames.contains(beanName)) {
        continue;
      }
      try {
        // In case of FactoryBean, match object created by FactoryBean.
        if (isFactoryBean(beanName)) {
          if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, requiredType)) {
            beanNames.add(beanName);
            // Match found for this bean: do not match FactoryBean itself anymore.
            continue;
          }
          // In case of FactoryBean, try to match FactoryBean itself next.
          beanName = FACTORY_BEAN_PREFIX + beanName;
        }
        // Match raw bean instance (might be raw FactoryBean).
        if (isTypeMatch(beanName, requiredType)) {
          beanNames.add(beanName);
        }
      }
      catch (NoSuchBeanDefinitionException ex) {
        // Shouldn't happen - probably a result of circular reference resolution...
        log.trace("Failed to check manually registered singleton with name '{}'", beanName, ex);
      }
    }
    return beanNames;
  }

  private boolean allowCheck(BeanDefinition definition) {
    return (
            definition.hasBeanClass()
                    || !definition.isLazyInit()
                    || isAllowEagerClassLoading()
    ) && !requiresEagerInitForType(definition.getFactoryBeanName());
  }

  /**
   * Check whether the specified bean would need to be eagerly initialized
   * in order to determine its type.
   *
   * @param factoryBeanName a factory-bean reference that the bean definition
   * defines a factory method for
   * @return whether eager initialization is necessary
   */
  private boolean requiresEagerInitForType(@Nullable String factoryBeanName) {
    return factoryBeanName != null && isFactoryBean(factoryBeanName) && !containsSingleton(factoryBeanName);
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
      if (bd != null && getMergedAnnotation(beanName, annotationType).isPresent()) {
        names.add(beanName);
      }
    }

    for (String beanName : manualSingletonNames) {
      if (!names.contains(beanName) && getMergedAnnotation(beanName, annotationType).isPresent()) {
        names.add(beanName);
      }
    }

    return names;
  }

  @Override
  public <A extends Annotation> A getAnnotationOnBean(String beanName, Class<A> annotationType) {
    return getMergedAnnotation(beanName, annotationType)
            .synthesize(MergedAnnotation::isPresent).orElse(null);
  }

  @Override
  public <A extends Annotation> MergedAnnotation<A> getMergedAnnotation(
          String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
    return BeanFactoryUtils.getMergedAnnotation(this, beanName, annotationType);
  }

  //---------------------------------------------------------------------
  // Implementation of ConfigurableBeanFactory interface @since 4.0
  //---------------------------------------------------------------------

  @Override
  public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
    super.copyConfigurationFrom(otherFactory);
    if (otherFactory instanceof StandardBeanFactory std) {
      this.dependencyComparator = std.dependencyComparator;
      this.allowEagerClassLoading = std.allowEagerClassLoading;
      this.allowBeanDefinitionOverriding = std.allowBeanDefinitionOverriding;
    }
  }

  /**
   * Reset all bean definition caches for the given bean,
   * including the caches of beans that are derived from it.
   * <p>Called after an existing bean definition has been replaced or removed,
   * triggering {@link #destroySingleton} and {@link BeanDefinitionPostProcessor#resetBeanDefinition}
   * on the given bean.
   *
   * @param beanName the name of the bean to reset
   * @see #registerBeanDefinition
   * @see #removeBeanDefinition
   */
  protected void resetBeanDefinition(String beanName) {
    // Remove corresponding bean from singleton cache, if any. Shouldn't usually
    // be necessary, rather just meant for overriding a context's default beans
    destroySingleton(beanName);

    // Notify all post-processors that the specified bean definition has been reset.
    for (BeanDefinitionPostProcessor processor : postProcessors().definitions) {
      processor.resetBeanDefinition(beanName);
    }
  }

  /**
   * Set whether the factory is allowed to eagerly load bean classes
   * even for bean definitions that are marked as "lazy-init".
   * <p>Default is "true". Turn this flag off to suppress class loading
   * for lazy-init beans unless such a bean is explicitly requested.
   * In particular, by-type lookups will then simply ignore bean definitions
   * without resolved class name, instead of loading the bean classes on
   * demand just to perform a type check.
   *
   * @see BeanDefinition#setLazyInit
   * @since 4.0
   */
  public void setAllowEagerClassLoading(boolean allowEagerClassLoading) {
    this.allowEagerClassLoading = allowEagerClassLoading;
  }

  /**
   * Return whether the factory is allowed to eagerly load bean classes
   * even for bean definitions that are marked as "lazy-init".
   *
   * @since 4.0
   */
  public boolean isAllowEagerClassLoading() {
    return this.allowEagerClassLoading;
  }

  /**
   * Set a {@link java.util.Comparator} for dependency Lists and arrays.
   *
   * @see OrderComparator
   * @see AnnotationAwareOrderComparator
   * @since 4.0
   */
  public void setDependencyComparator(@Nullable Comparator<Object> dependencyComparator) {
    this.dependencyComparator = dependencyComparator;
  }

  /**
   * Return the dependency comparator for this BeanFactory (may be {@code null}.
   *
   * @since 4.0
   */
  @Nullable
  public Comparator<Object> getDependencyComparator() {
    return this.dependencyComparator;
  }

  private interface BeanObjectSupplier<T> extends ObjectSupplier<T>, Serializable { }

}
