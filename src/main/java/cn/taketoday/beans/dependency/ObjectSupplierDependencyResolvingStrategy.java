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

package cn.taketoday.beans.dependency;

import java.util.function.Supplier;

import cn.taketoday.beans.DependencyResolvingFailedException;
import cn.taketoday.beans.factory.BeanFactory;
import cn.taketoday.beans.factory.ObjectSupplier;
import cn.taketoday.core.ResolvableType;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang 2021/11/18 20:44</a>
 * @since 4.0
 */
public class ObjectSupplierDependencyResolvingStrategy implements DependencyResolvingStrategy {

  @Override
  public void resolveDependency(
          DependencyInjectionPoint injectionPoint, DependencyResolvingContext context) {

    BeanFactory beanFactory = context.getBeanFactory();
    if (beanFactory != null && !context.hasDependency()) {
      Class<?> dependencyType = injectionPoint.getDependencyType();
      if (dependencyType == ObjectSupplier.class || dependencyType == Supplier.class) {
        ResolvableType parameterType = injectionPoint.getResolvableType();
        if (parameterType.hasGenerics()) {
          ResolvableType generic = parameterType.as(Supplier.class).getGeneric(0);
          if (generic == ResolvableType.NONE) {
            throw new DependencyResolvingFailedException(
                    "cannot determine a exactly bean type from injection-point: " + injectionPoint);
          }
          ObjectSupplier<Object> objectSupplier = beanFactory.getObjectSupplier(generic);
          context.setDependency(objectSupplier); // terminate
          context.setTerminate(true);
        }
      }
    }

  }

}
