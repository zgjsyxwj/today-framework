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

import java.lang.reflect.Method;
import java.util.Map;

import cn.taketoday.core.annotation.AnnotatedElementUtils;
import cn.taketoday.core.annotation.AnnotationAttributes;
import cn.taketoday.lang.Component;
import cn.taketoday.util.ConcurrentReferenceHashMap;

/**
 * Utilities for processing {@link Component}-annotated methods.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 4.0
 */
abstract class BeanAnnotationHelper {

  private static final Map<Method, String> beanNameCache = new ConcurrentReferenceHashMap<>();

  public static boolean isBeanAnnotated(Method method) {
    return AnnotatedElementUtils.hasAnnotation(method, Component.class);
  }

  public static String determineBeanNameFor(Method beanMethod) {
    String beanName = beanNameCache.get(beanMethod);
    if (beanName == null) {
      // By default, the bean name is the name of the @Component-annotated method
      beanName = beanMethod.getName();
      // Check to see if the user has explicitly set a custom bean name...
      AnnotationAttributes bean =
              AnnotatedElementUtils.findMergedAnnotationAttributes(beanMethod, Component.class, false, false);
      if (bean != null) {
        String[] names = bean.getStringArray("name");
        if (names.length > 0) {
          beanName = names[0];
        }
      }
      beanNameCache.put(beanMethod, beanName);
    }
    return beanName;
  }

}
