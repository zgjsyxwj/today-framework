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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.taketoday.web.resource;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import cn.taketoday.core.io.ClassPathResource;
import cn.taketoday.core.io.FileSystemResource;
import cn.taketoday.core.io.UrlResource;
import cn.taketoday.web.context.support.AnnotationConfigWebApplicationContext;
import cn.taketoday.web.servlet.DispatcherServlet;
import cn.taketoday.web.servlet.config.annotation.EnableWebMvc;
import cn.taketoday.web.servlet.config.annotation.PathMatchConfigurer;
import cn.taketoday.web.servlet.config.annotation.ResourceHandlerRegistry;
import cn.taketoday.web.servlet.config.annotation.WebMvcConfigurer;
import cn.taketoday.web.testfixture.servlet.MockHttpServletRequest;
import cn.taketoday.web.testfixture.servlet.MockHttpServletResponse;
import cn.taketoday.web.testfixture.servlet.MockServletConfig;
import cn.taketoday.web.testfixture.servlet.MockServletContext;
import cn.taketoday.web.util.UriUtils;
import cn.taketoday.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import jakarta.servlet.ServletException;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integration tests for static resource handling.
 *
 * @author Rossen Stoyanchev
 */
public class ResourceHttpRequestHandlerIntegrationTests {

	private final MockServletContext servletContext = new MockServletContext();

	private final MockServletConfig servletConfig = new MockServletConfig(this.servletContext);


	public static Stream<Arguments> argumentSource() {
		return Stream.of(
				arguments(true, "/cp"),
				arguments(true, "/fs"),
				arguments(true, "/url"),
				arguments(false, "/cp"),
				arguments(false, "/fs"),
				arguments(false, "/url")
		);
	}


	@ParameterizedTest
	@MethodSource("argumentSource")
	void cssFile(boolean usePathPatterns, String pathPrefix) throws Exception {
		MockHttpServletRequest request = initRequest(pathPrefix + "/test/foo.css");
		MockHttpServletResponse response = new MockHttpServletResponse();

		DispatcherServlet servlet = initDispatcherServlet(usePathPatterns, WebConfig.class);
		servlet.service(request, response);

		String description = "usePathPattern=" + usePathPatterns + ", prefix=" + pathPrefix;
		assertThat(response.getStatus()).as(description).isEqualTo(200);
		assertThat(response.getContentType()).as(description).isEqualTo("text/css");
		assertThat(response.getContentAsString()).as(description).isEqualTo("h1 { color:red; }");
	}

	@ParameterizedTest
	@MethodSource("argumentSource")
	void classpathLocationWithEncodedPath(boolean usePathPatterns, String pathPrefix) throws Exception {
		MockHttpServletRequest request = initRequest(pathPrefix + "/test/foo with spaces.css");
		MockHttpServletResponse response = new MockHttpServletResponse();

		DispatcherServlet servlet = initDispatcherServlet(usePathPatterns, WebConfig.class);
		servlet.service(request, response);

		String description = "usePathPattern=" + usePathPatterns + ", prefix=" + pathPrefix;
		assertThat(response.getStatus()).as(description).isEqualTo(200);
		assertThat(response.getContentType()).as(description).isEqualTo("text/css");
		assertThat(response.getContentAsString()).as(description).isEqualTo("h1 { color:red; }");
	}

	private DispatcherServlet initDispatcherServlet(boolean usePathPatterns, Class<?>... configClasses)
			throws ServletException {

		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(configClasses);
		if (usePathPatterns) {
			context.register(PathPatternParserConfig.class);
		}
		context.setServletConfig(this.servletConfig);
		context.refresh();

		DispatcherServlet servlet = new DispatcherServlet();
		servlet.setApplicationContext(context);
		servlet.init(this.servletConfig);
		return servlet;
	}

	private MockHttpServletRequest initRequest(String path) {
		path = UriUtils.encodePath(path, StandardCharsets.UTF_8);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
		request.setCharacterEncoding(StandardCharsets.UTF_8.name());
		return request;
	}


	@EnableWebMvc
	static class WebConfig implements WebMvcConfigurer {

		@Override
		public void addResourceHandlers(ResourceHandlerRegistry registry) {
			ClassPathResource classPathLocation = new ClassPathResource("", getClass());
			String path = getPath(classPathLocation);

			registerClasspathLocation("/cp/**", classPathLocation, registry);
			registerFileSystemLocation("/fs/**", path, registry);
			registerUrlLocation("/url/**", "file:" + path, registry);
		}

		protected void registerClasspathLocation(String pattern, ClassPathResource resource, ResourceHandlerRegistry registry) {
			registry.addResourceHandler(pattern).addResourceLocations(resource);
		}

		protected void registerFileSystemLocation(String pattern, String path, ResourceHandlerRegistry registry) {
			FileSystemResource fileSystemLocation = new FileSystemResource(path);
			registry.addResourceHandler(pattern).addResourceLocations(fileSystemLocation);
		}

		protected void registerUrlLocation(String pattern, String path, ResourceHandlerRegistry registry) {
			try {
				UrlResource urlLocation = new UrlResource(path);
				registry.addResourceHandler(pattern).addResourceLocations(urlLocation);
			}
			catch (MalformedURLException ex) {
				throw new IllegalStateException(ex);
			}
		}

		private String getPath(ClassPathResource resource) {
			try {
				return resource.getFile().getCanonicalPath().replace('\\', '/').replace("classes/java", "resources") + "/";
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}
	}


	static class PathPatternParserConfig implements WebMvcConfigurer {

		@Override
		public void configurePathMatch(PathMatchConfigurer configurer) {
			configurer.setPatternParser(new PathPatternParser());
		}
	}

}
