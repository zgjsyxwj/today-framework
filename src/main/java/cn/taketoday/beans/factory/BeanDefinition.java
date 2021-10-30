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
package cn.taketoday.beans.factory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import cn.taketoday.beans.FactoryBean;
import cn.taketoday.beans.NoSuchPropertyException;
import cn.taketoday.core.AttributeAccessor;
import cn.taketoday.core.ResolvableType;
import cn.taketoday.lang.Constant;
import cn.taketoday.lang.Nullable;
import cn.taketoday.lang.Prototype;
import cn.taketoday.lang.Singleton;
import cn.taketoday.util.StringUtils;

/**
 * Bean definition
 *
 * @author TODAY 2018-06-23 11:23:45
 */
public interface BeanDefinition extends AttributeAccessor {
  String INIT_METHODS = "initMethods";
  String DESTROY_METHOD = "destroyMethod";

  Method[] EMPTY_METHOD = Constant.EMPTY_METHOD_ARRAY;

  /**
   * Role hint indicating that a {@code BeanDefinition} is a major part
   * of the application. Typically， corresponds to a user-defined bean.
   */
  int ROLE_APPLICATION = 0;

  /**
   * Role hint indicating that a {@code BeanDefinition} is providing an
   * entirely background role and has no relevance to the end-user. This hint is
   * used when registering beans that are completely part of the internal workings
   */
  int ROLE_INFRASTRUCTURE = 2;

  /**
   * Get a property
   *
   * @param name The name of property
   * @return Property value object
   * @throws NoSuchPropertyException If there is no property with given name
   */
  Object getPropertyValue(String name) throws NoSuchPropertyException;

  /**
   * Indicates that If the bean is a {@link Singleton}.
   *
   * @return If the bean is a {@link Singleton}.
   */
  boolean isSingleton();

  /**
   * Indicates that If the bean is a
   * {@link Prototype Prototype}.
   *
   * @return If the bean is a {@link Prototype
   * Prototype}.
   * @since 2.17
   */
  boolean isPrototype();

  /**
   * Get bean class
   *
   * @return bean class
   */
  Class<?> getBeanClass();

  /**
   * Get init methods
   *
   * @return Get all the init methods, never be null
   */
  String[] getInitMethods();

  /**
   * Get all the destroy methods name
   *
   * @return all the destroy methods name, never be null
   */
  @Nullable
  String getDestroyMethod();

  /**
   * Get Bean {@link Scope}
   *
   * @return Bean {@link Scope}
   */
  String getScope();

  /**
   * Get bean name
   *
   * @return Bean name
   */
  String getName();

  /**
   * If bean is a {@link FactoryBean}
   *
   * @return If Bean is a {@link FactoryBean}
   */
  Boolean isFactoryBean();

  /**
   * If a {@link Singleton} has initialized
   * <p>
   * If this bean is created from {@link FactoryBean} This explains
   * {@link FactoryBean} is initialized
   *
   * @return If Bean or {@link FactoryBean} is initialized
   */
  boolean isInitialized();

  /**
   * if it is from abstract class.
   *
   * @return if it is from abstract class
   * @see #getChild()
   */
  boolean isAbstract();

  // ----------------- PropertyValue

  /**
   * Add PropertyValue to list.
   *
   * @param name supports property-path like 'user.name'
   * @since 3.0
   */
  void addPropertyValue(String name, Object value);

  /** @since 4.0 */
  void addPropertyValues(PropertyValue... propertyValues);

  /** @since 4.0 */
  void addPropertyValues(Map<String, Object> propertyValues);

  /**
   * Apply bean' {@link PropertySetter}s
   *
   * @param propertyValues The array of the bean's {@link PropertySetter}s
   */
  void setPropertyValues(PropertyValue... propertyValues);

  /** @since 4.0 */
  void setPropertyValues(Map<String, Object> propertyValues);

  void setPropertyValues(Collection<PropertyValue> propertyValues);

  /**
   * get simple properties
   *
   * @since 4.0
   */
  @Nullable
  Map<String, Object> getPropertyValues();

  /**
   * Apply bean If its initialized
   *
   * @param initialized The state of bean
   */
  void setInitialized(boolean initialized);

  /**
   * Apply bean' name
   *
   * @param name The bean's name
   */
  void setName(String name);

  /**
   * Apply bean' scope
   *
   * @param scope The scope of the bean
   * @see Scope#PROTOTYPE
   * @see Scope#SINGLETON
   */
  void setScope(String scope);

  /**
   * Apply bean' initialize {@link Method}s
   *
   * @param initMethods The array of the bean's initialize {@link Method}s
   */
  void setInitMethods(String... initMethods);

  /**
   * Apply bean' destroy {@link Method}s
   *
   * @param destroyMethod The array of the bean's destroy {@link Method}s
   */
  void setDestroyMethod(String destroyMethod);

  /**
   * Indicates that If the bean is a {@link FactoryBean}.
   *
   * @param factoryBean If its a {@link FactoryBean}
   */
  void setFactoryBean(boolean factoryBean);

  /**
   * Indicates that the abstract bean's child implementation
   *
   * @return Child implementation bean, returns {@code null} indicates that this
   * {@link BeanDefinition} is not abstract
   * @since 2.1.7
   */
  @Nullable
  BeanDefinition getChild();

  /**
   * new bean instance
   *
   * @param factory input bean factory
   * @return new bean instance
   * @since 3.0
   */
  @Deprecated
  Object newInstance(BeanFactory factory);

  /**
   * new bean instance
   *
   * @param factory input bean factory
   * @param args arguments to use when creating a corresponding instance
   * @return new bean instance
   * @since 3.0
   */
  @Deprecated
  Object newInstance(BeanFactory factory, Object... args);

  /**
   * Set whether this bean should be lazily initialized.
   * <p>If {@code false}, the bean will get instantiated on startup by bean
   * factories that perform eager initialization of singletons.
   *
   * @since 3.0
   */
  void setLazyInit(boolean lazyInit);

  /**
   * Return whether this bean should be lazily initialized, i.e. not
   * eagerly instantiated on startup. Only applicable to a singleton bean.
   *
   * @since 3.0
   */
  boolean isLazyInit();

  /**
   * @since 3.0
   */
  void copyFrom(BeanDefinition newDef);

  /**
   * Set a bean instance supplier
   *
   * @param supplier bean instance supplier (can be null)
   * @param <T> target bean type
   * @since 4.0
   */
  <T> void setSupplier(Supplier<T> supplier);

  /**
   * Validate bean definition
   *
   * @throws BeanDefinitionValidationException invalid {@link BeanDefinition}
   * @since 4.0
   */
  default void validate() throws BeanDefinitionValidationException {
    if (StringUtils.isEmpty(getName())) {
      throw new BeanDefinitionValidationException("Definition's bean name can't be null");
    }
  }

  /**
   * Set whether this bean dClaefinition is 'synthetic', that is, not defined
   * by the application itself (for example, an infrastructure bean such
   * as a helper for auto-proxying, created through {@code <aop:config>}).
   *
   * @since 4.0
   */
  void setSynthetic(boolean synthetic);

  /**
   * Return whether this bean definition is 'synthetic', that is,
   * not defined by the application itself.
   *
   * @since 4.0
   */
  boolean isSynthetic();

  /**
   * Set the role hint for this {@code BeanDefinition}. The role hint
   * provides the frameworks as well as tools with an indication of
   * the role and importance of a particular {@code BeanDefinition}.
   *
   * @see #ROLE_APPLICATION
   * @see #ROLE_INFRASTRUCTURE
   * @since 4.0
   */
  void setRole(int role);

  /**
   * Get the role hint for this {@code BeanDefinition}. The role hint
   * provides the frameworks as well as tools with an indication of
   * the role and importance of a particular {@code BeanDefinition}.
   *
   * @see #ROLE_APPLICATION
   * @see #ROLE_INFRASTRUCTURE
   * @since 4.0
   */
  int getRole();

  /**
   * Set whether this bean is a primary autowire candidate.
   * <p>If this value is {@code true} for exactly one bean among multiple
   * matching candidates, it will serve as a tie-breaker.
   *
   * @since 4.0
   */
  void setPrimary(boolean primary);

  /**
   * Return whether this bean is a primary autowire candidate.
   *
   * @since 4.0
   */
  boolean isPrimary();

  /**
   * check type
   *
   * @see #hasBeanClass()
   * @since 4.0
   */
  boolean isAssignableTo(ResolvableType typeToMatch);

  /**
   * check type
   *
   * @see #hasBeanClass()
   * @since 4.0
   */
  boolean isAssignableTo(Class<?> typeToMatch);

  /**
   * Specify the bean class name of this bean definition.
   * <p>The class name can be modified during bean factory post-processing,
   * typically replacing the original class name with a parsed variant of it.
   *
   * @since 4.0
   */
  void setBeanClassName(@Nullable String beanClassName);

  /**
   * Return the current bean class name of this bean definition.
   * <p>Note that this does not have to be the actual class name used at runtime, in
   * case of a child definition overriding/inheriting the class name from its parent.
   * Also, this may just be the class that a factory method is called on, or it may
   * even be empty in case of a factory bean reference that a method is called on.
   * Hence, do <i>not</i> consider this to be the definitive bean type at runtime but
   * rather only use it for parsing purposes at the individual bean definition level.
   *
   * @since 4.0
   */
  @Nullable
  String getBeanClassName();

  /** @since 4.0 */
  boolean hasBeanClass();

  /** @since 4.0 */
  BeanDefinition cloneDefinition();

}
