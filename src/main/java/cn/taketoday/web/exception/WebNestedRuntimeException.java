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
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */
package cn.taketoday.web.exception;

import cn.taketoday.context.NestedRuntimeException;
import cn.taketoday.context.utils.ExceptionUtils;

/**
 * @author TODAY <br>
 * 2019-06-03 10:27
 */
public class WebNestedRuntimeException extends NestedRuntimeException {
  private static final long serialVersionUID = 1L;

  public WebNestedRuntimeException() {
    super();
  }

  public WebNestedRuntimeException(String message) {
    super(message);
  }

  public WebNestedRuntimeException(Throwable cause) {
    super(cause);
  }

  public WebNestedRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * use default message
   * <p>
   * {@link ExceptionUtils#buildMessage(String, Throwable)} build more detail message
   * </p>
   */
  @Override
  protected String buildMessage(String message) {
    return message;
  }
}
