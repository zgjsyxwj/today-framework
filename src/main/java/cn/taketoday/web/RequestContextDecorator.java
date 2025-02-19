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

package cn.taketoday.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URI;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import cn.taketoday.core.MultiValueMap;
import cn.taketoday.http.HttpCookie;
import cn.taketoday.http.HttpHeaders;
import cn.taketoday.http.HttpStatus;
import cn.taketoday.lang.Assert;
import cn.taketoday.lang.Nullable;
import cn.taketoday.web.multipart.MultipartFile;
import cn.taketoday.web.util.pattern.PathMatchInfo;
import cn.taketoday.web.view.Model;
import cn.taketoday.web.view.ModelAndView;

/**
 * Provides a convenient implementation of the RequestContext
 * that can be subclassed by developers wishing to adapt the request to web.
 * This class implements the Wrapper or Decorator pattern.
 * Methods default to calling through to the wrapped request object.
 *
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/2/5 13:53
 */
public class RequestContextDecorator extends RequestContext {
  private final RequestContext delegate;

  public RequestContextDecorator(RequestContext delegate) {
    super(delegate.getWebApplicationContext());
    Assert.notNull(delegate, "RequestContext delegate is required");
    this.delegate = delegate;
  }

  public RequestContext getDelegate() {
    return delegate;
  }

  // delegate

  @Override
  public WebApplicationContext getWebApplicationContext() {
    return delegate.getWebApplicationContext();
  }

  @Override
  public Reader getReader(String encoding) throws IOException { return delegate.getReader(encoding); }

  @Override
  public ReadableByteChannel readableChannel() throws IOException { return delegate.readableChannel(); }

  @Override
  public WritableByteChannel writableChannel() throws IOException { return delegate.writableChannel(); }

  @Override
  public String getScheme() { return delegate.getScheme(); }

  @Override
  public String getContextPath() { return delegate.getContextPath(); }

  @Override
  public String doGetContextPath() { return delegate.doGetContextPath(); }

  @Override
  public URI getURI() { return delegate.getURI(); }

  @Override
  public String getRequestPath() { return delegate.getRequestPath(); }

  @Override
  public String doGetRequestPath() { return delegate.doGetRequestPath(); }

  @Override
  public String getRequestURL() { return delegate.getRequestURL(); }

  @Override
  public String getQueryString() { return delegate.getQueryString(); }

  @Override
  public String doGetQueryString() { return delegate.doGetQueryString(); }

  @Override
  public HttpCookie[] getCookies() { return delegate.getCookies(); }

  @Override
  public HttpCookie[] doGetCookies() { return delegate.doGetCookies(); }

  @Override
  @Nullable
  public HttpCookie getCookie(String name) { return delegate.getCookie(name); }

  @Override
  public void addCookie(HttpCookie cookie) { delegate.addCookie(cookie); }

  @Override
  public ArrayList<HttpCookie> responseCookies() { return delegate.responseCookies(); }

  @Override
  public Map<String, String[]> getParameters() { return delegate.getParameters(); }

  @Override
  public Map<String, String[]> doGetParameters() { return delegate.doGetParameters(); }

  @Override
  public void postGetParameters(MultiValueMap<String, String> parameters) { delegate.postGetParameters(parameters); }

  @Override
  public Iterator<String> getParameterNames() { return delegate.getParameterNames(); }

  @Override
  @Nullable
  public String[] getParameters(String name) { return delegate.getParameters(name); }

  @Override
  @Nullable
  public String getParameter(String name) { return delegate.getParameter(name); }

  @Override
  public String doGetMethod() { return delegate.doGetMethod(); }

  @Override
  public String remoteAddress() { return delegate.remoteAddress(); }

  @Override
  public long getContentLength() { return delegate.getContentLength(); }

  @Override
  public InputStream getBody() throws IOException { return delegate.getBody(); }

  @Override
  public HttpHeaders getHeaders() { return delegate.getHeaders(); }

  @Override
  public InputStream getInputStream() throws IOException { return delegate.getInputStream(); }

  @Override
  public InputStream doGetInputStream() throws IOException { return delegate.doGetInputStream(); }

  @Override
  public BufferedReader getReader() throws IOException { return delegate.getReader(); }

  @Override
  public BufferedReader doGetReader() throws IOException { return delegate.doGetReader(); }

  @Override
  public MultiValueMap<String, MultipartFile> multipartFiles() { return delegate.multipartFiles(); }

  @Override
  public MultiValueMap<String, MultipartFile> parseMultipartFiles() { return delegate.parseMultipartFiles(); }

  @Override
  public String getContentType() { return delegate.getContentType(); }

  @Override
  public HttpHeaders requestHeaders() { return delegate.requestHeaders(); }

  @Override
  public HttpHeaders createRequestHeaders() { return delegate.createRequestHeaders(); }

  @Override
  public Locale getLocale() { return delegate.getLocale(); }

  @Override
  public Locale doGetLocale() { return delegate.doGetLocale(); }

  @Override
  public boolean checkNotModified(long lastModifiedTimestamp) {
    return delegate.checkNotModified(lastModifiedTimestamp);
  }

  @Override
  public boolean checkNotModified(String etag) {
    return delegate.checkNotModified(etag);
  }

  @Override
  public boolean checkNotModified(@Nullable String etag, long lastModifiedTimestamp) {
    return delegate.checkNotModified(etag, lastModifiedTimestamp);
  }

  @Override
  public boolean isNotModified() {
    return delegate.isNotModified();
  }

  @Override
  public ModelAndView modelAndView() { return delegate.modelAndView(); }

  @Override
  public boolean hasModelAndView() { return delegate.hasModelAndView(); }

  @Override
  public void setContentLength(long length) { delegate.setContentLength(length); }

  @Override
  public boolean isCommitted() { return delegate.isCommitted(); }

  @Override
  public void reset() { delegate.reset(); }

  @Override
  public void sendRedirect(String location) throws IOException { delegate.sendRedirect(location); }

  @Override
  public void setStatus(int sc) { delegate.setStatus(sc); }

  @Override
  public void setStatus(int status, String message) { delegate.setStatus(status, message); }

  @Override
  public void setStatus(HttpStatus status) { delegate.setStatus(status); }

  @Override
  public int getStatus() { return delegate.getStatus(); }

  @Override
  public void sendError(int sc) throws IOException { delegate.sendError(sc); }

  @Override
  public void sendError(int sc, String msg) throws IOException { delegate.sendError(sc, msg); }

  @Override
  public OutputStream getOutputStream() throws IOException { return delegate.getOutputStream(); }

  @Override
  public OutputStream doGetOutputStream() throws IOException { return delegate.doGetOutputStream(); }

  @Override
  public PrintWriter getWriter() throws IOException { return delegate.getWriter(); }

  @Override
  public PrintWriter doGetWriter() throws IOException { return delegate.doGetWriter(); }

  @Override
  public void setContentType(String contentType) {
    delegate.setContentType(contentType);
  }

  @Nullable
  @Override
  public String getResponseContentType() {
    return delegate.getResponseContentType();
  }

  @Override
  public HttpHeaders responseHeaders() { return delegate.responseHeaders(); }

  @Override
  public void mergeToResponse(HttpHeaders headers) { delegate.mergeToResponse(headers); }

  @Override
  public HttpHeaders createResponseHeaders() { return delegate.createResponseHeaders(); }

  @Override
  public <T> T nativeRequest() { return delegate.nativeRequest(); }

  @Override
  @Nullable
  public <T> T unwrapRequest(Class<T> requestClass) { return delegate.unwrapRequest(requestClass); }

  @Override
  public <T> T nativeResponse() { return delegate.nativeResponse(); }

  @Override
  @Nullable
  public <T> T unwrapResponse(Class<T> responseClass) { return delegate.unwrapResponse(responseClass); }

  @Override
  public Object requestBody() { return delegate.requestBody(); }

  @Override
  public void setRequestBody(Object body) { delegate.setRequestBody(body); }

  @Override
  public PathMatchInfo pathMatchInfo() { return delegate.pathMatchInfo(); }

  @Override
  public String[] pathVariables() { return delegate.pathVariables(); }

  @Override
  public String[] pathVariables(String[] variables) { return delegate.pathVariables(variables); }

  @Override
  public Model getModel() {
    return delegate.getModel();
  }

  @Override
  public Model createModel() {
    return delegate.createModel();
  }

  @Override
  public boolean containsAttribute(String name) {
    return delegate.containsAttribute(name);
  }

  @Override
  public void setAttributes(Map<String, Object> attributes) {
    delegate.setAttributes(attributes);
  }

  @Override
  public Object getAttribute(String name) {
    return delegate.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    delegate.setAttribute(name, value);
  }

  @Override
  public Object removeAttribute(String name) { return delegate.removeAttribute(name); }

  @Override
  public Map<String, Object> asMap() { return delegate.asMap(); }

  @Override
  public void clear() { delegate.clear(); }

  @Override
  public String[] getAttributeNames() {
    return delegate.getAttributeNames();
  }

  @Override
  public void resetResponseHeader() { delegate.resetResponseHeader(); }

  @Override
  public void flush() throws IOException { delegate.flush(); }

  @Override
  public void cleanupMultipartFiles() { delegate.cleanupMultipartFiles(); }

  @Override
  public void setRequestHandled(boolean requestHandled) { delegate.setRequestHandled(requestHandled); }

  @Override
  public boolean isRequestHandled() { return delegate.isRequestHandled(); }

  @Override
  public String toString() {
    return "Wrapper for " + delegate;
  }

}
