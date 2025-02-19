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
package cn.taketoday.web.socket.client;

import cn.taketoday.context.Lifecycle;
import cn.taketoday.http.HttpHeaders;
import cn.taketoday.lang.Nullable;
import cn.taketoday.util.concurrent.ListenableFuture;
import cn.taketoday.util.concurrent.ListenableFutureCallback;
import cn.taketoday.web.socket.LoggingWebSocketHandlerDecorator;
import cn.taketoday.web.socket.WebSocketHandler;
import cn.taketoday.web.socket.WebSocketHttpHeaders;
import cn.taketoday.web.socket.WebSocketSession;

import java.util.List;

/**
 * A WebSocket connection manager that is given a URI, a {@link WebSocketClient}, and a
 * {@link WebSocketHandler}, connects to a WebSocket server through {@link #start()} and
 * {@link #stop()} methods. If {@link #setAutoStartup(boolean)} is set to {@code true}
 * this will be done automatically when the ApplicationContext is refreshed.
 *
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @author TODAY 2021/11/12 15:58
 * @since 4.0
 */
public class WebSocketConnectionManager extends ConnectionManagerSupport {
  private final WebSocketClient client;
  private final WebSocketHandler webSocketHandler;

  @Nullable
  private WebSocketSession webSocketSession;

  private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

  public WebSocketConnectionManager(
          WebSocketClient client, WebSocketHandler webSocketHandler, String uriTemplate, Object... uriVariables) {

    super(uriTemplate, uriVariables);
    this.client = client;
    this.webSocketHandler = decorateWebSocketHandler(webSocketHandler);
  }

  /**
   * Decorate the WebSocketHandler provided to the class constructor.
   * <p>By default {@link LoggingWebSocketHandlerDecorator} is added.
   */
  protected WebSocketHandler decorateWebSocketHandler(WebSocketHandler handler) {
    return new LoggingWebSocketHandlerDecorator(handler);
  }

  /**
   * Set the sub-protocols to use. If configured, specified sub-protocols will be
   * requested in the handshake through the {@code Sec-WebSocket-Protocol} header. The
   * resulting WebSocket session will contain the protocol accepted by the server, if
   * any.
   */
  public void setSubProtocols(List<String> protocols) {
    this.headers.setSecWebSocketProtocol(protocols);
  }

  /**
   * Return the configured sub-protocols to use.
   */
  public List<String> getSubProtocols() {
    return this.headers.getSecWebSocketProtocol();
  }

  /**
   * Set the origin to use.
   */
  public void setOrigin(@Nullable String origin) {
    this.headers.setOrigin(origin);
  }

  /**
   * Return the configured origin.
   */
  @Nullable
  public String getOrigin() {
    return this.headers.getOrigin();
  }

  /**
   * Provide default headers to add to the WebSocket handshake request.
   */
  public void setHeaders(HttpHeaders headers) {
    this.headers.clear();
    this.headers.putAll(headers);
  }

  /**
   * Return the default headers for the WebSocket handshake request.
   */
  public HttpHeaders getHeaders() {
    return this.headers;
  }

  @Override
  public void startInternal() {
    if (this.client instanceof Lifecycle lifecycle && !lifecycle.isRunning()) {
      lifecycle.start();
    }
    super.startInternal();
  }

  @Override
  public void stopInternal() throws Exception {
    if (this.client instanceof Lifecycle lifecycle && lifecycle.isRunning()) {
      lifecycle.stop();
    }
    super.stopInternal();
  }

  @Override
  protected void openConnection() {
    logger.info("Connecting to WebSocket at {}", getUri());

    ListenableFuture<WebSocketSession> future =
            this.client.doHandshake(this.webSocketHandler, this.headers, getUri());

    future.addCallback(new ListenableFutureCallback<>() {
      @Override
      public void onSuccess(@Nullable WebSocketSession result) {
        webSocketSession = result;
        logger.info("Successfully connected");
      }

      @Override
      public void onFailure(Throwable ex) {
        logger.error("Failed to connect", ex);
      }
    });
  }

  @Override
  protected void closeConnection() throws Exception {
    if (this.webSocketSession != null) {
      this.webSocketSession.close();
    }
  }

  @Override
  protected boolean isConnected() {
    return (this.webSocketSession != null && this.webSocketSession.isOpen());
  }

}
