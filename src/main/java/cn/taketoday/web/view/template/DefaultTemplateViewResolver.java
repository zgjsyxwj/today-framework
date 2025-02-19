/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2022 All Rights Reserved.
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

package cn.taketoday.web.view.template;

import cn.taketoday.context.aware.ResourceLoaderAware;
import cn.taketoday.core.io.ResourceLoader;
import cn.taketoday.expression.ExpressionContext;
import cn.taketoday.expression.ExpressionFactory;
import cn.taketoday.lang.Nullable;
import cn.taketoday.web.view.AbstractTemplateViewResolver;
import cn.taketoday.web.view.AbstractUrlBasedView;

/**
 * Convenience subclass of {@link cn.taketoday.web.view.UrlBasedViewResolver}
 * that supports {@link DefaultTemplateView}
 *
 * <p>The view class for all views generated by this resolver can be specified
 * via the "viewClass" property. See UrlBasedViewResolver's javadoc for details.
 *
 * <p><b>Note:</b> When chaining ViewResolvers, a DefaultTemplateViewResolver will
 * check for the existence of the specified template resources and only return
 * a non-null View object if the template was actually found.
 *
 * @author Juergen Hoeller
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @see #setViewClass
 * @see #setPrefix
 * @see #setSuffix
 * @see #setRequestContextAttribute
 * @see DefaultTemplateView
 * @since 4.0 2022/2/9 21:21
 */
public class DefaultTemplateViewResolver extends AbstractTemplateViewResolver implements ResourceLoaderAware {

  @Nullable
  private ResourceLoader resourceLoader;

  @Nullable
  private ExpressionContext sharedContext;

  @Nullable
  private ExpressionFactory expressionFactory = ExpressionFactory.getSharedInstance();

  /**
   * Sets the default {@link #setViewClass view class} to {@link #requiredViewClass}:
   * by default {@link DefaultTemplateView}.
   */
  public DefaultTemplateViewResolver() {
    setViewClass(requiredViewClass());
  }

  /**
   * A convenience constructor that allows for specifying {@link #setPrefix prefix}
   * and {@link #setSuffix suffix} as constructor arguments.
   *
   * @param prefix the prefix that gets prepended to view names when building a URL
   * @param suffix the suffix that gets appended to view names when building a URL
   */
  public DefaultTemplateViewResolver(String prefix, String suffix) {
    this();
    setPrefix(prefix);
    setSuffix(suffix);
  }

  /**
   * Requires {@link DefaultTemplateView}.
   */
  @Override
  protected Class<?> requiredViewClass() {
    return DefaultTemplateView.class;
  }

  @Override
  protected AbstractUrlBasedView instantiateView() {
    if (getViewClass() == DefaultTemplateView.class) {
      DefaultTemplateView templateView = new DefaultTemplateView();
      templateView.setSharedContext(sharedContext);
      templateView.setResourceLoader(resourceLoader);
      templateView.setExpressionFactory(expressionFactory);
    }
    return super.instantiateView();
  }

  @Override
  public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public void setExpressionFactory(@Nullable ExpressionFactory expressionFactory) {
    this.expressionFactory = expressionFactory;
  }

  public void setSharedContext(@Nullable ExpressionContext sharedContext) {
    this.sharedContext = sharedContext;
  }

}
