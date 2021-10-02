/*
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2020 All Rights Reserved.
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
package cn.taketoday.orm.hibernate5;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import javax.persistence.Entity;
import javax.sql.DataSource;

import cn.taketoday.beans.factory.BeanDefinition;
import cn.taketoday.beans.factory.BeanDefinitionRegistry;
import cn.taketoday.beans.factory.SingletonBeanRegistry;
import cn.taketoday.context.ApplicationContext;
import cn.taketoday.context.ConfigurableApplicationContext;
import cn.taketoday.context.ContextUtils;
import cn.taketoday.context.DefaultProps;
import cn.taketoday.context.annotation.AnnotatedBeanDefinitionReader;
import cn.taketoday.context.event.ApplicationContextEvent;
import cn.taketoday.context.event.ApplicationEventCapable;
import cn.taketoday.context.event.ApplicationListener;
import cn.taketoday.context.event.ContextRefreshEvent;
import cn.taketoday.context.event.LoadingMissingBeanEvent;
import cn.taketoday.core.ConfigurationException;
import cn.taketoday.logger.Logger;
import cn.taketoday.logger.LoggerFactory;
import cn.taketoday.util.ClassUtils;

/**
 * @author TODAY 2019-11-05 22:11
 */
public class HibernateConfiguration extends Configuration
        implements ApplicationListener<ApplicationContextEvent>, ApplicationEventCapable {
  // @since 4.0
  private final Logger log = LoggerFactory.getLogger(getClass());

  // @since 4.0
  private AnnotatedBeanDefinitionReader beanDefinitionReader;

  public static final String SESSION_FACTORY_BEAN_NAME = "org.hibernate.SessionFactory";

  public SessionFactory buildSessionFactory(DataSource dataSource, Properties hibernateProperties) {

    hibernateProperties.put(AvailableSettings.DATASOURCE, dataSource);
    hibernateProperties.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, HibernateSessionContext.class.getName());
    hibernateProperties.put(AvailableSettings.CLASSLOADERS, Collections.singleton(ClassUtils.getDefaultClassLoader()));

    setProperties(hibernateProperties);

    return super.buildSessionFactory();
  }

  @Override
  public void onApplicationEvent(ApplicationContextEvent event) {

    if (event instanceof ContextRefreshEvent) {
      // TODO 修复懒加载模式下错误
      refreshSessionFactory(event.getSource());
    }
    else if (event instanceof LoadingMissingBeanEvent) {
      registerSessionFactoryBean(((LoadingMissingBeanEvent) event).getCandidates(), event.getSource());
    }
  }

  protected void refreshSessionFactory(final ApplicationContext applicationContext) {
    SingletonBeanRegistry beanRegistry = applicationContext.unwrapFactory(SingletonBeanRegistry.class);
    if (beanRegistry.getSingleton(SESSION_FACTORY_BEAN_NAME) == null) {
      final DataSource dataSource = applicationContext.getBean(DataSource.class);
      if (dataSource == null) {
        throw new ConfigurationException("You must provide a javax.sql.DataSource bean");
      }
      final Properties properties = ContextUtils.loadProps(
              new DefaultProps().setPrefix("hibernate."),
              applicationContext.getEnvironment().getProperties());

      beanRegistry.registerSingleton(SESSION_FACTORY_BEAN_NAME, buildSessionFactory(dataSource, properties));
      log.info("Refresh 'SessionFactory' bean");
    }
  }

  protected void registerSessionFactoryBean(
          Collection<Class<?>> candidates, ApplicationContext context) {
    for (Class<?> entityClass : candidates) {
      if (entityClass.isAnnotationPresent(Entity.class)) {
        addAnnotatedClass(entityClass);
      }
    }

    // @since 4.0
    if (beanDefinitionReader == null) {
      BeanDefinitionRegistry registry = context.unwrapFactory(BeanDefinitionRegistry.class);
      beanDefinitionReader = new AnnotatedBeanDefinitionReader(
              context.unwrap(ConfigurableApplicationContext.class), registry);
    }

    BeanDefinition registered = beanDefinitionReader.registerBean(
            SESSION_FACTORY_BEAN_NAME, SessionFactory.class);

    registered.setDestroyMethods("close");
    log.info("Register 'SessionFactory' bean definition {}", registered);
  }

  @Override
  public Class<?>[] getApplicationEvent() {
    return new Class<?>[] { LoadingMissingBeanEvent.class, ContextRefreshEvent.class };
  }

}
