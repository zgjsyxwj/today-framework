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

package cn.taketoday.web.handler.method;

import cn.taketoday.beans.factory.BeanSupplier;
import cn.taketoday.lang.Nullable;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/1/13 20:09
 */
class SuppliedActionMappingAnnotationHandler extends ActionMappingAnnotationHandler {
  private final BeanSupplier<Object> beanSupplier;

  SuppliedActionMappingAnnotationHandler(
          BeanSupplier<Object> beanSupplier, HandlerMethod handlerMethod, @Nullable ResolvableMethodParameter[] parameters) {
    super(handlerMethod, parameters);
    this.beanSupplier = beanSupplier;
  }

  @Override
  protected Object getHandlerBean() {
    return beanSupplier.get();
  }

}

