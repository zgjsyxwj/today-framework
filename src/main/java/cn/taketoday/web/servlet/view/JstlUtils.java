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

package cn.taketoday.web.servlet.view;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import cn.taketoday.context.ApplicationContext;
import cn.taketoday.context.MessageSource;
import cn.taketoday.context.support.MessageSourceResourceBundle;
import cn.taketoday.context.support.ResourceBundleMessageSource;
import cn.taketoday.core.i18n.LocaleContext;
import cn.taketoday.core.i18n.TimeZoneAwareLocaleContext;
import cn.taketoday.lang.Nullable;
import cn.taketoday.web.LocaleContextResolver;
import cn.taketoday.web.LocaleResolver;
import cn.taketoday.web.RequestContext;
import cn.taketoday.web.RequestContextUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.jstl.core.Config;
import jakarta.servlet.jsp.jstl.fmt.LocalizationContext;

/**
 * Helper class for preparing JSTL views,
 * in particular for exposing a JSTL localization context.
 *
 * @author Juergen Hoeller
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/2/3 22:45
 */
public abstract class JstlUtils {

  /**
   * Checks JSTL's "jakarta.servlet.jsp.jstl.fmt.localizationContext"
   * context-param and creates a corresponding child message source,
   * with the provided Framework-defined MessageSource as parent.
   *
   * @param servletContext the ServletContext we're running in
   * (to check JSTL-related context-params in {@code web.xml})
   * @param messageSource the MessageSource to expose, typically
   * the ApplicationContext of the current DispatcherServlet
   * @return the MessageSource to expose to JSTL; first checking the
   * JSTL-defined bundle, then the Framework-defined MessageSource
   * @see ApplicationContext
   */
  public static MessageSource getJstlAwareMessageSource(
          @Nullable ServletContext servletContext, MessageSource messageSource) {
    if (servletContext != null) {
      String jstlInitParam = servletContext.getInitParameter(Config.FMT_LOCALIZATION_CONTEXT);
      if (jstlInitParam != null) {
        // Create a ResourceBundleMessageSource for the specified resource bundle
        // basename in the JSTL context-param in web.xml, wiring it with the given
        // Framework-defined MessageSource as parent.
        ResourceBundleMessageSource jstlBundleWrapper = new ResourceBundleMessageSource();
        jstlBundleWrapper.setBasename(jstlInitParam);
        jstlBundleWrapper.setParentMessageSource(messageSource);
        return jstlBundleWrapper;
      }
    }
    return messageSource;
  }

  /**
   * Exposes JSTL-specific request attributes specifying locale
   * and resource bundle for JSTL's formatting and message tags,
   * using Framework's locale and MessageSource.
   *
   * @param request the current HTTP request
   * @param servletRequest the current HTTP servlet request
   * @param messageSource the MessageSource to expose,
   * typically the current ApplicationContext (may be {@code null})
   * @see #exposeLocalizationContext(RequestContext, HttpServletRequest)
   */
  public static void exposeLocalizationContext(
          RequestContext request, HttpServletRequest servletRequest, @Nullable MessageSource messageSource) {
    Locale jstlLocale = RequestContextUtils.getLocale(request);
    Config.set(servletRequest, Config.FMT_LOCALE, jstlLocale);
    TimeZone timeZone = RequestContextUtils.getTimeZone(request);
    if (timeZone != null) {
      Config.set(servletRequest, Config.FMT_TIME_ZONE, timeZone);
    }
    if (messageSource != null) {
      LocalizationContext jstlContext = new FrameworkLocalizationContext(messageSource, servletRequest, request);
      Config.set(servletRequest, Config.FMT_LOCALIZATION_CONTEXT, jstlContext);
    }
  }

  /**
   * Exposes JSTL-specific request attributes specifying locale
   * and resource bundle for JSTL's formatting and message tags,
   * using Framework's locale and MessageSource.
   *
   * @param request the context for the current HTTP request,
   * including the ApplicationContext to expose as MessageSource
   */
  public static void exposeLocalizationContext(RequestContext request, HttpServletRequest servletRequest) {
    Locale locale = null;
    TimeZone timeZone = null;
    // Determine locale to use for this RequestContext.
    LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
    if (localeResolver instanceof LocaleContextResolver) {
      LocaleContext localeContext = ((LocaleContextResolver) localeResolver).resolveLocaleContext(request);
      locale = localeContext.getLocale();
      if (localeContext instanceof TimeZoneAwareLocaleContext) {
        timeZone = ((TimeZoneAwareLocaleContext) localeContext).getTimeZone();
      }
    }
    else if (localeResolver != null) {
      // Try LocaleResolver (we're within a DispatcherServlet request).
      locale = localeResolver.resolveLocale(request);
    }

    // fallback
    if (locale == null) {
      locale = getLocale(servletRequest);
    }
    if (timeZone == null) {
      timeZone = getTimeZone(servletRequest);
    }
    if (locale != null) {
      Config.set(servletRequest, Config.FMT_LOCALE, locale);
    }
    if (timeZone != null) {
      Config.set(servletRequest, Config.FMT_TIME_ZONE, timeZone);
    }

    MessageSource messageSource = getJstlAwareMessageSource(
            servletRequest.getServletContext(), request.getWebApplicationContext());
    LocalizationContext jstlContext = new FrameworkLocalizationContext(messageSource, servletRequest, request);
    Config.set(servletRequest, Config.FMT_LOCALIZATION_CONTEXT, jstlContext);
  }

  @Nullable
  public static Locale getLocale(HttpServletRequest request) {
    Object localeObject = Config.get(request, Config.FMT_LOCALE);
    if (localeObject == null) {
      HttpSession session = request.getSession(false);
      if (session != null) {
        localeObject = Config.get(session, Config.FMT_LOCALE);
      }
      if (localeObject == null) {
        localeObject = Config.get(request.getServletContext(), Config.FMT_LOCALE);
      }
    }
    return localeObject instanceof Locale ? (Locale) localeObject : null;
  }

  @Nullable
  public static TimeZone getTimeZone(HttpServletRequest request) {
    Object timeZoneObject = Config.get(request, Config.FMT_TIME_ZONE);
    if (timeZoneObject == null) {
      HttpSession session = request.getSession(false);
      if (session != null) {
        timeZoneObject = Config.get(session, Config.FMT_TIME_ZONE);
      }
      if (timeZoneObject == null) {
        timeZoneObject = Config.get(request.getServletContext(), Config.FMT_TIME_ZONE);
      }
    }
    return timeZoneObject instanceof TimeZone ? (TimeZone) timeZoneObject : null;
  }

  /**
   * Framework-specific LocalizationContext adapter that merges session-scoped
   * JSTL LocalizationContext/Locale attributes with the local Framework request context.
   */
  private static class FrameworkLocalizationContext extends LocalizationContext {

    private final MessageSource messageSource;

    private final HttpServletRequest request;
    private final RequestContext requestContext;

    public FrameworkLocalizationContext(
            MessageSource messageSource, HttpServletRequest request, RequestContext requestContext) {
      this.request = request;
      this.messageSource = messageSource;
      this.requestContext = requestContext;
    }

    @Override
    public ResourceBundle getResourceBundle() {
      HttpSession session = this.request.getSession(false);
      if (session != null) {
        Object lcObject = Config.get(session, Config.FMT_LOCALIZATION_CONTEXT);
        if (lcObject instanceof LocalizationContext) {
          ResourceBundle lcBundle = ((LocalizationContext) lcObject).getResourceBundle();
          return new MessageSourceResourceBundle(this.messageSource, getLocale(), lcBundle);
        }
      }
      return new MessageSourceResourceBundle(this.messageSource, getLocale());
    }

    @Override
    public Locale getLocale() {
      HttpSession session = request.getSession(false);
      if (session != null) {
        Object localeObject = Config.get(session, Config.FMT_LOCALE);
        if (localeObject instanceof Locale) {
          return (Locale) localeObject;
        }
      }
      return RequestContextUtils.getLocale(requestContext);
    }
  }

}

