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

import java.util.UUID;

import cn.taketoday.core.conversion.Converter;
import cn.taketoday.util.StringUtils;

/**
 * Converts from a String to a {@link UUID}.
 *
 * @author Phillip Webb
 * @author TODAY
 * @see UUID#fromString
 * @since 3.0
 */
final class StringToUUIDConverter implements Converter<String, UUID> {

  @Override
  public UUID convert(String source) {
    return (StringUtils.hasText(source) ? UUID.fromString(source.trim()) : null);
  }

}
