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

package cn.taketoday.web.view.freemarker;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Properties;

import cn.taketoday.beans.factory.support.BeanDefinition;
import cn.taketoday.beans.factory.support.StandardBeanFactory;
import cn.taketoday.core.io.ByteArrayResource;
import cn.taketoday.core.io.DefaultResourceLoader;
import cn.taketoday.core.io.FileBasedResource;
import cn.taketoday.core.io.Resource;
import cn.taketoday.core.io.ResourceLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/2/5 13:06
 */
class FreeMarkerConfigurationFactoryBeanTests {

  private final FreeMarkerConfigurationFactoryBean fcfb = new FreeMarkerConfigurationFactoryBean();

  @Test
  public void freeMarkerConfigurationFactoryBeanWithConfigLocation() throws Exception {
    fcfb.setConfigLocation(new FileBasedResource("myprops.properties"));
    Properties props = new Properties();
    props.setProperty("myprop", "/mydir");
    fcfb.setFreemarkerSettings(props);
    assertThatIOException().isThrownBy(fcfb::afterPropertiesSet);
  }

  @Test
  public void freeMarkerConfigurationFactoryBeanWithResourceLoaderPath() throws Exception {
    fcfb.setTemplateLoaderPath("file:/mydir");
    fcfb.afterPropertiesSet();
    Configuration cfg = fcfb.getObject();
    assertThat(cfg.getTemplateLoader()).isInstanceOf(ContextTemplateLoader.class);
  }

  @Test
  @SuppressWarnings("rawtypes")
  public void freeMarkerConfigurationFactoryBeanWithNonFileResourceLoaderPath() throws Exception {
    fcfb.setTemplateLoaderPath("file:/mydir");
    Properties settings = new Properties();
    settings.setProperty("localized_lookup", "false");
    fcfb.setFreemarkerSettings(settings);
    fcfb.setResourceLoader(new ResourceLoader() {
      @Override
      public Resource getResource(String location) {
        if (!("file:/mydir".equals(location) || "file:/mydir/test".equals(location))) {
          throw new IllegalArgumentException(location);
        }
        return new ByteArrayResource("test".getBytes(), "test");
      }

      @Override
      public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
      }
    });
    fcfb.afterPropertiesSet();
    assertThat(fcfb.getObject()).isInstanceOf(Configuration.class);
    Configuration fc = fcfb.getObject();
    Template ft = fc.getTemplate("test");
    assertThat(FreeMarkerTemplateUtils.processTemplateIntoString(ft, new HashMap())).isEqualTo("test");
  }

  @Test  // SPR-12448
  public void freeMarkerConfigurationAsBean() {
    StandardBeanFactory beanFactory = new StandardBeanFactory();
    BeanDefinition loaderDef = new BeanDefinition(ContextTemplateLoader.class);
    loaderDef.getConstructorArgumentValues().addGenericArgumentValue(new DefaultResourceLoader());
    loaderDef.getConstructorArgumentValues().addGenericArgumentValue("/freemarker");
    BeanDefinition configDef = new BeanDefinition(Configuration.class);
    configDef.propertyValues().add("templateLoader", loaderDef);
    beanFactory.registerBeanDefinition("freeMarkerConfig", configDef);
    assertThat(beanFactory.getBean(Configuration.class)).isNotNull();
  }

}
