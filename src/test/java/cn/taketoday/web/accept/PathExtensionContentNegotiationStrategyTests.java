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

package cn.taketoday.web.accept;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.taketoday.http.MediaType;
import cn.taketoday.web.HttpMediaTypeNotAcceptableException;
import cn.taketoday.web.RequestContext;
import cn.taketoday.web.mock.MockHttpServletRequest;
import cn.taketoday.web.servlet.ServletRequestContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/2/7 14:03
 */
class PathExtensionContentNegotiationStrategyTests {

  private final MockHttpServletRequest servletRequest = new MockHttpServletRequest();

  private final RequestContext webRequest = new ServletRequestContext(null, servletRequest, null);

  private PathExtensionContentNegotiationStrategy strategy = new PathExtensionContentNegotiationStrategy();

  @Test
  void resolveMediaTypesFromMapping() throws Exception {
    this.servletRequest.setRequestURI("test.html");

    List<MediaType> mediaTypes = this.strategy.resolveMediaTypes(this.webRequest);

    assertThat(mediaTypes).isEqualTo(Arrays.asList(new MediaType("text", "html")));

    Map<String, MediaType> mapping = Collections.singletonMap("HTML", MediaType.APPLICATION_XHTML_XML);
    this.strategy = new PathExtensionContentNegotiationStrategy(mapping);
    mediaTypes = this.strategy.resolveMediaTypes(this.webRequest);

    assertThat(mediaTypes).isEqualTo(Arrays.asList(new MediaType("application", "xhtml+xml")));
  }

  @Test
  void resolveMediaTypesFromMediaTypeFactory() throws Exception {
    this.servletRequest.setRequestURI("test.xls");

    List<MediaType> mediaTypes = this.strategy.resolveMediaTypes(this.webRequest);

    assertThat(mediaTypes).isEqualTo(Arrays.asList(new MediaType("application", "vnd.ms-excel")));
  }

  @Test
    // SPR-8678
  void getMediaTypeFilenameWithContextPath() throws Exception {
    this.servletRequest.setContextPath("/project-1.0.0.M3");
    this.servletRequest.setRequestURI("/project-1.0.0.M3/");
    assertThat(this.strategy.resolveMediaTypes(webRequest)).as("Context path should be excluded").isEqualTo(ContentNegotiationStrategy.MEDIA_TYPE_ALL_LIST);

    this.servletRequest.setRequestURI("/project-1.0.0.M3");
    assertThat(this.strategy.resolveMediaTypes(webRequest)).as("Context path should be excluded").isEqualTo(ContentNegotiationStrategy.MEDIA_TYPE_ALL_LIST);
  }

  @Test
    // SPR-9390
  void getMediaTypeFilenameWithEncodedURI() throws Exception {
    this.servletRequest.setRequestURI("/quo%20vadis%3f.html");
    List<MediaType> result = this.strategy.resolveMediaTypes(webRequest);

    assertThat(result).as("Invalid content type").isEqualTo(Collections.singletonList(new MediaType("text", "html")));
  }

  @Test
    // SPR-10170
  void resolveMediaTypesIgnoreUnknownExtension() throws Exception {
    this.servletRequest.setRequestURI("test.foobar");

    List<MediaType> mediaTypes = this.strategy.resolveMediaTypes(this.webRequest);

    assertThat(mediaTypes).isEqualTo(ContentNegotiationStrategy.MEDIA_TYPE_ALL_LIST);
  }

  @Test
  void resolveMediaTypesDoNotIgnoreUnknownExtension() {
    this.servletRequest.setRequestURI("test.foobar");

    this.strategy.setIgnoreUnknownExtensions(false);
    assertThatExceptionOfType(HttpMediaTypeNotAcceptableException.class)
            .isThrownBy(() -> this.strategy.resolveMediaTypes(this.webRequest));
  }

}
