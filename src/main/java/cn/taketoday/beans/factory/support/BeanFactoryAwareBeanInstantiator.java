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

import java.lang.reflect.Constructor;

import cn.taketoday.beans.BeanInstantiationException;
import cn.taketoday.beans.factory.BeanDefinitionRegistry;
import cn.taketoday.beans.factory.BeanFactory;
import cn.taketoday.beans.factory.SingletonBeanRegistry;
import cn.taketoday.beans.factory.dependency.DependencyInjector;
import cn.taketoday.beans.support.BeanInstantiator;
import cn.taketoday.beans.support.BeanInstantiatorFactory;
import cn.taketoday.beans.support.ReflectiveInstantiatorFactory;
import cn.taketoday.core.ConstructorNotFoundException;
import cn.taketoday.lang.Assert;
import cn.taketoday.lang.Nullable;

/**
 * provide Bean Constructor Arguments resolving
 *
 * @author TODAY 2021/10/4 22:26
 * @since 4.0
 */
public class BeanFactoryAwareBeanInstantiator {
  public static final String BEAN_NAME = "beanFactoryAwareBeanInstantiator";

  private final BeanFactory beanFactory;
  private final DependencyInjector dependencyInjector;
  private BeanInstantiatorFactory instantiatorFactory = ReflectiveInstantiatorFactory.INSTANCE;

  public BeanFactoryAwareBeanInstantiator(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
    this.dependencyInjector = beanFactory.getInjector();
  }

  public <T> T instantiate(Class<T> beanClass) {
    return instantiate(beanClass, null);
  }

  /**
   * use obtainConstructor to get {@link Constructor} to create bean instance.
   *
   * @param beanClass target bean class
   * @param providedArgs User provided arguments
   * @return bean class 's instance
   * @throws BeanInstantiationException if any reflective operation exception occurred
   * @throws ConstructorNotFoundException If beanClass has no suitable constructor
   * @see BeanUtils#obtainConstructor(Class)
   * @since 4.0
   */
  @SuppressWarnings("unchecked")
  public <T> T instantiate(Class<T> beanClass, @Nullable Object[] providedArgs) {
    Constructor<T> constructor = BeanUtils.obtainConstructor(beanClass);
    if (constructor.getParameterCount() == 0) {
      return (T) instantiatorFactory.newInstantiator(constructor).instantiate();
    }
    Object[] args = dependencyInjector.resolveArguments(constructor, providedArgs);
    BeanInstantiator beanInstantiator = instantiatorFactory.newInstantiator(constructor);
    return (T) beanInstantiator.instantiate(args);
  }

  @SuppressWarnings("unchecked")
  public <T> T instantiate(
          Class<T> beanClass,
          DependencyInjector injector,
          BeanInstantiatorFactory instantiatorFactory, @Nullable Object[] providedArgs) {
    Constructor<T> constructor = BeanUtils.obtainConstructor(beanClass);
    Object[] args = injector.resolveArguments(constructor, providedArgs);

    BeanInstantiator beanInstantiator = instantiatorFactory.newInstantiator(constructor);
    return (T) beanInstantiator.instantiate(args);
  }

  public void setInstantiatorFactory(@Nullable BeanInstantiatorFactory instantiatorFactory) {
    if (instantiatorFactory == null) {
      instantiatorFactory = ReflectiveInstantiatorFactory.INSTANCE;
    }
    this.instantiatorFactory = instantiatorFactory;
  }

  @Nullable
  public BeanInstantiatorFactory getInstantiatorFactory() {
    return instantiatorFactory;
  }

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  // static factory-method

  public static BeanFactoryAwareBeanInstantiator from(BeanFactory beanFactory) {
    Assert.notNull(beanFactory, "beanFactory is required");
    BeanFactoryAwareBeanInstantiator instantiator = beanFactory.getBean(
            BEAN_NAME, BeanFactoryAwareBeanInstantiator.class);
    if (instantiator == null) {
      instantiator = new BeanFactoryAwareBeanInstantiator(beanFactory);
      if (beanFactory instanceof SingletonBeanRegistry singletonBeanRegistry) {
        singletonBeanRegistry.registerSingleton(BEAN_NAME, instantiator);
      }
      else if (beanFactory instanceof BeanDefinitionRegistry registry) {
        BeanDefinition definition = new BeanDefinition(BEAN_NAME, BeanFactoryAwareBeanInstantiator.class);
        registry.registerBeanDefinition(BEAN_NAME, definition);
        BeanFactoryAwareBeanInstantiator finalInstantiator = instantiator;
        definition.setInstanceSupplier(() -> finalInstantiator);
      }
    }
    return instantiator;
  }

}
