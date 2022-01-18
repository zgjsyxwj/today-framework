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

package cn.taketoday.web.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import cn.taketoday.web.mock.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Unit tests for {@link UrlPathHelper}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Costin Leau
 */
class UrlPathHelperTests {

  private final UrlPathHelper helper = new UrlPathHelper();

  private final MockHttpServletRequest request = new MockHttpServletRequest();

  @Test
  void getPathWithinApplication() {
    request.setContextPath("/petclinic");
    request.setRequestURI("/petclinic/welcome.html");

    assertThat(helper.getPathWithinApplication(request)).isEqualTo("/welcome.html");
  }

  @Test
  void getPathWithinApplicationForRootWithNoLeadingSlash() {
    request.setContextPath("/petclinic");
    request.setRequestURI("/petclinic");

    assertThat(helper.getPathWithinApplication(request)).as("Incorrect root path returned").isEqualTo("/");
  }

  @Test
  void getPathWithinApplicationForSlashContextPath() {
    request.setContextPath("/");
    request.setRequestURI("/welcome.html");

    assertThat(helper.getPathWithinApplication(request)).isEqualTo("/welcome.html");
  }

  @Test
  void getPathWithinServlet() {
    request.setContextPath("/petclinic");
    request.setServletPath("/main");
    request.setRequestURI("/petclinic/main/welcome.html");

    assertThat(helper.getPathWithinServletMapping(request)).isEqualTo("/welcome.html");
  }

  @Test
  void alwaysUseFullPath() {
    helper.setAlwaysUseFullPath(true);
    request.setContextPath("/petclinic");
    request.setServletPath("/main");
    request.setRequestURI("/petclinic/main/welcome.html");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/main/welcome.html");
  }

  @Test
    // SPR-11101
  void getPathWithinServletWithoutUrlDecoding() {
    request.setContextPath("/SPR-11101");
    request.setServletPath("/test_url_decoding/a/b");
    request.setRequestURI("/test_url_decoding/a%2Fb");

    helper.setUrlDecode(false);
    String actual = helper.getPathWithinServletMapping(request);

    assertThat(actual).isEqualTo("/test_url_decoding/a%2Fb");
  }

  @Test
  void getRequestUri() {
    request.setRequestURI("/welcome.html");
    assertThat(helper.getRequestUri(request)).isEqualTo("/welcome.html");

    request.setRequestURI("/foo%20bar");
    assertThat(helper.getRequestUri(request)).isEqualTo("/foo bar");

    request.setRequestURI("/foo+bar");
    assertThat(helper.getRequestUri(request)).isEqualTo("/foo+bar");

    request.setRequestURI("/home/" + "/path");
    assertThat(helper.getRequestUri(request)).isEqualTo("/home/path");
  }

  @Test
  void getRequestRemoveSemicolonContent() {
    helper.setRemoveSemicolonContent(true);
    request.setRequestURI("/foo;f=F;o=O;o=O/bar;b=B;a=A;r=R");
    assertThat(helper.getRequestUri(request)).isEqualTo("/foo/bar");

    request.setRequestURI("/foo;f=F;o=O;o=O/bar;b=B;a=A;r=R/baz;test");
    assertThat(helper.getRequestUri(request)).isEqualTo("/foo/bar/baz");

    // SPR-13455
    request.setRequestURI("/foo/;test/1");
    request.setServletPath("/foo/1");
    assertThat(helper.getRequestUri(request)).isEqualTo("/foo/1");
  }

  @Test
  void getRequestKeepSemicolonContent() {
    helper.setRemoveSemicolonContent(false);

    testKeepSemicolonContent("/foo;a=b;c=d", "/foo;a=b;c=d");
    testKeepSemicolonContent("/test;jsessionid=1234", "/test");
    testKeepSemicolonContent("/test;JSESSIONID=1234", "/test");
    testKeepSemicolonContent("/test;jsessionid=1234;a=b", "/test;a=b");
    testKeepSemicolonContent("/test;a=b;jsessionid=1234;c=d", "/test;a=b;c=d");
    testKeepSemicolonContent("/test;jsessionid=1234/anotherTest", "/test/anotherTest");
    testKeepSemicolonContent("/test;jsessionid=;a=b", "/test;a=b");
    testKeepSemicolonContent("/somethingLongerThan12;jsessionid=1234", "/somethingLongerThan12");
  }

  private void testKeepSemicolonContent(String requestUri, String expectedPath) {
    request.setRequestURI(requestUri);
    assertThat(helper.getRequestUri(request)).isEqualTo(expectedPath);
  }

  @Test
  void getLookupPathWithSemicolonContent() {
    helper.setRemoveSemicolonContent(false);

    request.setContextPath("/petclinic");
    request.setServletPath("/main");
    request.setRequestURI("/petclinic;a=b/main;b=c/welcome.html;c=d");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/welcome.html;c=d");
  }

  @Test
  void getLookupPathWithSemicolonContentAndNullPathInfo() {
    helper.setRemoveSemicolonContent(false);

    request.setContextPath("/petclinic");
    request.setServletPath("/welcome.html");
    request.setRequestURI("/petclinic;a=b/welcome.html;c=d");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/welcome.html;c=d");
  }

  @Test
    // gh-27303
  void defaultInstanceReadOnlyBehavior() {
    UrlPathHelper helper = UrlPathHelper.defaultInstance;

    assertThatIllegalArgumentException()
            .isThrownBy(() -> helper.setAlwaysUseFullPath(true))
            .withMessage("This instance cannot be modified");
    assertThatIllegalArgumentException()
            .isThrownBy(() -> helper.setUrlDecode(true))
            .withMessage("This instance cannot be modified");
    assertThatIllegalArgumentException()
            .isThrownBy(() -> helper.setRemoveSemicolonContent(true))
            .withMessage("This instance cannot be modified");
    assertThatIllegalArgumentException()
            .isThrownBy(() -> helper.setDefaultEncoding("UTF-8"))
            .withMessage("This instance cannot be modified");

    assertThat(helper.isUrlDecode()).isTrue();
    assertThat(helper.shouldRemoveSemicolonContent()).isTrue();
  }

  //
  // suite of tests root requests for default servlets (SRV 11.2) on Websphere vs Tomcat and other containers
  // see: https://jira.springframework.org/browse/SPR-7064
  //

  //
  // / mapping (default servlet)
  //

  @Test
  void tomcatDefaultServletRoot() {
    request.setContextPath("/test");
    request.setServletPath("/");
    request.setPathInfo(null);
    request.setRequestURI("/test/");
    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/");
  }

  @Test
  void tomcatDefaultServletFile() {
    request.setContextPath("/test");
    request.setServletPath("/foo");
    request.setPathInfo(null);
    request.setRequestURI("/test/foo");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo");
  }

  @Test
  void tomcatDefaultServletFolder() {
    request.setContextPath("/test");
    request.setServletPath("/foo/");
    request.setPathInfo(null);
    request.setRequestURI("/test/foo/");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo/");
  }

  @Test
    //SPR-12372, SPR-13455
  void removeDuplicateSlashesInPath() {
    request.setContextPath("/SPR-12372");
    request.setPathInfo(null);
    request.setServletPath("/foo/bar/");
    request.setRequestURI("/SPR-12372/foo//bar/");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo/bar/");

    request.setServletPath("/foo/bar/");
    request.setRequestURI("/SPR-12372/foo/bar//");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo/bar/");

    // "normal" case
    request.setServletPath("/foo/bar//");
    request.setRequestURI("/SPR-12372/foo/bar//");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo/bar//");
  }

  @Test
  void wasDefaultServletRoot() {
    request.setContextPath("/test");
    request.setPathInfo("/");
    request.setServletPath("");
    request.setRequestURI("/test/");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/");
  }

  @Test
  void wasDefaultServletRootWithCompliantSetting() {
    tomcatDefaultServletRoot();
  }

  @Test
  void wasDefaultServletFile() {
    request.setContextPath("/test");
    request.setPathInfo("/foo");
    request.setServletPath("");
    request.setRequestURI("/test/foo");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo");
  }

  @Test
  void wasDefaultServletFileWithCompliantSetting() {
    tomcatDefaultServletFile();
  }

  @Test
  void wasDefaultServletFolder() {
    request.setContextPath("/test");
    request.setPathInfo("/foo/");
    request.setServletPath("");
    request.setRequestURI("/test/foo/");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo/");
  }

  @Test
  void wasDefaultServletFolderWithCompliantSetting() {
    tomcatDefaultServletFolder();
  }

  //
  // /foo/* mapping
  //

  @Test
  void tomcatCasualServletRoot() {
    request.setContextPath("/test");
    request.setPathInfo("/");
    request.setServletPath("/foo");
    request.setRequestURI("/test/foo/");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/");
  }

  @Disabled
  @Test
    // test the root mapping for /foo/* w/o a trailing slash - <host>/<context>/foo
  void tomcatCasualServletRootWithMissingSlash() {
    request.setContextPath("/test");
    request.setPathInfo(null);
    request.setServletPath("/foo");
    request.setRequestURI("/test/foo");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/");
  }

  @Test
  void tomcatCasualServletFile() {
    request.setContextPath("/test");
    request.setPathInfo("/foo");
    request.setServletPath("/foo");
    request.setRequestURI("/test/foo/foo");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo");
  }

  @Test
  void tomcatCasualServletFolder() {
    request.setContextPath("/test");
    request.setPathInfo("/foo/");
    request.setServletPath("/foo");
    request.setRequestURI("/test/foo/foo/");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo/");
  }

  @Test
  void wasCasualServletRootWithCompliantSetting() {
    tomcatCasualServletRoot();
  }

  @Disabled
  @Test
    // test the root mapping for /foo/* w/o a trailing slash - <host>/<context>/foo
  void wasCasualServletRootWithMissingSlash() {
    request.setContextPath("/test");
    request.setPathInfo(null);
    request.setServletPath("/foo");
    request.setRequestURI("/test/foo");
    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/");
  }

  @Disabled
  @Test
  void wasCasualServletRootWithMissingSlashWithCompliantSetting() {
    tomcatCasualServletRootWithMissingSlash();
  }

  @Test
  void wasCasualServletFile() {
    request.setContextPath("/test");
    request.setPathInfo("/foo");
    request.setServletPath("/foo");
    request.setRequestURI("/test/foo/foo");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo");
  }

  @Test
  void wasCasualServletFileWithCompliantSetting() {
    tomcatCasualServletFile();
  }

  @Test
  void wasCasualServletFolder() {
    request.setContextPath("/test");
    request.setPathInfo("/foo/");
    request.setServletPath("/foo");
    request.setRequestURI("/test/foo/foo/");

    assertThat(helper.getLookupPathForRequest(request)).isEqualTo("/foo/");
  }

  @Test
  void wasCasualServletFolderWithCompliantSetting() {
    tomcatCasualServletFolder();
  }

  @Test
  void getOriginatingRequestUri() {
    request.setAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE, "/path");
    request.setRequestURI("/forwarded");
    assertThat(helper.getOriginatingRequestUri(request)).isEqualTo("/path");
  }

  @Test
  void getOriginatingRequestUriDefault() {
    request.setRequestURI("/forwarded");
    assertThat(helper.getOriginatingRequestUri(request)).isEqualTo("/forwarded");
  }

  @Test
  void getOriginatingQueryString() {
    request.setQueryString("forward=on");
    request.setAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE, "/path");
    request.setAttribute(WebUtils.FORWARD_QUERY_STRING_ATTRIBUTE, "original=on");
    assertThat(this.helper.getOriginatingQueryString(request)).isEqualTo("original=on");
  }

  @Test
  void getOriginatingQueryStringNotPresent() {
    request.setQueryString("forward=true");
    assertThat(this.helper.getOriginatingQueryString(request)).isEqualTo("forward=true");
  }

  @Test
  void getOriginatingQueryStringIsNull() {
    request.setQueryString("forward=true");
    request.setAttribute(WebUtils.FORWARD_REQUEST_URI_ATTRIBUTE, "/path");
    assertThat(this.helper.getOriginatingQueryString(request)).isNull();
  }

}
