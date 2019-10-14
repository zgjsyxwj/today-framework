/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2019 All Rights Reserved.
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.taketoday.framework;

import javax.servlet.Servlet;

import org.slf4j.LoggerFactory;

import cn.taketoday.context.ApplicationContext;
import cn.taketoday.context.exception.ConfigurationException;
import cn.taketoday.framework.server.AbstractWebServer;
import cn.taketoday.framework.server.ConfigurableWebServer;
import cn.taketoday.framework.server.WebServer;
import cn.taketoday.web.servlet.ConfigurableWebServletApplicationContext;
import cn.taketoday.web.servlet.StandardWebServletApplicationContext;

/**
 * {@link Servlet} based Web {@link ApplicationContext}
 * 
 * @author TODAY <br>
 *         2019-01-17 15:54
 */
public class ServletWebServerApplicationContext
        extends StandardWebServletApplicationContext
        implements WebServerApplicationContext, ConfigurableWebServletApplicationContext {

    private WebServer webServer;

    private final Class<?> startupClass;

    public ServletWebServerApplicationContext(Class<?> startupClass) {
        this.startupClass = startupClass;
    }

    public ServletWebServerApplicationContext() {
        this(null);
    }

    @Override
    protected void onRefresh() throws Throwable {

        // disable web mvc xml
        getEnvironment().setProperty(Constant.ENABLE_WEB_MVC_XML, "false");

        LoggerFactory.getLogger(getClass()).info("Looking For: [{}] Bean.", WebServer.class.getName());

        // Get WebServer instance
        this.webServer = getBean(WebServer.class);
        if (this.webServer == null) {
            throw new ConfigurationException("The context doesn't exist a [cn.taketoday.framework.server.WebServer] bean");
        }

        if (this.webServer instanceof ConfigurableWebServer) {
            if (this.webServer instanceof AbstractWebServer) {

                ((AbstractWebServer) webServer).getWebApplicationConfiguration()//
                        .configureWebServer((AbstractWebServer) webServer);
            }

            LoggerFactory.getLogger(getClass()).info("Initializing Web Server: [{}]", webServer);

            ((ConfigurableWebServer) webServer).initialize();
        }
        super.onRefresh();
    }

    @Override
    public WebServer getWebServer() {
        return webServer;
    }

    @Override
    public Class<?> getStartupClass() {
        return startupClass;
    }

}
