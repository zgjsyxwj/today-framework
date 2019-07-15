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
package cn.taketoday.web.interceptor;

import cn.taketoday.web.RequestContext;
import cn.taketoday.web.mapping.HandlerMethod;
import cn.taketoday.web.mapping.WebMapping;

/**
 * Handler Intercepter process around Handler.
 * 
 * @author TODAY <br>
 *         2018-06-25 20:06:11
 */
@FunctionalInterface
public interface HandlerInterceptor {

    /**
     * Before {@link HandlerMethod} process.
     * 
     * @param requestContext
     *            Current request Context
     * @param webMapping
     *            request mapping
     * @return
     * @throws Throwable
     */
    boolean beforeProcess(RequestContext requestContext, WebMapping webMapping) throws Throwable;

    /**
     * After {@link HandlerMethod} process.
     * 
     * @param requestContext
     *            Current request Context
     * @param webMapping
     *            request mapping
     * @param result
     *            HandlerMethod returned value
     * @throws Throwable
     */
    default void afterProcess(RequestContext requestContext, WebMapping webMapping, Object result) throws Throwable {

    }
}
