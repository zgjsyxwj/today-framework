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
package cn.taketoday.aop;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * Part of a {@link Pointcut}: Checks whether the target method is eligible for advice.
 *
 * <p>A MethodMatcher may be evaluated <b>statically</b> or at <b>runtime</b> (dynamically).
 * Static matching involves method and (possibly) method attributes. Dynamic matching
 * also makes arguments for a particular call available, and any effects of running
 * previous advice applying to the join-point.
 *
 * <p>If an implementation returns {@code false} from its {@link #isRuntime()}
 * method, evaluation can be performed statically, and the result will be the same
 * for all invocations of this method, whatever their arguments. This means that
 * if the {@link #isRuntime()} method returns {@code false}, the 1-arg
 * {@link #matches(MethodInvocation)} method will never be invoked.
 *
 * <p>If an implementation returns {@code true} from its 2-arg
 * {@link #matches(java.lang.reflect.Method, Class)} method and its {@link #isRuntime()} method
 * returns {@code true}, the 1-arg {@link #matches(MethodInvocation)}
 * method will be invoked <i>immediately before each potential execution of the related advice</i>,
 * to decide whether the advice should run. All previous advice, such as earlier interceptors
 * in an interceptor chain, will have run, so any state changes they have produced in
 * parameters or ThreadLocal state will be available at the time of evaluation.
 *
 * <p>Concrete implementations of this interface typically should provide proper
 * implementations of {@link Object#equals(Object)} and {@link Object#hashCode()}
 * in order to allow the matcher to be used in caching scenarios &mdash; for
 * example, in proxies generated by CGLIB.
 *
 * @author Rod Johnson
 * @author TODAY 2019-10-20 22:43
 * @see Pointcut
 * @see ClassFilter
 * @since 3.0
 */
public interface MethodMatcher {

  /**
   * Checking whether the given method matches.
   *
   * @param method the candidate method
   * @param targetClass the target class
   * @return whether or not this method matches on application startup.
   */
  boolean matches(Method method, Class<?> targetClass);

  /**
   * Is this MethodMatcher dynamic, that is, must a final call be made on the
   * {@link #matches(MethodInvocation)} method at runtime
   * even if the 2-arg matches method returns {@code true}?
   * <p>
   * Can be invoked when an AOP proxy is created, and need not be invoked again
   * before each method invocation,
   *
   * @return whether or not a runtime match via the 1-arg  {@link #matches(MethodInvocation)}
   * method is required if static matching passed
   */
  boolean isRuntime();

  /**
   * Check whether there a runtime (dynamic) match for this method, which must
   * have matched statically.
   * <p>
   * This method is invoked only if the 2-arg matches method returns {@code true}
   * for the given method and target class, and if the {@link #isRuntime()} method
   * returns {@code true}. Invoked immediately before potential running of the
   * advice, after any advice earlier in the advice chain has run.
   *
   * @param invocation runtime invocation contains the candidate method
   * and target class, arguments to the method
   * @return whether there's a runtime match
   * @see MethodMatcher#matches(Method, Class)
   */
  boolean matches(MethodInvocation invocation);

  /**
   * Canonical instance that matches all methods.
   */
  MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;

}
