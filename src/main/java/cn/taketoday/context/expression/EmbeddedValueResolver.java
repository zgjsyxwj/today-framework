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

package cn.taketoday.context.expression;

import cn.taketoday.beans.factory.support.BeanExpressionContext;
import cn.taketoday.beans.factory.support.BeanExpressionResolver;
import cn.taketoday.beans.factory.support.ConfigurableBeanFactory;
import cn.taketoday.core.StringValueResolver;
import cn.taketoday.lang.Nullable;

/**
 * {@link StringValueResolver} adapter for resolving placeholders and
 * expressions against a {@link ConfigurableBeanFactory}.
 *
 * <p>Note that this adapter resolves expressions as well, in contrast
 * to the {@link ConfigurableBeanFactory#resolveEmbeddedValue} method.
 * The {@link BeanExpressionContext} used is for the plain bean factory,
 * with no scope specified for any contextual objects to access.
 *
 * @author Juergen Hoeller
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @see ConfigurableBeanFactory#resolveEmbeddedValue(String)
 * @see ConfigurableBeanFactory#getBeanExpressionResolver()
 * @see BeanExpressionContext
 * @since 4.0 2021/12/7 11:24
 */
public class EmbeddedValueResolver implements StringValueResolver {

  private final BeanExpressionContext exprContext;

  @Nullable
  private final BeanExpressionResolver exprResolver;

  public EmbeddedValueResolver(ConfigurableBeanFactory beanFactory) {
    this.exprContext = new BeanExpressionContext(beanFactory, null);
    this.exprResolver = beanFactory.getBeanExpressionResolver();
  }

  @Override
  @Nullable
  public String resolveStringValue(String strVal) {
    String value = exprContext.getBeanFactory().resolveEmbeddedValue(strVal);
    if (exprResolver != null && value != null) {
      Object evaluated = this.exprResolver.evaluate(value, this.exprContext);
      value = evaluated != null ? evaluated.toString() : null;
    }
    return value;
  }

  /**
   * contains placeholder or EL expressions
   *
   * @param expr expression
   */
  public static boolean isEmbedded(@Nullable String expr) {
    return expr != null && ((expr.startsWith("#{") || expr.startsWith("${")) && expr.endsWith("}"));
  }

}
