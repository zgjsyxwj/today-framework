/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
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
package cn.taketoday.aop.support.annotation;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

import cn.taketoday.context.factory.BeanDefinition;

/**
 * @author TODAY 2018-10-13 11:25
 * @see cn.taketoday.aop.annotation.AfterThrowing
 */
public class AfterThrowingMethodInterceptor extends AbstractAnnotationMethodInterceptor {
  public static final int DEFAULT_ORDER = 5;

  public AfterThrowingMethodInterceptor(Method method, BeanDefinition aspectDef) {
    super(method, aspectDef);
    setOrder(DEFAULT_ORDER);
  }

  @Override
  public Object invoke(final MethodInvocation inv) throws Throwable {
    try {
      return inv.proceed();
    }
    catch (Throwable ex) {
      return invokeAdviceMethod(inv, null, ex); // fix Use
    }
  }

}
