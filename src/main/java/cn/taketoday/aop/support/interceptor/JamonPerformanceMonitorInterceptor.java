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
package cn.taketoday.aop.support.interceptor;

import com.jamonapi.MonKey;
import com.jamonapi.MonKeyImp;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.jamonapi.utils.Misc;

import org.aopalliance.intercept.MethodInvocation;

import cn.taketoday.logging.Logger;

/**
 * Performance monitor interceptor that uses <b>JAMon</b> library to perform the
 * performance measurement on the intercepted method and output the stats.
 * In addition, it tracks/counts exceptions thrown by the intercepted method.
 * The stack traces can be viewed in the JAMon web application.
 *
 * <p>This code is inspired by Thierry Templier's blog.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Steve Souza
 * @author TODAY
 * @see com.jamonapi.MonitorFactory
 * @see PerformanceMonitorInterceptor
 * @since 3.0
 */
@SuppressWarnings("serial")
public class JamonPerformanceMonitorInterceptor extends AbstractMonitoringInterceptor {

  private boolean trackAllInvocations = false;

  /**
   * Create a new JamonPerformanceMonitorInterceptor with a static logger.
   */
  public JamonPerformanceMonitorInterceptor() {
  }

  /**
   * Create a new JamonPerformanceMonitorInterceptor with a dynamic or static logger,
   * according to the given flag.
   *
   * @param useDynamicLogger whether to use a dynamic logger or a static logger
   * @see #setUseDynamicLogger
   */
  public JamonPerformanceMonitorInterceptor(boolean useDynamicLogger) {
    setUseDynamicLogger(useDynamicLogger);
  }

  /**
   * Create a new JamonPerformanceMonitorInterceptor with a dynamic or static logger,
   * according to the given flag.
   *
   * @param useDynamicLogger whether to use a dynamic logger or a static logger
   * @param trackAllInvocations whether to track all invocations that go through
   * this interceptor, or just invocations with trace logging enabled
   * @see #setUseDynamicLogger
   */
  public JamonPerformanceMonitorInterceptor(boolean useDynamicLogger, boolean trackAllInvocations) {
    setUseDynamicLogger(useDynamicLogger);
    setTrackAllInvocations(trackAllInvocations);
  }

  /**
   * Set whether to track all invocations that go through this interceptor,
   * or just invocations with trace logging enabled.
   * <p>Default is "false": Only invocations with trace logging enabled will
   * be monitored. Specify "true" to let JAMon track all invocations,
   * gathering statistics even when trace logging is disabled.
   */
  public void setTrackAllInvocations(boolean trackAllInvocations) {
    this.trackAllInvocations = trackAllInvocations;
  }

  /**
   * Always applies the interceptor if the "trackAllInvocations" flag has been set;
   * else just kicks in if the log is enabled.
   *
   * @see #setTrackAllInvocations
   * @see #isLogEnabled
   */
  @Override
  protected boolean isInterceptorEnabled(MethodInvocation invocation, Logger logger) {
    return (this.trackAllInvocations || isLogEnabled(logger));
  }

  /**
   * Wraps the invocation with a JAMon Monitor and writes the current
   * performance statistics to the log (if enabled).
   *
   * @see com.jamonapi.MonitorFactory#start
   * @see com.jamonapi.Monitor#stop
   */
  @Override
  protected Object invokeUnderTrace(MethodInvocation invocation, Logger logger) throws Throwable {
    String name = createInvocationTraceName(invocation);
    MonKey key = new MonKeyImp(name, name, "ms.");

    Monitor monitor = MonitorFactory.start(key);
    try {
      return invocation.proceed();
    }
    catch (Throwable ex) {
      trackException(key, ex);
      throw ex;
    }
    finally {
      monitor.stop();
      if (!this.trackAllInvocations || isLogEnabled(logger)) {
        writeToLog(logger, "JAMon performance statistics for method [" + name + "]:\n" + monitor);
      }
    }
  }

  /**
   * Count the thrown exception and put the stack trace in the details portion of the key.
   * This will allow the stack trace to be viewed in the JAMon web application.
   */
  protected void trackException(MonKey key, Throwable ex) {
    String stackTrace = "stackTrace=" + Misc.getExceptionTrace(ex);
    key.setDetails(stackTrace);

    // Specific exception counter. Example: java.lang.RuntimeException
    MonitorFactory.add(new MonKeyImp(ex.getClass().getName(), stackTrace, "Exception"), 1);

    // General exception counter which is a total for all exceptions thrown
    MonitorFactory.add(new MonKeyImp(MonitorFactory.EXCEPTIONS_LABEL, stackTrace, "Exception"), 1);
  }

}
