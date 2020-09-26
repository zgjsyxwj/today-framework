/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2020 All Rights Reserved.
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
package cn.taketoday.web.handler;

import java.util.List;

import cn.taketoday.context.OrderedSupport;
import cn.taketoday.context.logger.Logger;
import cn.taketoday.context.logger.LoggerFactory;
import cn.taketoday.web.RequestContext;
import cn.taketoday.web.interceptor.HandlerInterceptor;
import cn.taketoday.web.interceptor.HandlerInterceptorsCapable;

import static cn.taketoday.context.utils.ObjectUtils.isEmpty;

/**
 * @author TODAY <br>
 * 2019-12-25 16:19
 */
public abstract class InterceptableRequestHandler
  extends OrderedSupport implements RequestHandler, HandlerInterceptorsCapable {

  private static final Logger log = LoggerFactory.getLogger(InterceptableRequestHandler.class);

  /** 拦截器 */
  private HandlerInterceptor[] interceptors;

  public InterceptableRequestHandler() {}

  public InterceptableRequestHandler(HandlerInterceptor... interceptors) {
    setInterceptors(interceptors);
  }

  /**
   * Before Handler process.
   *
   * @param context
   *   Current request Context
   * @param interceptors
   *   Target handler's {@link HandlerInterceptor}s
   *
   * @return If is it possible to execute the target handler
   *
   * @throws Throwable
   *   If any exception occurred in a {@link HandlerInterceptor}
   */
  protected boolean beforeProcess(final RequestContext context, final HandlerInterceptor[] interceptors) throws Throwable {
    for (final HandlerInterceptor intercepter : interceptors) {
      if (!intercepter.beforeProcess(context, this)) {
        if (log.isDebugEnabled()) {
          log.debug("Interceptor: [{}] return false", intercepter);
        }
        return false;
      }
    }
    return true;
  }

  /**
   * After Handler processed.
   *
   * @param result
   *   Handler executed result
   * @param context
   *   Current request context
   * @param interceptors
   *   Target handler's {@link HandlerInterceptor}s
   *
   * @throws Throwable
   *   If any exception occurred in a {@link HandlerInterceptor}
   */
  protected void afterProcess(final Object result,
                              final RequestContext context,
                              final HandlerInterceptor[] interceptors) throws Throwable {
    for (final HandlerInterceptor intercepter : interceptors) {
      intercepter.afterProcess(context, this, result);
    }
  }

  @Override
  public Object handleRequest(final RequestContext context) throws Throwable {

    final HandlerInterceptor[] interceptors = getInterceptors();
    if (interceptors != null) {
      if (!beforeProcess(context, interceptors)) { // before process
        return HandlerAdapter.NONE_RETURN_VALUE;
      }
      final Object result = handleInternal(context);
      afterProcess(result, context, interceptors);
      return result;
    }
    return handleInternal(context);
  }

  protected abstract Object handleInternal(final RequestContext context) throws Throwable;

  @Override
  public HandlerInterceptor[] getInterceptors() {
    return interceptors;
  }

  public void setInterceptors(List<HandlerInterceptor> interceptors) {
    setInterceptors(isEmpty(interceptors) ? null : interceptors.toArray(new HandlerInterceptor[interceptors.size()]));
  }

  public void setInterceptors(HandlerInterceptor... interceptors) {
    this.interceptors = interceptors;
  }

}
