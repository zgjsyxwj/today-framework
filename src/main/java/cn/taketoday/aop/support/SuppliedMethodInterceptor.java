/*
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
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.aop.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.function.Supplier;

import cn.taketoday.context.utils.Assert;

/**
 * @author TODAY 2021/3/6 15:55
 * @since 3.0
 */
public class SuppliedMethodInterceptor implements MethodInterceptor {
  final Supplier<MethodInterceptor> interceptorSupplier;

  public SuppliedMethodInterceptor(Supplier<MethodInterceptor> interceptorSupplier) {
    Assert.notNull(interceptorSupplier, "interceptorSupplier must not be null");
    this.interceptorSupplier = interceptorSupplier;
  }

  MethodInterceptor obtainInterceptor() {
    final MethodInterceptor ret = interceptorSupplier.get();
    Assert.state(ret != null, "No MethodInterceptor");
    return ret;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    return obtainInterceptor().invoke(invocation);
  }
}
