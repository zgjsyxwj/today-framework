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

package cn.taketoday.web.socket;

import java.util.Objects;

import cn.taketoday.lang.Assert;

/**
 * Represents a WebSocket close status code and reason. Status codes in the 1xxx range are
 * pre-defined by the protocol. Optionally, a status code may be sent with a reason.
 * <p>
 * When closing an established connection (e.g., when sending a Close
 * frame, after the opening handshake has completed), an endpoint MAY
 * indicate a reason for closure.  The interpretation of this reason by
 * an endpoint, and the action an endpoint should take given this
 * reason, are left undefined by this specification.  This specification
 * defines a set of pre-defined status codes and specifies which ranges
 * may be used by extensions, frameworks, and end applications.  The
 * status code and any associated textual message are optional
 * components of a Close frame.
 * </p>
 * <p>See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455, Section 7.4.1
 * "Defined Status Codes"</a>.
 * <p>
 * From Spring
 * </p>
 *
 * @author Rossen Stoyanchev
 * @author TODAY 2021/4/5 17:53
 * @since 3.0
 */
public final class CloseStatus {

  /**
   * "1000 indicates a normal closure, meaning that the purpose for which the connection
   * was established has been fulfilled."
   */
  public static final CloseStatus NORMAL = new CloseStatus(1000);

  /**
   * "1001 indicates that an endpoint is "going away", such as a server going down or a
   * browser having navigated away from a page."
   */
  public static final CloseStatus GOING_AWAY = new CloseStatus(1001);

  /**
   * "1002 indicates that an endpoint is terminating the connection due to a protocol
   * error."
   */
  public static final CloseStatus PROTOCOL_ERROR = new CloseStatus(1002);

  /**
   * "1003 indicates that an endpoint is terminating the connection because it has
   * received a type of data it cannot accept (e.g., an endpoint that understands only
   * text data MAY send this if it receives a binary message)."
   */
  public static final CloseStatus NOT_ACCEPTABLE = new CloseStatus(1003);

  // 10004: Reserved.
  // The specific meaning might be defined in the future.

  /**
   * "1005 is a reserved value and MUST NOT be set as a status code in a Close control
   * frame by an endpoint. It is designated for use in applications expecting a status
   * code to indicate that no status code was actually present."
   */
  public static final CloseStatus NO_STATUS_CODE = new CloseStatus(1005);

  /**
   * "1006 is a reserved value and MUST NOT be set as a status code in a Close control
   * frame by an endpoint. It is designated for use in applications expecting a status
   * code to indicate that the connection was closed abnormally, e.g., without sending
   * or receiving a Close control frame."
   */
  public static final CloseStatus NO_CLOSE_FRAME = new CloseStatus(1006);

  /**
   * "1007 indicates that an endpoint is terminating the connection because it has
   * received data within a message that was not consistent with the type of the message
   * (e.g., non-UTF-8 [RFC3629] data within a text message)."
   */
  public static final CloseStatus BAD_DATA = new CloseStatus(1007);

  /**
   * "1008 indicates that an endpoint is terminating the connection because it has
   * received a message that violates its policy. This is a generic status code that can
   * be returned when there is no other more suitable status code (e.g., 1003 or 1009)
   * or if there is a need to hide specific details about the policy."
   */
  public static final CloseStatus POLICY_VIOLATION = new CloseStatus(1008);

  /**
   * "1009 indicates that an endpoint is terminating the connection because it has
   * received a message that is too big for it to process."
   */
  public static final CloseStatus TOO_BIG_TO_PROCESS = new CloseStatus(1009);

  /**
   * "1010 indicates that an endpoint (client) is terminating the connection because it
   * has expected the server to negotiate one or more extension, but the server didn't
   * return them in the response message of the WebSocket handshake. The list of
   * extensions that are needed SHOULD appear in the /reason/ part of the Close frame.
   * Note that this status code is not used by the server, because it can fail the
   * WebSocket handshake instead."
   */
  public static final CloseStatus REQUIRED_EXTENSION = new CloseStatus(1010);

  /**
   * "1011 indicates that a server is terminating the connection because it encountered
   * an unexpected condition that prevented it from fulfilling the request."
   */
  public static final CloseStatus SERVER_ERROR = new CloseStatus(1011);

  /**
   * "1012 indicates that the service is restarted. A client may reconnect, and if it
   * chooses to do, should reconnect using a randomized delay of 5 - 30s."
   */
  public static final CloseStatus SERVICE_RESTARTED = new CloseStatus(1012);

  /**
   * "1013 indicates that the service is experiencing overload. A client should only
   * connect to a different IP (when there are multiple for the target) or reconnect to
   * the same IP upon user action."
   */
  public static final CloseStatus SERVICE_OVERLOAD = new CloseStatus(1013);

  /**
   * "1015 is a reserved value and MUST NOT be set as a status code in a Close control
   * frame by an endpoint. It is designated for use in applications expecting a status
   * code to indicate that the connection was closed due to a failure to perform a TLS
   * handshake (e.g., the server certificate can't be verified)."
   */
  public static final CloseStatus TLS_HANDSHAKE_FAILURE = new CloseStatus(1015);

  /**
   * A status code for use within the framework the indicate a session has
   * become unreliable (e.g. timed out while sending a message) and extra
   * care should be exercised, e.g. avoid sending any further data to the
   * client that may be done during normal shutdown.
   */
  public static final CloseStatus SESSION_NOT_RELIABLE = new CloseStatus(4500);

  private final int code;

  private final String reason;

  /**
   * Create a new {@link CloseStatus} instance.
   *
   * @param code the status code
   */
  public CloseStatus(int code) {
    this(code, null);
  }

  /**
   * Create a new {@link CloseStatus} instance.
   *
   * @param code the status code
   * @param reason the reason
   */
  public CloseStatus(int code, String reason) {
    Assert.isTrue((code >= 1000 && code < 5000), "Invalid status code");
    this.code = code;
    this.reason = reason;
  }

  /**
   * Return the status code.
   */
  public int getCode() {
    return this.code;
  }

  /**
   * Return the reason, or {@code null} if none.
   */
  public String getReason() {
    return this.reason;
  }

  /**
   * Create a new {@link CloseStatus} from this one with the specified reason.
   *
   * @param reason the reason
   * @return a new {@link CloseStatus} instance
   */
  public CloseStatus withReason(String reason) {
    Assert.hasText(reason, "Reason must not be empty");
    return new CloseStatus(this.code, reason);
  }

  public boolean equalsCode(CloseStatus other) {
    return (this.code == other.code);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof CloseStatus)) {
      return false;
    }
    CloseStatus otherStatus = (CloseStatus) other;
    return (this.code == otherStatus.code && Objects.equals(this.reason, otherStatus.reason));
  }

  @Override
  public int hashCode() {
    return this.code * 29 + Objects.hashCode(this.reason);
  }

  @Override
  public String toString() {
    return "CloseStatus[code=" + this.code + ", reason=" + this.reason + "]";
  }

}
