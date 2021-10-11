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

package cn.taketoday.web.socket.jetty;

import org.eclipse.jetty.websocket.api.WebSocketPartialListener;

import java.nio.ByteBuffer;

import cn.taketoday.web.socket.BinaryMessage;
import cn.taketoday.web.socket.TextMessage;
import cn.taketoday.web.socket.WebSocketHandler;

/**
 * @author TODAY 2021/5/6 21:59
 * @since 3.0.1
 */
public class JettyPartialWebSocketConnectionListener
        extends AbstractWebSocketConnectionListener implements WebSocketPartialListener {

  public JettyPartialWebSocketConnectionListener(JettyWebSocketSession session, WebSocketHandler handler) {
    super(session, handler);
  }

  // Partial

  @Override
  public void onWebSocketPartialBinary(ByteBuffer payload, boolean last) {
    handler.handleMessage(session, new BinaryMessage(payload, last));
  }

  @Override
  public void onWebSocketPartialText(String payload, boolean last) {
    handler.handleMessage(session, new TextMessage(payload, last));
  }

}
