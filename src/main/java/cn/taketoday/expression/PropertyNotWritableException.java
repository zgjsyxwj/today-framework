/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.taketoday.expression;

/**
 * Thrown when a property could not be written to while setting the value on a
 * {@link ValueExpression}.
 *
 * <p>
 * For example, this could be triggered by trying to set a map value on an
 * unmodifiable map.
 * </p>
 *
 * @since JSP 2.1
 */
@SuppressWarnings("serial")
public class PropertyNotWritableException extends ExpressionException {

  // -------------------------------------

  /**
   * Creates a <code>PropertyNotWritableException</code> with no detail message.
   */
  public PropertyNotWritableException() {
    super();
  }

  // -------------------------------------

  /**
   * Creates a <code>PropertyNotWritableException</code> with the provided detail
   * message.
   *
   * @param pMessage the detail message
   */
  public PropertyNotWritableException(String pMessage) {
    super(pMessage);
  }

  // -------------------------------------

  /**
   * Creates a <code>PropertyNotWritableException</code> with the given root
   * cause.
   *
   * @param exception the originating cause of this exception
   */
  public PropertyNotWritableException(Throwable exception) {
    super(exception);
  }

  // -------------------------------------

  /**
   * Creates a <code>PropertyNotWritableException</code> with the given detail
   * message and root cause.
   *
   * @param pMessage the detail message
   * @param pRootCause the originating cause of this exception
   */
  public PropertyNotWritableException(String pMessage, Throwable pRootCause) {
    super(pMessage, pRootCause);
  }

}
