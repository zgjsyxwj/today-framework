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

package cn.taketoday.context.annotation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.taketoday.beans.factory.BeanDefinitionRegistry;
import cn.taketoday.beans.factory.support.AnnotatedBeanDefinition;
import cn.taketoday.beans.factory.support.DefaultBeanDefinitionRegistry;
import cn.taketoday.lang.Component;
import cn.taketoday.lang.Service;
import cn.taketoday.util.StringUtils;
import cn.taketoday.web.annotation.Controller;
import example.scannable.CustomStereotype;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2021/12/8 14:13
 */
class AnnotationBeanNamePopulatorTests {

  private AnnotationBeanNamePopulator beanNamePopulator = new AnnotationBeanNamePopulator();

  @Test
  public void generateBeanNameWithNamedComponent() {
    BeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
    AnnotatedBeanDefinition bd = new AnnotatedBeanDefinition(ComponentWithName.class);
    String beanName = this.beanNamePopulator.populateName(bd, registry);
    assertThat(beanName).as("The generated beanName must *never* be null.").isNotNull();
    assertThat(StringUtils.hasText(beanName)).as("The generated beanName must *never* be blank.").isTrue();
    assertThat(beanName).isEqualTo("walden");
  }

  @Test
  public void generateBeanNameWithDefaultNamedComponent() {
    BeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
    AnnotatedBeanDefinition bd = new AnnotatedBeanDefinition(DefaultNamedComponent.class);
    String beanName = this.beanNamePopulator.populateName(bd, registry);
    assertThat(beanName).as("The generated beanName must *never* be null.").isNotNull();
    assertThat(StringUtils.hasText(beanName)).as("The generated beanName must *never* be blank.").isTrue();
    assertThat(beanName).isEqualTo("thoreau");
  }

  @Test
  public void generateBeanNameWithNamedComponentWhereTheNameIsBlank() {
    BeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
    AnnotatedBeanDefinition bd = new AnnotatedBeanDefinition(ComponentWithBlankName.class);
    String beanName = this.beanNamePopulator.populateName(bd, registry);
    assertThat(beanName).as("The generated beanName must *never* be null.").isNotNull();
    assertThat(StringUtils.hasText(beanName)).as("The generated beanName must *never* be blank.").isTrue();
    String expectedGeneratedBeanName = this.beanNamePopulator.buildDefaultBeanName(bd);
    assertThat(beanName).isEqualTo(expectedGeneratedBeanName);
  }

  @Test
  public void generateBeanNameWithAnonymousComponentYieldsGeneratedBeanName() {
    BeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
    AnnotatedBeanDefinition bd = new AnnotatedBeanDefinition(AnonymousComponent.class);
    String beanName = this.beanNamePopulator.populateName(bd, registry);
    assertThat(beanName).as("The generated beanName must *never* be null.").isNotNull();
    assertThat(StringUtils.hasText(beanName)).as("The generated beanName must *never* be blank.").isTrue();
    String expectedGeneratedBeanName = this.beanNamePopulator.buildDefaultBeanName(bd);
    assertThat(beanName).isEqualTo(expectedGeneratedBeanName);
  }

  @Test
  public void generateBeanNameFromMetaComponentWithStringValue() {
    BeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
    AnnotatedBeanDefinition bd = new AnnotatedBeanDefinition(ComponentFromStringMeta.class);
    String beanName = this.beanNamePopulator.populateName(bd, registry);
    assertThat(beanName).isEqualTo("henry");
  }

  @Test
  public void generateBeanNameFromMetaComponentWithNonStringValue() {
    BeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
    AnnotatedBeanDefinition bd = new AnnotatedBeanDefinition(ComponentFromNonStringMeta.class);
    String beanName = this.beanNamePopulator.populateName(bd, registry);
    assertThat(beanName).isEqualTo("componentFromNonStringMeta");
  }

  @Test
  public void generateBeanNameFromComposedControllerAnnotationWithoutName() {
    // SPR-11360
    BeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
    AnnotatedBeanDefinition bd = new AnnotatedBeanDefinition(ComposedControllerAnnotationWithoutName.class);
    String beanName = this.beanNamePopulator.populateName(bd, registry);
    String expectedGeneratedBeanName = this.beanNamePopulator.buildDefaultBeanName(bd);
    assertThat(beanName).isEqualTo(expectedGeneratedBeanName);
  }

  @Test
  public void generateBeanNameFromComposedControllerAnnotationWithBlankName() {
    // SPR-11360
    BeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
    AnnotatedBeanDefinition bd = new AnnotatedBeanDefinition(ComposedControllerAnnotationWithBlankName.class);
    String beanName = this.beanNamePopulator.populateName(bd, registry);
    String expectedGeneratedBeanName = this.beanNamePopulator.buildDefaultBeanName(bd);
    assertThat(beanName).isEqualTo(expectedGeneratedBeanName);
  }

  @Test
  public void generateBeanNameFromComposedControllerAnnotationWithStringValue() {
    // SPR-11360
    BeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
    AnnotatedBeanDefinition bd = new AnnotatedBeanDefinition(ComposedControllerAnnotationWithStringValue.class);
    String beanName = this.beanNamePopulator.populateName(bd, registry);
    assertThat(beanName).isEqualTo("restController");
  }

  @Component("walden")
  private static class ComponentWithName {
  }

  @Component(" ")
  private static class ComponentWithBlankName {
  }

  @Component
  private static class AnonymousComponent {
  }

  @Service("henry")
  private static class ComponentFromStringMeta {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Component
  public @interface NonStringMetaComponent {

    long value();
  }

  @NonStringMetaComponent(123)
  private static class ComponentFromNonStringMeta {
  }

  /**
   *
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Controller
  public static @interface TestRestController {

    String value() default "";
  }

  @TestRestController
  public static class ComposedControllerAnnotationWithoutName {
  }

  @TestRestController(" ")
  public static class ComposedControllerAnnotationWithBlankName {
  }

  @TestRestController("restController")
  public static class ComposedControllerAnnotationWithStringValue {
  }

  @CustomStereotype
  static class DefaultNamedComponent {

  }

}
