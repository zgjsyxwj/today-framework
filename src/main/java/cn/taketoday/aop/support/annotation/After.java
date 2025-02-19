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
package cn.taketoday.aop.support.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.taketoday.core.annotation.AliasFor;

/**
 * @author TODAY <br>
 * 2018-08-09 18:56
 */
@Retention(RetentionPolicy.RUNTIME)
@Advice(interceptor = AfterMethodInterceptor.class)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface After {

  int DEFAULT_ORDER = 3;

  /** Annotated with */
  @AliasFor(annotation = Advice.class)
  Class<? extends Annotation>[] value() default {};

  /** Package name */
  @AliasFor(annotation = Advice.class)
  String[] pointcut() default {};

  /** Target classes */
  @AliasFor(annotation = Advice.class)
  Class<?>[] target() default {};

  /** Method in class */
  @AliasFor(annotation = Advice.class)
  String[] method() default {};

}
