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

package cn.taketoday.beans.support;

import cn.taketoday.core.reflect.Accessor;
import cn.taketoday.lang.Nullable;

/**
 * @author TODAY 2021/8/27 21:41
 */
public abstract class ConstructorAccessor
        extends BeanInstantiator implements Accessor {

  /**
   * Invoke {@link java.lang.reflect.Constructor} with given args
   *
   * @return returns Object
   * @since 4.0
   */
  @Override
  public abstract Object doInstantiate(@Nullable Object[] args);
}
