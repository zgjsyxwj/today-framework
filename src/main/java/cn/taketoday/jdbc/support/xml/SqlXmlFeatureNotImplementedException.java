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

package cn.taketoday.jdbc.support.xml;

import cn.taketoday.dao.InvalidDataAccessApiUsageException;

/**
 * Exception thrown when the underlying implementation does not support the
 * requested feature of the API.
 *
 * @author Thomas Risberg
 * @since 4.0
 */
@SuppressWarnings("serial")
public class SqlXmlFeatureNotImplementedException extends InvalidDataAccessApiUsageException {

  /**
   * Constructor for SqlXmlFeatureNotImplementedException.
   *
   * @param msg the detail message
   */
  public SqlXmlFeatureNotImplementedException(String msg) {
    super(msg);
  }

  /**
   * Constructor for SqlXmlFeatureNotImplementedException.
   *
   * @param msg the detail message
   * @param cause the root cause from the data access API in use
   */
  public SqlXmlFeatureNotImplementedException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
