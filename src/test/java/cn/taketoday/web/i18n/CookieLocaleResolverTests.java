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

package cn.taketoday.web.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.TimeZone;

import cn.taketoday.context.annotation.Configuration;
import cn.taketoday.core.i18n.LocaleContext;
import cn.taketoday.core.i18n.SimpleLocaleContext;
import cn.taketoday.core.i18n.SimpleTimeZoneAwareLocaleContext;
import cn.taketoday.core.i18n.TimeZoneAwareLocaleContext;
import cn.taketoday.web.mock.MockHttpServletRequest;
import cn.taketoday.web.mock.MockHttpServletResponse;
import cn.taketoday.web.servlet.MockServletRequestContext;
import cn.taketoday.web.servlet.StandardWebServletApplicationContext;
import cn.taketoday.web.session.EnableWebSession;
import cn.taketoday.web.util.WebUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/2/3 23:52
 */
public class CookieLocaleResolverTests {

  StandardWebServletApplicationContext webApplicationContext = new StandardWebServletApplicationContext();

  {
    webApplicationContext.register(SessionLocaleResolverTests.SessionConfig.class);
    webApplicationContext.refresh();
  }

  @EnableWebSession
  @Configuration
  static class SessionConfig {

  }

  @Test
  public void testResolveLocale() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    Cookie cookie = new Cookie("LanguageKoekje", "nl");
    request.setCookies(cookie);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setCookieName("LanguageKoekje");
    Locale loc = resolver.resolveLocale(requestContext);
    assertThat(loc.getLanguage()).isEqualTo("nl");
  }

  @Test
  public void testResolveLocaleContext() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    Cookie cookie = new Cookie("LanguageKoekje", "nl");
    request.setCookies(cookie);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setCookieName("LanguageKoekje");
    LocaleContext loc = resolver.resolveLocaleContext(requestContext);
    assertThat(loc.getLocale().getLanguage()).isEqualTo("nl");
    boolean condition = loc instanceof TimeZoneAwareLocaleContext;
    assertThat(condition).isTrue();
    assertThat(((TimeZoneAwareLocaleContext) loc).getTimeZone()).isNull();
  }

  @Test
  public void testResolveLocaleContextWithTimeZone() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    Cookie cookie = new Cookie("LanguageKoekje", "nl GMT+1");
    request.setCookies(cookie);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setCookieName("LanguageKoekje");
    LocaleContext loc = resolver.resolveLocaleContext(requestContext);
    assertThat(loc.getLocale().getLanguage()).isEqualTo("nl");
    boolean condition = loc instanceof TimeZoneAwareLocaleContext;
    assertThat(condition).isTrue();
    assertThat(((TimeZoneAwareLocaleContext) loc).getTimeZone()).isEqualTo(TimeZone.getTimeZone("GMT+1"));
  }

  @Test
  public void testResolveLocaleContextWithInvalidLocale() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    Cookie cookie = new Cookie("LanguageKoekje", "++ GMT+1");
    request.setCookies(cookie);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setCookieName("LanguageKoekje");
    assertThatIllegalStateException()
            .isThrownBy(() -> resolver.resolveLocaleContext(requestContext))
            .withMessageContaining("LanguageKoekje")
            .withMessageContaining("++ GMT+1");
  }

  @Test
  public void testResolveLocaleContextWithInvalidLocaleOnErrorDispatch() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    request.addPreferredLocale(Locale.GERMAN);
    request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, new ServletException());
    Cookie cookie = new Cookie("LanguageKoekje", "++ GMT+1");
    request.setCookies(cookie);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setDefaultTimeZone(TimeZone.getTimeZone("GMT+2"));
    resolver.setCookieName("LanguageKoekje");
    LocaleContext loc = resolver.resolveLocaleContext(requestContext);
    assertThat(loc.getLocale()).isEqualTo(Locale.GERMAN);
    boolean condition = loc instanceof TimeZoneAwareLocaleContext;
    assertThat(condition).isTrue();
    assertThat(((TimeZoneAwareLocaleContext) loc).getTimeZone()).isEqualTo(TimeZone.getTimeZone("GMT+2"));
  }

  @Test
  public void testResolveLocaleContextWithInvalidTimeZone() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    Cookie cookie = new Cookie("LanguageKoekje", "nl X-MT");
    request.setCookies(cookie);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setCookieName("LanguageKoekje");
    assertThatIllegalStateException()
            .isThrownBy(() -> resolver.resolveLocaleContext(requestContext))
            .withMessageContaining("LanguageKoekje")
            .withMessageContaining("nl X-MT");
  }

  @Test
  public void testResolveLocaleContextWithInvalidTimeZoneOnErrorDispatch() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, new ServletException());
    Cookie cookie = new Cookie("LanguageKoekje", "nl X-MT");
    request.setCookies(cookie);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setDefaultTimeZone(TimeZone.getTimeZone("GMT+2"));
    resolver.setCookieName("LanguageKoekje");
    LocaleContext loc = resolver.resolveLocaleContext(requestContext);
    assertThat(loc.getLocale().getLanguage()).isEqualTo("nl");
    boolean condition = loc instanceof TimeZoneAwareLocaleContext;
    assertThat(condition).isTrue();
    assertThat(((TimeZoneAwareLocaleContext) loc).getTimeZone()).isEqualTo(TimeZone.getTimeZone("GMT+2"));
  }

  @Test
  public void testSetAndResolveLocale() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setLocale(requestContext, new Locale("nl", ""));

    Cookie cookie = response.getCookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    assertThat(cookie).isNotNull();
    assertThat(cookie.getName()).isEqualTo(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    assertThat(cookie.getDomain()).isNull();
    assertThat(cookie.getPath()).isEqualTo(CookieLocaleResolver.DEFAULT_COOKIE_PATH);
    assertThat(cookie.getSecure()).isFalse();

    request = new MockHttpServletRequest();
    request.setCookies(cookie);

    resolver = new CookieLocaleResolver();
    Locale loc = resolver.resolveLocale(requestContext);
    assertThat(loc.getLanguage()).isEqualTo("nl");
  }

  @Test
  public void testSetAndResolveLocaleContext() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setLocaleContext(requestContext, new SimpleLocaleContext(new Locale("nl", "")));

    Cookie cookie = response.getCookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    request = new MockHttpServletRequest();
    request.setCookies(cookie);

    resolver = new CookieLocaleResolver();
    LocaleContext loc = resolver.resolveLocaleContext(requestContext);
    assertThat(loc.getLocale().getLanguage()).isEqualTo("nl");
    boolean condition = loc instanceof TimeZoneAwareLocaleContext;
    assertThat(condition).isTrue();
    assertThat(((TimeZoneAwareLocaleContext) loc).getTimeZone()).isNull();
  }

  @Test
  public void testSetAndResolveLocaleContextWithTimeZone() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setLocaleContext(requestContext,
            new SimpleTimeZoneAwareLocaleContext(new Locale("nl", ""), TimeZone.getTimeZone("GMT+1")));

    Cookie cookie = response.getCookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    request = new MockHttpServletRequest();
    request.setCookies(cookie);

    resolver = new CookieLocaleResolver();
    LocaleContext loc = resolver.resolveLocaleContext(requestContext);
    assertThat(loc.getLocale().getLanguage()).isEqualTo("nl");
    boolean condition = loc instanceof TimeZoneAwareLocaleContext;
    assertThat(condition).isTrue();
    assertThat(((TimeZoneAwareLocaleContext) loc).getTimeZone()).isEqualTo(TimeZone.getTimeZone("GMT+1"));
  }

  @Test
  public void testSetAndResolveLocaleContextWithTimeZoneOnly() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setLocaleContext(requestContext,
            new SimpleTimeZoneAwareLocaleContext(null, TimeZone.getTimeZone("GMT+1")));

    Cookie cookie = response.getCookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    request = new MockHttpServletRequest();
    request.addPreferredLocale(Locale.GERMANY);
    request.setCookies(cookie);

    requestContext = new MockServletRequestContext(request, response);

    resolver = new CookieLocaleResolver();
    LocaleContext loc = resolver.resolveLocaleContext(requestContext);
    assertThat(loc.getLocale()).isEqualTo(Locale.GERMANY);
    boolean condition = loc instanceof TimeZoneAwareLocaleContext;
    assertThat(condition).isTrue();
    assertThat(((TimeZoneAwareLocaleContext) loc).getTimeZone()).isEqualTo(TimeZone.getTimeZone("GMT+1"));
  }

  @Test
  public void testSetAndResolveLocaleWithCountry() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setLocale(requestContext, new Locale("de", "AT"));

    Cookie cookie = response.getCookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    assertThat(cookie).isNotNull();
    assertThat(cookie.getName()).isEqualTo(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    assertThat(cookie.getDomain()).isNull();
    assertThat(cookie.getPath()).isEqualTo(CookieLocaleResolver.DEFAULT_COOKIE_PATH);
    assertThat(cookie.getSecure()).isFalse();
    assertThat(cookie.getValue()).isEqualTo("de-AT");

    request = new MockHttpServletRequest();
    request.setCookies(cookie);

    resolver = new CookieLocaleResolver();
    Locale loc = resolver.resolveLocale(requestContext);
    assertThat(loc.getLanguage()).isEqualTo("de");
    assertThat(loc.getCountry()).isEqualTo("AT");
  }

  @Test
  public void testSetAndResolveLocaleWithCountryAsLegacyJava() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setLanguageTagCompliant(false);
    resolver.setLocale(requestContext, new Locale("de", "AT"));

    Cookie cookie = response.getCookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    assertThat(cookie).isNotNull();
    assertThat(cookie.getName()).isEqualTo(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    assertThat(cookie.getDomain()).isNull();
    assertThat(cookie.getPath()).isEqualTo(CookieLocaleResolver.DEFAULT_COOKIE_PATH);
    assertThat(cookie.getSecure()).isFalse();
    assertThat(cookie.getValue()).isEqualTo("de_AT");

    request = new MockHttpServletRequest();
    request.setCookies(cookie);

    resolver = new CookieLocaleResolver();
    Locale loc = resolver.resolveLocale(requestContext);
    assertThat(loc.getLanguage()).isEqualTo("de");
    assertThat(loc.getCountry()).isEqualTo("AT");
  }

  @Test
  public void testCustomCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setCookieName("LanguageKoek");
    resolver.setCookieDomain(".springframework.org");
    resolver.setCookiePath("/mypath");
    resolver.setCookieMaxAge(10000);
    resolver.setCookieSecure(true);
    resolver.setLocale(requestContext, new Locale("nl", ""));

    Cookie cookie = response.getCookie("LanguageKoek");
    assertThat(cookie).isNotNull();
    assertThat(cookie.getName()).isEqualTo("LanguageKoek");
    assertThat(cookie.getDomain()).isEqualTo(".springframework.org");
    assertThat(cookie.getPath()).isEqualTo("/mypath");
    assertThat(cookie.getMaxAge()).isEqualTo(10000);
    assertThat(cookie.getSecure()).isTrue();

    request = new MockHttpServletRequest();
    request.setCookies(cookie);

    resolver = new CookieLocaleResolver();
    resolver.setCookieName("LanguageKoek");
    Locale loc = resolver.resolveLocale(requestContext);
    assertThat(loc.getLanguage()).isEqualTo("nl");
  }

  @Test
  public void testResolveLocaleWithoutCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addPreferredLocale(Locale.TAIWAN);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();

    Locale loc = resolver.resolveLocale(requestContext);
    assertThat(loc).isEqualTo(request.getLocale());
  }

  @Test
  public void testResolveLocaleContextWithoutCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addPreferredLocale(Locale.TAIWAN);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();

    LocaleContext loc = resolver.resolveLocaleContext(requestContext);
    assertThat(loc.getLocale()).isEqualTo(request.getLocale());
    boolean condition = loc instanceof TimeZoneAwareLocaleContext;
    assertThat(condition).isTrue();
    assertThat(((TimeZoneAwareLocaleContext) loc).getTimeZone()).isNull();
  }

  @Test
  public void testResolveLocaleWithoutCookieAndDefaultLocale() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addPreferredLocale(Locale.TAIWAN);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setDefaultLocale(Locale.GERMAN);

    Locale loc = resolver.resolveLocale(requestContext);
    assertThat(loc).isEqualTo(Locale.GERMAN);
  }

  @Test
  public void testResolveLocaleContextWithoutCookieAndDefaultLocale() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addPreferredLocale(Locale.TAIWAN);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setDefaultLocale(Locale.GERMAN);
    resolver.setDefaultTimeZone(TimeZone.getTimeZone("GMT+1"));

    LocaleContext loc = resolver.resolveLocaleContext(requestContext);
    assertThat(loc.getLocale()).isEqualTo(Locale.GERMAN);
    boolean condition = loc instanceof TimeZoneAwareLocaleContext;
    assertThat(condition).isTrue();
    assertThat(((TimeZoneAwareLocaleContext) loc).getTimeZone()).isEqualTo(TimeZone.getTimeZone("GMT+1"));
  }

  @Test
  public void testResolveLocaleWithCookieWithoutLocale() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addPreferredLocale(Locale.TAIWAN);
    Cookie cookie = new Cookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME, "");
    request.setCookies(cookie);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();

    Locale loc = resolver.resolveLocale(requestContext);
    assertThat(loc).isEqualTo(request.getLocale());
  }

  @Test
  public void testResolveLocaleContextWithCookieWithoutLocale() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addPreferredLocale(Locale.TAIWAN);
    Cookie cookie = new Cookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME, "");
    request.setCookies(cookie);
    MockServletRequestContext requestContext = new MockServletRequestContext(request, null);

    CookieLocaleResolver resolver = new CookieLocaleResolver();

    LocaleContext loc = resolver.resolveLocaleContext(requestContext);
    assertThat(loc.getLocale()).isEqualTo(request.getLocale());
    boolean condition = loc instanceof TimeZoneAwareLocaleContext;
    assertThat(condition).isTrue();
    assertThat(((TimeZoneAwareLocaleContext) loc).getTimeZone()).isNull();
  }

  @Test
  public void testSetLocaleToNull() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    request.addPreferredLocale(Locale.TAIWAN);
    Cookie cookie = new Cookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME, Locale.UK.toString());
    request.setCookies(cookie);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setLocale(requestContext, null);
    Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.LOCALE_REQUEST_ATTRIBUTE_NAME);
    assertThat(locale).isEqualTo(Locale.TAIWAN);

    Cookie[] cookies = response.getCookies();
    assertThat(cookies.length).isEqualTo(1);
    Cookie localeCookie = cookies[0];
    assertThat(localeCookie.getName()).isEqualTo(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    assertThat(localeCookie.getValue()).isEqualTo("");
  }

  @Test
  public void testSetLocaleContextToNull() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addPreferredLocale(Locale.TAIWAN);
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    Cookie cookie = new Cookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME, Locale.UK.toString());
    request.setCookies(cookie);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setLocaleContext(requestContext, null);
    Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.LOCALE_REQUEST_ATTRIBUTE_NAME);
    assertThat(locale).isEqualTo(Locale.TAIWAN);
    TimeZone timeZone = (TimeZone) request.getAttribute(CookieLocaleResolver.TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
    assertThat(timeZone).isNull();

    Cookie[] cookies = response.getCookies();
    assertThat(cookies.length).isEqualTo(1);
    Cookie localeCookie = cookies[0];
    assertThat(localeCookie.getName()).isEqualTo(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    assertThat(localeCookie.getValue()).isEqualTo("");
  }

  @Test
  public void testSetLocaleToNullWithDefault() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addPreferredLocale(Locale.TAIWAN);
    Cookie cookie = new Cookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME, Locale.UK.toString());
    request.setCookies(cookie);
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setDefaultLocale(Locale.CANADA_FRENCH);
    resolver.setLocale(requestContext, null);
    Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.LOCALE_REQUEST_ATTRIBUTE_NAME);
    assertThat(locale).isEqualTo(Locale.CANADA_FRENCH);

    Cookie[] cookies = response.getCookies();
    assertThat(cookies.length).isEqualTo(1);
    Cookie localeCookie = cookies[0];
    assertThat(localeCookie.getName()).isEqualTo(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    assertThat(localeCookie.getValue()).isEqualTo("");
  }

  @Test
  public void testSetLocaleContextToNullWithDefault() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    MockServletRequestContext requestContext = new MockServletRequestContext(request, response);

    request.addPreferredLocale(Locale.TAIWAN);
    Cookie cookie = new Cookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME, Locale.UK.toString());
    request.setCookies(cookie);

    CookieLocaleResolver resolver = new CookieLocaleResolver();
    resolver.setDefaultLocale(Locale.CANADA_FRENCH);
    resolver.setDefaultTimeZone(TimeZone.getTimeZone("GMT+1"));
    resolver.setLocaleContext(requestContext, null);
    Locale locale = (Locale) request.getAttribute(CookieLocaleResolver.LOCALE_REQUEST_ATTRIBUTE_NAME);
    assertThat(locale).isEqualTo(Locale.CANADA_FRENCH);
    TimeZone timeZone = (TimeZone) request.getAttribute(CookieLocaleResolver.TIME_ZONE_REQUEST_ATTRIBUTE_NAME);
    assertThat(timeZone).isEqualTo(TimeZone.getTimeZone("GMT+1"));

    Cookie[] cookies = response.getCookies();
    assertThat(cookies.length).isEqualTo(1);
    Cookie localeCookie = cookies[0];
    assertThat(localeCookie.getName()).isEqualTo(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
    assertThat(localeCookie.getValue()).isEqualTo("");
  }

}
