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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import cn.taketoday.beans.BeanInstantiationException;
import cn.taketoday.beans.factory.support.ConfigurableBeanFactory;
import cn.taketoday.core.conversion.ConversionService;
import cn.taketoday.core.conversion.Converter;
import cn.taketoday.core.conversion.support.DefaultConversionService;
import cn.taketoday.lang.Assert;
import cn.taketoday.lang.Nullable;
import cn.taketoday.util.ClassUtils;

/**
 * Simple template superclass for {@link FactoryBean} implementations that
 * creates a singleton or a prototype object, depending on a flag.
 *
 * <p>
 * If the "singleton" flag is {@code true} (the default), this class will create
 * the object that it creates exactly once on initialization and subsequently
 * return said singleton instance on all calls to the {@link #getObject()} method.
 *
 * <p>
 * Else, this class will create a new instance every time the {@link #getObject()}
 * method is invoked. Subclasses are responsible for implementing the abstract
 * {@link #createBeanInstance()} template method to actually create the
 * object(s) to expose.
 *
 * @param <T> the bean type
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author TODAY <br>
 * 2020-02-21 10:38
 * @see #setSingleton
 * @see #createBeanInstance()
 * @since 2.1.7
 */
public abstract class AbstractFactoryBean<T>
        implements FactoryBean<T>, BeanClassLoaderAware, BeanFactoryAware, InitializingBean, DisposableBean {

  private BeanFactory beanFactory;
  private boolean singleton = true;
  private boolean initialized = false;
  private T singletonInstance;
  private T earlySingletonInstance;
  private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

  /**
   * Set if a singleton should be created, or a new object on each request
   * otherwise. Default is {@code true} (a singleton).
   */
  public void setSingleton(boolean singleton) {
    this.singleton = singleton;
  }

  @Override
  public boolean isSingleton() {
    return this.singleton;
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    this.beanClassLoader = classLoader;
  }

  public ClassLoader getBeanClassLoader() {
    return beanClassLoader;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  /**
   * Return the BeanFactory that this bean runs in.
   */
  public BeanFactory getBeanFactory() {
    return this.beanFactory;
  }

  /**
   * Eagerly create the singleton instance, if necessary.
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    if (isSingleton()) {
      this.initialized = true;
      this.earlySingletonInstance = null;
      this.singletonInstance = createBeanInstance();
    }
  }

  /**
   * Expose the singleton instance or create a new prototype instance.
   *
   * @see #createBeanInstance()
   * @see #getEarlySingletonInterfaces()
   */
  @Override
  public final T getObject() throws Exception {
    if (isSingleton()) {
      return (this.initialized ? this.singletonInstance : getEarlySingletonInstance());
    }
    return createBeanInstance();
  }

  /**
   * Determine an 'early singleton' instance, exposed in case of a circular
   * reference. Not called in a non-circular scenario.
   */
  @SuppressWarnings("unchecked")
  private T getEarlySingletonInstance() {
    Class<?>[] ifcs = getEarlySingletonInterfaces();
    if (ifcs == null) {
      throw new FactoryBeanNotInitializedException(
              getClass().getName() + " does not support circular references");
    }
    if (this.earlySingletonInstance == null) {
      this.earlySingletonInstance = (T) Proxy.newProxyInstance(this.beanClassLoader, ifcs, new EarlySingletonInvocationHandler());
    }
    return this.earlySingletonInstance;
  }

  /**
   * Destroy the singleton instance, if any.
   *
   * @see #destroyInstance(Object)
   */
  @Override
  public void destroy() throws Exception {
    if (isSingleton()) {
      destroyInstance(this.singletonInstance);
    }
  }

  /**
   * This abstract method declaration mirrors the method in the FactoryBean
   * interface, for a consistent offering of abstract template methods.
   */
  @Override
  public abstract Class<?> getObjectType();

  /**
   * Template method that subclasses must override to construct the object
   * returned by this factory.
   * <p>
   * Invoked on initialization of this FactoryBean in case of a singleton; else,
   * on each {@link #getObject()} call.
   *
   * @return the object returned by this factory
   * @see #getObject()
   */
  protected abstract T createBeanInstance() throws Exception;

  /**
   * Return an array of interfaces that a singleton object exposed by this
   * FactoryBean is supposed to implement, for use with an 'early singleton proxy'
   * that will be exposed in case of a circular reference.
   * <p>
   * The default implementation returns this FactoryBean's object type, provided
   * that it is an interface, or {@code null} otherwise. The latter indicates that
   * early singleton access is not supported by this FactoryBean. This will lead
   * to a FactoryBeanNotInitializedException getting thrown.
   *
   * @return the interfaces to use for 'early singletons', or {@code null} to
   * indicate a BeanInstantiationException
   * @see BeanInstantiationException
   */
  protected Class<?>[] getEarlySingletonInterfaces() {
    Class<?> type = getObjectType();
    return (type != null && type.isInterface() ? new Class<?>[] { type } : null);
  }

  /**
   * Callback for destroying a singleton instance. Subclasses may override this to
   * destroy the previously created instance.
   * <p>
   * The default implementation is empty.
   *
   * @param instance the singleton instance, as returned by
   * {@link #createBeanInstance()}
   * @throws Exception in case of shutdown errors
   * @see #createBeanInstance()
   */
  protected void destroyInstance(T instance) throws Exception { }

  /**
   * Obtain a bean ConversionService from the BeanFactory that this bean
   * runs in. This is typically a fresh instance for each call.
   * <p>Falls back to a DefaultConversionService when not running in a BeanFactory.
   *
   * @see ConfigurableBeanFactory#getConversionService()
   */
  protected ConversionService getConversionService() {
    BeanFactory beanFactory = getBeanFactory();
    if (beanFactory instanceof ConfigurableBeanFactory) {
      return ((ConfigurableBeanFactory) beanFactory).getConversionService();
    }
    else {
      return DefaultConversionService.getSharedInstance();
    }
  }

  /**
   * Convert the value to the required type (if necessary from a String).
   *
   * @param value the value to convert
   * @param requiredType the type we must convert to
   * (or {@code null} if not known, for example in case of a collection element)
   * @return the new value, possibly the result of type conversion
   * @throws cn.taketoday.core.conversion.ConversionException if type conversion failed
   * @see java.beans.PropertyEditor#setAsText(String)
   * @see java.beans.PropertyEditor#getValue()
   * @see ConversionService
   * @see Converter
   */
  @Nullable
  protected <E> E convertIfNecessary(@Nullable Object value, @Nullable Class<E> requiredType) {
    return convertIfNecessary(getConversionService(), value, requiredType);
  }

  @SuppressWarnings("unchecked")
  @Nullable
  protected <E> E convertIfNecessary(ConversionService conversionService, @Nullable Object value, @Nullable Class<E> requiredType) {
    if (value != null && requiredType != null && !requiredType.isInstance(value)) {
      return conversionService.convert(value, requiredType);
    }
    return (E) value;
  }

  /**
   * Reflective InvocationHandler for lazy access to the actual singleton object.
   */
  private class EarlySingletonInvocationHandler implements InvocationHandler {

    /**
     * Expose the singleton instance (for access through the 'early singleton'
     * proxy).
     *
     * @return the singleton instance that this FactoryBean holds
     * @throws IllegalStateException if the singleton instance is not initialized
     */
    private T getSingletonInstance() {
      Assert.state(initialized, "Singleton instance not initialized yet");
      return singletonInstance;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

      final String name = method.getName();
      if ("equals".equals(name)) {
        // Only consider equal when proxies are identical.
        return (proxy == args[0]);
      }
      else if ("hashCode".equals(name)) {
        // Use hashCode of reference proxy.
        return System.identityHashCode(proxy);
      }
      else if (!initialized && "toString".equals(name)) {
        return "Early singleton proxy for interfaces " + Arrays.toString(getEarlySingletonInterfaces());
      }
      try {
        return method.invoke(getSingletonInstance(), args);
      }
      catch (InvocationTargetException ex) {
        throw ex.getTargetException();
      }
    }
  }

}
