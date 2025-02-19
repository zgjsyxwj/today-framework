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

package cn.taketoday.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents an HTTP output message that allows for setting a streaming body.
 * Note that such messages typically do not support {@link #getBody()} access.
 *
 * @author Arjen Poutsma
 * @see #setBody
 * @since 4.0
 */
public interface StreamingHttpOutputMessage extends HttpOutputMessage {

  /**
   * Set the streaming body callback for this message.
   *
   * @param body the streaming body callback
   */
  void setBody(Body body);

  /**
   * Defines the contract for bodies that can be written directly to an
   * {@link OutputStream}. Useful with HTTP client libraries that provide
   * indirect access to an {@link OutputStream} via a callback mechanism.
   */
  @FunctionalInterface
  interface Body {

    /**
     * Write this body to the given {@link OutputStream}.
     *
     * @param outputStream the output stream to write to
     * @throws IOException in case of I/O errors
     */
    void writeTo(OutputStream outputStream) throws IOException;
  }

}
