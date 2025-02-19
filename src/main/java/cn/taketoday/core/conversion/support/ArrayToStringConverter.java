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
package cn.taketoday.core.conversion.support;

import java.util.Arrays;

import cn.taketoday.core.TypeDescriptor;
import cn.taketoday.core.conversion.ConversionService;
import cn.taketoday.util.ObjectUtils;

/**
 * Converts an array to a comma-delimited String.
 *
 * @author Keith Donald
 * @author TODAY
 * @since 3.0
 */
final class ArrayToStringConverter extends ArraySourceConverter {
  private final CollectionToStringConverter helperConverter;

  public ArrayToStringConverter(ConversionService conversionService) {
    this.helperConverter = new CollectionToStringConverter(conversionService);
  }

  @Override
  protected boolean supportsInternal(final TypeDescriptor targetType, Class<?> sourceType) {
    // Object[].class -> String.class
    return targetType.is(String.class);
  }

  @Override
  public Object convert(TypeDescriptor targetType, Object source) {
    return this.helperConverter.convertInternal(
            targetType, Arrays.asList(ObjectUtils.toObjectArray(source)));
  }

}
