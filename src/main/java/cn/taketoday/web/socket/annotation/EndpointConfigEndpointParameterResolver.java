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

package cn.taketoday.web.socket.annotation;

import jakarta.websocket.EndpointConfig;

import cn.taketoday.web.handler.method.ResolvableMethodParameter;
import cn.taketoday.web.socket.WebSocketSession;

/**
 * @author TODAY 2021/5/10 0:02
 * @since 3.0.1
 */
public class EndpointConfigEndpointParameterResolver implements EndpointParameterResolver {

  @Override
  public boolean supports(ResolvableMethodParameter parameter) {
    return parameter.is(EndpointConfig.class);
  }

  @Override
  public Object resolve(WebSocketSession session, ResolvableMethodParameter parameter) {
    return session.getAttribute(WebSocketSession.JAVAX_ENDPOINT_CONFIG_KEY);
  }
}
