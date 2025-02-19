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
package cn.taketoday.beans.factory.support;

import cn.taketoday.beans.BeansException;
import cn.taketoday.beans.factory.AutowireCapableBeanFactory;
import cn.taketoday.beans.factory.BeanDefinitionStoreException;
import cn.taketoday.beans.factory.BeanFactory;
import cn.taketoday.beans.factory.BeanFactoryAware;
import cn.taketoday.beans.factory.BeanFactoryPostProcessor;
import cn.taketoday.beans.factory.BeanPostProcessor;
import cn.taketoday.beans.factory.FactoryBean;
import cn.taketoday.beans.factory.HierarchicalBeanFactory;
import cn.taketoday.beans.factory.NoSuchBeanDefinitionException;
import cn.taketoday.beans.factory.Scope;
import cn.taketoday.beans.factory.SingletonBeanRegistry;
import cn.taketoday.core.StringValueResolver;
import cn.taketoday.core.conversion.ConversionService;
import cn.taketoday.lang.Nullable;

/**
 * Configuration interface to be implemented by most bean factories. Provides
 * facilities to configure a bean factory, in addition to the bean factory
 * client methods in the {@link BeanFactory} interface.
 *
 * <p>This bean factory interface is not meant to be used in normal application
 * code: Stick to {@link BeanFactory} for typical needs. This extended interface
 * is just meant to allow for framework-internal plug'n'play and for special
 * access to bean factory configuration methods.
 *
 * <p>
 * Like Spring's ConfigurableListableBeanFactory
 *
 * @author TODAY 2018-11-14 19:40
 * @see BeanFactory
 */
public interface ConfigurableBeanFactory
        extends HierarchicalBeanFactory, SingletonBeanRegistry, AutowireCapableBeanFactory {

  /**
   * Destroy the given bean instance (usually a prototype instance
   * obtained from this factory) according to its bean definition.
   * <p>Any exception that arises during destruction should be caught
   * and logged instead of propagated to the caller of this method.
   *
   * @param beanName the name of the bean definition
   * @param beanInstance the bean instance to destroy
   * @throws NoSuchBeanDefinitionException given bean name not found
   */
  void destroyBean(String beanName, Object beanInstance);

  /**
   * Initialize singletons
   * <p>
   * Ensure that all non-lazy-init singletons are instantiated, also considering
   * {@link FactoryBean FactoryBeans}.
   * Typically invoked at the end of factory setup, if desired.
   * </p>
   *
   * @throws BeansException if one of the singleton beans could not be created.
   * Note: This may have left the factory with some beans already initialized!
   * @since 2.1.2
   */
  void preInstantiateSingletons();

  /**
   * Remove a {@link BeanPostProcessor}
   *
   * @param beanPostProcessor bean post processor instance
   * @since 2.1.2
   */
  void removeBeanPostProcessor(BeanPostProcessor beanPostProcessor);

  /**
   * Register the given scope, backed by the given Scope implementation.
   *
   * @param name scope name
   * @param scope The backing Scope implementation
   * @since 2.1.7
   */
  void registerScope(String name, Scope scope);

  /**
   * Return the names of all currently registered scopes.
   * <p>This will only return the names of explicitly registered scopes.
   * Built-in scopes such as "singleton" and "prototype" won't be exposed.
   *
   * @return the array of scope names, or an empty array if none
   * @see #registerScope
   */
  String[] getRegisteredScopeNames();

  /**
   * Return the Scope implementation for the given scope name, if any.
   * <p>This will only return explicitly registered scopes.
   * Built-in scopes such as "singleton" and "prototype" won't be exposed.
   *
   * @param scopeName the name of the scope
   * @return the registered Scope implementation, or {@code null} if none
   * @see #registerScope
   */
  @Nullable
  Scope getRegisteredScope(String scopeName);

  /**
   * Destroy the specified scoped bean in the current target scope, if any.
   * <p>
   * Any exception that arises during destruction should be caught and logged
   * instead of propagated to the caller of this method.
   *
   * @param beanName the name of the scoped bean
   * @since 2.1.7
   */
  void destroyScopedBean(String beanName);

  /**
   * Set the parent of this bean factory.
   * <p>Note that the parent cannot be changed: It should only be set outside
   * a constructor if it isn't available at the time of factory instantiation.
   *
   * @param parentBeanFactory the parent BeanFactory
   * @throws IllegalStateException if this factory is already associated with
   * a parent BeanFactory
   * @see #getParentBeanFactory()
   * @since 4.0
   */
  void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

  /**
   * Register a special dependency type with corresponding autowired value.
   * <p>This is intended for factory/context references that are supposed
   * to be autowirable but are not defined as beans in the factory:
   * e.g. a dependency of type ApplicationContext resolved to the
   * ApplicationContext instance that the bean is living in.
   * <p>Note: There are no such default types registered in a plain BeanFactory,
   * not even for the BeanFactory interface itself.
   *
   * @param dependencyType the dependency type to register. This will typically
   * be a base interface such as BeanFactory, with extensions of it resolved
   * as well if declared as an autowiring dependency (e.g. BeanFactory),
   * as long as the given value actually implements the extended interface.
   * @param autowiredValue the corresponding autowired value. This may also be an
   * implementation of the {@link java.util.function.Supplier} interface,
   * which allows for lazy resolution of the actual target value.
   * @since 4.0
   */
  void registerDependency(Class<?> dependencyType, @Nullable Object autowiredValue);

  /**
   * Destroy all singleton beans in this factory, including inner beans that have
   * been registered as disposable. To be called on shutdown of a factory.
   * <p>Any exception that arises during destruction should be caught
   * and logged instead of propagated to the caller of this method.
   *
   * @since 4.0
   */
  void destroySingletons();

  /**
   * Specify a 4.0 ConversionService to use for converting property values
   *
   * @param conversionService conversionService
   * @since 4.0
   */
  void setConversionService(@Nullable ConversionService conversionService);

  /**
   * Return the associated ConversionService, if any.
   *
   * @since 4.0
   */
  @Nullable
  ConversionService getConversionService();

  /**
   * Specify the resolution strategy for expressions in bean definition values.
   * <p>There is no expression support active in a BeanFactory by default.
   * An ApplicationContext will typically set a standard expression strategy
   * here, supporting "#{...}" expressions in a Unified EL compatible style.
   *
   * @since 4.0
   */
  void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver);

  /**
   * Return the resolution strategy for expressions in bean definition values.
   *
   * @since 4.0
   */
  @Nullable
  BeanExpressionResolver getBeanExpressionResolver();

  /**
   * Set the class loader to use for loading bean classes.
   * Default is the thread context class loader.
   * <p>Note that this class loader will only apply to bean definitions
   * that do not carry a resolved bean class yet. This is the case
   * by default: Bean definitions only carry bean class names,
   * to be resolved once the factory processes the bean definition.
   *
   * @param beanClassLoader the class loader to use,
   * or {@code null} to suggest the default class loader
   * @since 4.0
   */
  void setBeanClassLoader(@Nullable ClassLoader beanClassLoader);

  /**
   * Return this factory's class loader for loading bean classes
   * (only {@code null} if even the system ClassLoader isn't accessible).
   *
   * @see cn.taketoday.util.ClassUtils#forName(String, ClassLoader)
   * @since 4.0
   */
  @Nullable
  ClassLoader getBeanClassLoader();

  /**
   * Specify a temporary ClassLoader to use for type matching purposes.
   * Default is none, simply using the standard bean ClassLoader.
   * <p>A temporary ClassLoader is usually just specified if
   * <i>load-time weaving</i> is involved, to make sure that actual bean
   * classes are loaded as lazily as possible. The temporary loader is
   * then removed once the BeanFactory completes its bootstrap phase.
   *
   * @since 4.0
   */
  void setTempClassLoader(@Nullable ClassLoader tempClassLoader);

  /**
   * Return the temporary ClassLoader to use for type matching purposes,
   * if any.
   *
   * @since 4.0
   */
  @Nullable
  ClassLoader getTempClassLoader();

  /**
   * Copy all relevant configuration from the given other factory.
   * <p>Should include all standard configuration settings as well as
   * BeanPostProcessors, Scopes, and factory-specific internal settings.
   * Should not include any metadata of actual bean definitions,
   * such as BeanDefinition objects and bean name aliases.
   *
   * @param otherFactory the other BeanFactory to copy from
   * @since 4.0
   */
  void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

  /**
   * Add a String resolver for embedded values such as annotation attributes.
   *
   * @param valueResolver the String resolver to apply to embedded values
   * @since 4.0
   */
  void addEmbeddedValueResolver(StringValueResolver valueResolver);

  /**
   * Determine whether an embedded value resolver has been registered with this
   * bean factory, to be applied through {@link #resolveEmbeddedValue(String)}.
   *
   * @since 4.0
   */
  boolean hasEmbeddedValueResolver();

  /**
   * Resolve the given embedded value, e.g. an annotation attribute.
   *
   * @param value the value to resolve
   * @return the resolved value (may be the original value as-is)
   * @since 4.0
   */
  @Nullable
  String resolveEmbeddedValue(String value);

  /**
   * Add a new BeanPostProcessor that will get applied to beans created
   * by this factory. To be invoked during factory configuration.
   * <p>Note: Post-processors submitted here will be applied in the order of
   * registration; any ordering semantics expressed through implementing the
   * {@link cn.taketoday.core.Ordered} interface will be ignored. Note that
   * autodetected post-processors (e.g. as beans in an ApplicationContext)
   * will always be applied after programmatically registered ones.
   *
   * @param beanPostProcessor the post-processor to register
   * @since 2.1.2
   */
  void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

  /**
   * Return the current number of registered BeanPostProcessors, if any.
   *
   * @since 4.0
   */
  int getBeanPostProcessorCount();

  /**
   * Determine whether the bean with the given name is a FactoryBean.
   *
   * @param name the name of the bean to check
   * @return whether the bean is a FactoryBean
   * ({@code false} means the bean exists but is not a FactoryBean)
   * @throws NoSuchBeanDefinitionException if there is no bean with the given name
   * @since 4.0
   */
  boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

  /**
   * Explicitly control the current in-creation status of the specified bean.
   * For container-internal use only.
   *
   * @param beanName the name of the bean
   * @param inCreation whether the bean is currently in creation
   * @since 4.0
   */
  void setCurrentlyInCreation(String beanName, boolean inCreation);

  /**
   * Determine whether the specified bean is currently in creation.
   *
   * @param beanName the name of the bean
   * @return whether the bean is currently in creation
   * @since 4.0
   */
  boolean isCurrentlyInCreation(String beanName);

  /**
   * Register a dependent bean for the given bean,
   * to be destroyed before the given bean is destroyed.
   *
   * @param beanName the name of the bean
   * @param dependentBeanName the name of the dependent bean
   * @since 4.0
   */
  void registerDependentBean(String beanName, String dependentBeanName);

  /**
   * Return the names of all beans which depend on the specified bean, if any.
   *
   * @param beanName the name of the bean
   * @return the array of dependent bean names, or an empty array if none
   * @since 4.0
   */
  String[] getDependentBeans(String beanName);

  /**
   * Return the names of all beans that the specified bean depends on, if any.
   *
   * @param beanName the name of the bean
   * @return the array of names of beans which the bean depends on,
   * or an empty array if none
   * @since 4.0
   */
  String[] getDependenciesForBean(String beanName);

  /**
   * Given a bean name, create an alias. We typically use this method to
   * support names that are illegal within XML ids (used for bean names).
   * <p>Typically invoked during factory configuration, but can also be
   * used for runtime registration of aliases. Therefore, a factory
   * implementation should synchronize alias access.
   *
   * @param beanName the canonical name of the target bean
   * @param alias the alias to be registered for the bean
   * @throws BeanDefinitionStoreException if the alias is already in use
   * @since 4.0
   */
  void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;

  /**
   * Resolve all alias target names and aliases registered in this
   * factory, applying the given StringValueResolver to them.
   * <p>The value resolver may for example resolve placeholders
   * in target bean names and even in alias names.
   *
   * @param valueResolver the StringValueResolver to apply
   * @since 4.0
   */
  void resolveAliases(StringValueResolver valueResolver);

  /**
   * Freeze all bean definitions, signalling that the registered bean definitions
   * will not be modified or post-processed any further.
   * <p>This allows the factory to aggressively cache bean definition metadata.
   *
   * @since 4.0
   */
  void freezeConfiguration();

  /**
   * Return whether this factory's bean definitions are frozen,
   * i.e. are not supposed to be modified or post-processed any further.
   *
   * @return {@code true} if the factory's configuration is considered frozen
   * @since 4.0
   */
  boolean isConfigurationFrozen();

  /**
   * Clear the merged bean definition cache, removing entries for beans
   * which are not considered eligible for full metadata caching yet.
   * <p>Typically triggered after changes to the original bean definitions,
   * e.g. after applying a {@link BeanFactoryPostProcessor}. Note that metadata
   * for beans which have already been created at this point will be kept around.
   *
   * @see #getBeanDefinition
   * @since 4.0
   */
  void clearMetadataCache();

  /**
   * Ignore the given dependency type for autowiring:
   * for example, String. Default is none.
   *
   * @param type the dependency type to ignore
   * @since 4.0
   */
  void ignoreDependencyType(Class<?> type);

  /**
   * Ignore the given dependency interface for autowiring.
   * <p>This will typically be used by application contexts to register
   * dependencies that are resolved in other ways, like BeanFactory through
   * BeanFactoryAware or ApplicationContext through ApplicationContextAware.
   * <p>By default, only the BeanFactoryAware interface is ignored.
   * For further types to ignore, invoke this method for each type.
   *
   * @param ifc the dependency interface to ignore
   * @see BeanFactoryAware
   * @see cn.taketoday.context.aware.ApplicationContextAware
   * @since 4.0
   */
  void ignoreDependencyInterface(Class<?> ifc);

}
