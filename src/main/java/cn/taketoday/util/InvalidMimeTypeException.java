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
package cn.taketoday.util;

/**
 * Exception thrown from {@link MimeTypeUtils#parseMimeType(String)} in case of
 * encountering an invalid content type specification String.
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @author TODAY <br>
 * 2019-12-08 19:28
 */
@SuppressWarnings("serial")
public class InvalidMimeTypeException extends IllegalArgumentException {

  private final String mimeType;

  /**
   * Create a new InvalidContentTypeException for the given content type.
   *
   * @param mimeType the offending media type
   * @param message a detail message indicating the invalid part
   */
  public InvalidMimeTypeException(String mimeType, String message) {
    super("Invalid mime type \"" + mimeType + "\": " + message);
    this.mimeType = mimeType;
  }

  /**
   * Return the offending content type.
   */
  public String getMimeType() {
    return this.mimeType;
  }

}
