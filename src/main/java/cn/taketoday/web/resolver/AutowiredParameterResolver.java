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

package cn.taketoday.web.resolver;

import cn.taketoday.beans.factory.annotation.Autowired;
import cn.taketoday.beans.factory.support.DependencyDescriptor;
import cn.taketoday.beans.factory.support.DependencyInjector;
import cn.taketoday.lang.Nullable;
import cn.taketoday.web.RequestContext;
import cn.taketoday.web.WebApplicationContext;
import cn.taketoday.web.handler.method.ResolvableMethodParameter;

/**
 * @author TODAY 2021/4/2 23:21
 * @since 3.0
 */
public class AutowiredParameterResolver implements ParameterResolvingStrategy {
  private final DependencyInjector injector;

  public AutowiredParameterResolver(WebApplicationContext context) {
    this.injector = context.getInjector();
  }

  @Override
  public boolean supportsParameter(ResolvableMethodParameter resolvable) {
    return resolvable.hasParameterAnnotation(Autowired.class);
  }

  @Nullable
  @Override
  public Object resolveParameter(RequestContext context, ResolvableMethodParameter resolvable) throws Throwable {
    return injector.resolveValue(new DependencyDescriptor(resolvable.getParameter(), true));
  }

}
