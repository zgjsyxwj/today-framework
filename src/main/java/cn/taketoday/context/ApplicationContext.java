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

import java.io.Closeable;

import cn.taketoday.beans.factory.AutowireCapableBeanFactory;
import cn.taketoday.beans.factory.BeanFactory;
import cn.taketoday.beans.factory.HierarchicalBeanFactory;
import cn.taketoday.context.event.ApplicationEventPublisher;
import cn.taketoday.context.expression.ExpressionEvaluator;
import cn.taketoday.core.env.Environment;
import cn.taketoday.core.env.EnvironmentCapable;
import cn.taketoday.core.io.PatternResourceLoader;
import cn.taketoday.lang.NonNull;
import cn.taketoday.lang.Nullable;

/**
 * @author TODAY <br>
 * 2018-06-23 16:39:36
 */
public interface ApplicationContext
        extends Closeable, HierarchicalBeanFactory, MessageSource,
                ApplicationEventPublisher, PatternResourceLoader, EnvironmentCapable {

  /**
   * @see Environment
   * @since 4.0
   */
  String APPLICATION_NAME = "application.name";

  /**
   * Get {@link Environment}
   *
   * @return {@link Environment}
   */
  @Override
  Environment getEnvironment();

  /**
   * Get AbstractBeanFactory
   *
   * @return A bean factory
   * @since 3.0
   */
  BeanFactory getBeanFactory();

  /**
   * unwrap bean-factory to {@code requiredType}
   *
   * @throws IllegalArgumentException not a requiredType
   * @see #getBeanFactory()
   * @since 4.0
   */
  @NonNull
  <T> T unwrapFactory(Class<T> requiredType);

  /**
   * unwrap this ApplicationContext to {@code requiredType}
   *
   * @throws IllegalArgumentException not a requiredType
   * @since 4.0
   */
  @NonNull
  <T> T unwrap(Class<T> requiredType);

  /**
   * Close context and destroy all singletons
   */
  @Override
  void close();

  /**
   * Context has started
   *
   * @return is started
   */
  boolean hasStarted();

  /**
   * Get the context startup time stamp
   *
   * @return startup timestamp
   */
  long getStartupDate();

  /**
   * Get context's state
   *
   * @return context's state
   * @since 2.1.5
   */
  State getState();

  /**
   * Return the unique id of this application context.
   *
   * @return the unique id of the context, or {@code null} if none
   * @since 4.0
   */
  @Nullable
  String getId();

  /**
   * Return a name for the deployed application that this context belongs to.
   *
   * @return a name for the deployed application, or the empty String by default
   * @since 4.0
   */
  String getApplicationName();

  /**
   * Return the parent context, or {@code null} if there is no parent
   * and this is the root of the context hierarchy.
   *
   * @return the parent context, or {@code null} if there is no parent
   * @since 4.0
   */
  @Nullable
  ApplicationContext getParent();

  /**
   * Expose AutowireCapableBeanFactory functionality for this context.
   * <p>This is not typically used by application code, except for the purpose of
   * initializing bean instances that live outside of the application context,
   * applying the bean lifecycle (fully or partly) to them.
   * <p>Alternatively, the internal BeanFactory exposed by the
   * {@link ConfigurableApplicationContext} interface offers access to the
   * {@link AutowireCapableBeanFactory} interface too. The present method mainly
   * serves as a convenient, specific facility on the ApplicationContext interface.
   * <p><b>NOTE: this method will consistently throw IllegalStateException
   * after the application context has been closed.</b> In current Framework
   * versions, only refreshable application contexts behave that way;
   * all application context implementations will be required to comply.
   *
   * @return the AutowireCapableBeanFactory for this context
   * @throws IllegalStateException if the context does not support the
   * {@link AutowireCapableBeanFactory} interface, or does not hold an
   * autowire-capable bean factory yet (e.g. if {@code refresh()} has
   * never been called), or if the context has been closed already
   * @see ConfigurableApplicationContext#refresh()
   * @see ConfigurableApplicationContext#getBeanFactory()
   */
  AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

  /**
   * @return ExpressionEvaluator of this context
   * @since 4.0
   */
  ExpressionEvaluator getExpressionEvaluator();

  enum State {

    /** context instantiated */
    NONE,
    /** context is loading */
    STARTING,
    /** context is started */
    STARTED,
    /** context failed to start */
    FAILED,
    /** context is closing */
    CLOSING,
    /** context is closed */
    CLOSED;
  }

}
