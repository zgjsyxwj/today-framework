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

package cn.taketoday.context.autowire;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.function.Supplier;

import cn.taketoday.beans.ArgumentsResolvingContext;
import cn.taketoday.beans.factory.ObjectSupplier;
import cn.taketoday.beans.support.BeanUtils;
import cn.taketoday.context.StandardApplicationContext;
import cn.taketoday.beans.factory.annotation.Autowired;
import lombok.ToString;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author TODAY 2021/3/6 13:24
 */
class ObjectSupplierTests {

  @ToString
  static class Bean {

  }

  @ToString
  static class TEST {

    @Autowired
    ObjectSupplier<Bean> beanObjectSupplier;

    TEST(ObjectSupplier<Bean> beanObjectSupplier, Supplier<Bean> supplier) {

    }

  }

  @ToString
  static class ResolvableTypeTEST {

    @Autowired
    ObjectSupplier<List<Bean>> beanObjectSupplier;

    ResolvableTypeTEST(
            ObjectSupplier<List<Bean>> beanObjectSupplier, Supplier<List<Bean>> supplier) {

    }

  }

  @Test
  public void testProperty() throws Throwable {
    try (StandardApplicationContext context = new StandardApplicationContext()) {
      context.register(Bean.class, TEST.class);
      context.refresh();

      TEST test = context.getBean(TEST.class);
      Bean bean = context.getBean(Bean.class);

      assertThat(test.beanObjectSupplier.get())
              .isEqualTo(bean);

    }
  }

  @Test
  @Disabled
  public void parameter() {
    Constructor<TEST> constructor = BeanUtils.getConstructor(TEST.class);
    assertThat(constructor).isNotNull();
    Parameter[] parameters = constructor.getParameters();


    try (StandardApplicationContext context = new StandardApplicationContext()) {
      ArgumentsResolvingContext resolvingContext =
              new ArgumentsResolvingContext(constructor, context, null);

      context.register(Bean.class);
      context.refresh();
//      ObjectSupplier<?> supplier = parameterResolver.resolveArgument(parameters[0], resolvingContext);
//      ObjectSupplier<?> supplier1 = parameterResolver.resolveArgument(parameters[1], resolvingContext);
//      Bean bean = context.getBean(Bean.class);

//      assertThat(supplier).isNotNull();
//      assertThat(supplier1).isNotNull();
//
//      assertThat(bean).isEqualTo(supplier.get()).isNotNull();
//      assertThat(bean).isEqualTo(supplier1.get()).isNotNull();
//
//      assertThat(supplier1).isNotEqualTo(supplier);
//      assertThat(supplier1).isEqualTo(parameterResolver.resolveArgument(parameters[1], resolvingContext));

    }
  }

}
