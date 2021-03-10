/*
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
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

package cn.taketoday.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import cn.taketoday.web.multipart.MultipartFile;
import cn.taketoday.web.ui.Model;
import cn.taketoday.web.ui.RedirectModel;

/**
 * @author TODAY 2021/3/10 16:35
 */
public class MockRequestContext extends AbstractRequestContext {

  @Override
  protected InputStream getInputStreamInternal() throws IOException {
    return null;
  }

  @Override
  protected OutputStream getOutputStreamInternal() throws IOException {
    return null;
  }

  @Override
  protected HttpCookie[] getCookiesInternal() {
    return new HttpCookie[0];
  }

  @Override
  protected Map<String, List<MultipartFile>> parseMultipartFiles() throws IOException {
    return null;
  }

  @Override
  public String requestHeader(String name) {
    return null;
  }

  @Override
  public Enumeration<String> requestHeaders(String name) {
    return null;
  }

  @Override
  public Enumeration<String> requestHeaderNames() {
    return null;
  }

  @Override
  public int requestIntHeader(String name) {
    return 0;
  }

  @Override
  public long requestDateHeader(String name) {
    return 0;
  }

  @Override
  public String contentType() {
    return null;
  }

  @Override
  public String responseHeader(String name) {
    return null;
  }

  @Override
  public Collection<String> responseHeaders(String name) {
    return null;
  }

  @Override
  public Collection<String> responseHeaderNames() {
    return null;
  }

  @Override public HttpHeaders responseHeader(String name, String value) {
    return null;
  }

  @Override public HttpHeaders addResponseHeader(String name, String value) {
    return null;
  }

  @Override public HttpHeaders responseDateHeader(String name, long date) {
    return null;
  }

  @Override public HttpHeaders addResponseDateHeader(String name, long date) {
    return null;
  }

  @Override public HttpHeaders responseIntHeader(String name, int value) {
    return null;
  }

  @Override public HttpHeaders addResponseIntHeader(String name, int value) {
    return null;
  }

  @Override public HttpHeaders contentType(String contentType) {
    return null;
  }

  @Override public String requestURI() {
    return null;
  }

  @Override public String requestURL() {
    return null;
  }

  @Override public String queryString() {
    return null;
  }

  @Override public RequestContext addCookie(HttpCookie cookie) {
    return null;
  }

  @Override public Map<String, String[]> parameters() {
    return null;
  }

  @Override public Enumeration<String> parameterNames() {
    return null;
  }

  @Override public String[] parameters(String name) {
    return new String[0];
  }

  @Override public String parameter(String name) {
    return null;
  }

  @Override public String method() {
    return null;
  }

  @Override public String remoteAddress() {
    return null;
  }

  @Override public long contentLength() {
    return 0;
  }

  @Override public RequestContext contentLength(long length) {
    return null;
  }

  @Override public boolean committed() {
    return false;
  }

  @Override public RequestContext reset() {
    return null;
  }

  @Override public RequestContext redirect(String location) throws IOException {
    return null;
  }

  @Override public RequestContext status(int sc) {
    return null;
  }

  @Override
  public RequestContext status(int status, String message) {
    return null;
  }

  @Override
  public int status() {
    return 0;
  }

  @Override
  public RequestContext sendError(int sc) throws IOException {
    return null;
  }

  @Override
  public RequestContext sendError(int sc, String msg) throws IOException {
    return null;
  }

  @Override
  public <T> T nativeSession() {
    return null;
  }

  @Override
  public <T> T nativeSession(Class<T> sessionClass) {
    return null;
  }

  @Override
  public <T> T nativeRequest() {
    return null;
  }

  @Override
  public <T> T nativeRequest(Class<T> requestClass) {
    return null;
  }

  @Override
  public <T> T nativeResponse() {
    return null;
  }

  @Override
  public <T> T nativeResponse(Class<T> responseClass) {
    return null;
  }

  @Override
  public RedirectModel redirectModel() {
    return null;
  }

  @Override
  public RedirectModel applyRedirectModel(RedirectModel redirectModel) {
    return null;
  }

  @Override
  public Model attributes(Map<String, Object> attributes) {
    return null;
  }

  @Override
  public Enumeration<String> attributes() {
    return null;
  }

  @Override
  public Object attribute(String name) {
    return null;
  }

  @Override
  public <T> T attribute(String name, Class<T> targetClass) {
    return null;
  }

  @Override
  public Model attribute(String name, Object value) {
    return null;
  }

  @Override
  public Model removeAttribute(String name) {
    return null;
  }

  @Override
  public Map<String, Object> asMap() {
    return null;
  }

  @Override
  public void clear() {

  }

  @Override
  public void flush() throws IOException {

  }

  @Override
  public String toString() {
    return "Mock Request context";
  }
}
