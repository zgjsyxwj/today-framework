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
package cn.taketoday.context.el;

import cn.taketoday.context.StandardApplicationContext;
import cn.taketoday.context.expression.ExpressionEvaluator;
import cn.taketoday.lang.Value;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * @author TODAY <br>
 * 2019-02-23 20:58
 */
@ToString
class ELFieldTests {

  @Value("#{235.1}")
  private double testDouble;

  @Value("#{235.1}")
  private float testFloat;

  @Value(required = false, value = "#{user}")
  private User user;

  @Value(value = "#{env.getProperty('site.name')}")
  private String siteName;

  @Getter
  @Setter
  @ToString
  static class User {
    Integer id;
    String sex;
    Integer age;
    String passwd;
    Date brithday;
    String userId;
    String userName;
  }

  @Test
  void number() {
    try (StandardApplicationContext applicationContext = new StandardApplicationContext()) {

      applicationContext.register(ELFieldTests.class);
      applicationContext.refresh();
      ELFieldTests bean = applicationContext.getBean(getClass());
      System.err.println(bean.testFloat);
      System.err.println(bean.testDouble);

      assert bean.testFloat == 235.1f;
      assert bean.testDouble == 235.1;
    }
  }

  @Test
  void testEnv() {
    try (StandardApplicationContext applicationContext = new StandardApplicationContext()) {

      User user = new User();
      user.setAge(20)//
              .setBrithday(new Date())//
              .setId(1);

      ExpressionEvaluator expressionEvaluator = applicationContext.getExpressionEvaluator();
      expressionEvaluator.obtainProcessor().defineBean("user", user);
      applicationContext.setPropertiesLocation("info.properties");

      applicationContext.register(ELFieldTests.class);
      applicationContext.refresh();

      ELFieldTests bean = applicationContext.getBean(getClass());
      System.err.println(bean);
      assert bean.user == user;
      assert bean.siteName.equals("TODAY BLOG");
    }
  }

  @Test
  void defineBean() {
    try (StandardApplicationContext applicationContext = new StandardApplicationContext()) {

      User user = new User();
      user.setAge(20)//
              .setBrithday(new Date())//
              .setId(1);

      ExpressionEvaluator expressionEvaluator = applicationContext.getExpressionEvaluator();
      expressionEvaluator.obtainProcessor().defineBean("user", user);

      applicationContext.register(ELFieldTests.class);
      applicationContext.refresh();

      ELFieldTests bean = applicationContext.getBean(getClass());
      System.err.println(bean);
      assert bean.user == user;
    }
  }

}
