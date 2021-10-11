/**
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
package cn.taketoday.web;

import java.util.Map;
import java.util.function.Supplier;

import cn.taketoday.beans.factory.StandardBeanFactory;
import cn.taketoday.lang.Assert;
import cn.taketoday.web.session.WebSession;
import cn.taketoday.web.session.WebSessionManager;

/**
 * @author TODAY <br>
 * 2019-11-20 21:47
 */
public class StandardWebBeanFactory extends StandardBeanFactory {

  @Override
  protected Map<Class<?>, Object> createObjectFactories() {
    final Map<Class<?>, Object> env = super.createObjectFactories();
    // @since 3.0
    final class WebSessionFactory implements Supplier<WebSession> {
      WebSessionManager sessionManager;

      private WebSessionManager obtainWebSessionManager() {
        WebSessionManager sessionManager = this.sessionManager;
        if (sessionManager == null) {
          sessionManager = getBean(WebSessionManager.class);
          Assert.state(sessionManager != null, "You must enable web session -> @EnableWebSession");
          this.sessionManager = sessionManager;
        }
        return sessionManager;
      }

      @Override
      public WebSession get() {
        final RequestContext context = RequestContextHolder.currentContext();
        return obtainWebSessionManager().getSession(context);
      }
    }
    env.put(WebSession.class, new WebSessionFactory());
    env.put(RequestContext.class, factory(RequestContextHolder::currentContext));
    return env;
  }

  protected <T> Supplier<T> factory(Supplier<T> objectFactory) {
    return objectFactory;
  }

}
