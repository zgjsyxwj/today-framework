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

package cn.taketoday.web.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.taketoday.lang.Constant;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import cn.taketoday.http.HttpMethod;
import cn.taketoday.lang.Nullable;
import cn.taketoday.web.util.WebUtils;

/**
 * {@link HttpServletRequest} wrapper that caches all content read from
 * the {@linkplain #getInputStream() input stream} and {@linkplain #getReader() reader},
 * and allows this content to be retrieved via a {@link #getContentAsByteArray() byte array}.
 *
 * <p>This class acts as an interceptor that only caches content as it is being
 * read but otherwise does not cause content to be read. That means if the request
 * content is not consumed, then the content is not cached, and cannot be
 * retrieved via {@link #getContentAsByteArray()}.
 *
 * @author Juergen Hoeller
 * @author Brian Clozel
 * @see ContentCachingResponseWrapper
 * @since 4.0
 */
public class ContentCachingRequestWrapper extends HttpServletRequestWrapper {

  private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

  private final ByteArrayOutputStream cachedContent;

  @Nullable
  private final Integer contentCacheLimit;

  @Nullable
  private ServletInputStream inputStream;

  @Nullable
  private BufferedReader reader;

  /**
   * Create a new ContentCachingRequestWrapper for the given servlet request.
   *
   * @param request the original servlet request
   */
  public ContentCachingRequestWrapper(HttpServletRequest request) {
    super(request);
    int contentLength = request.getContentLength();
    this.cachedContent = new ByteArrayOutputStream(contentLength >= 0 ? contentLength : 1024);
    this.contentCacheLimit = null;
  }

  /**
   * Create a new ContentCachingRequestWrapper for the given servlet request.
   *
   * @param request the original servlet request
   * @param contentCacheLimit the maximum number of bytes to cache per request
   * @see #handleContentOverflow(int)
   */
  public ContentCachingRequestWrapper(HttpServletRequest request, int contentCacheLimit) {
    super(request);
    this.cachedContent = new ByteArrayOutputStream(contentCacheLimit);
    this.contentCacheLimit = contentCacheLimit;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (this.inputStream == null) {
      this.inputStream = new ContentCachingInputStream(getRequest().getInputStream());
    }
    return this.inputStream;
  }

  @Override
  public String getCharacterEncoding() {
    String enc = super.getCharacterEncoding();
    return (enc != null ? enc : Constant.DEFAULT_ENCODING);
  }

  @Override
  public BufferedReader getReader() throws IOException {
    if (this.reader == null) {
      this.reader = new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }
    return this.reader;
  }

  @Override
  public String getParameter(String name) {
    if (this.cachedContent.size() == 0 && isFormPost()) {
      writeRequestParametersToCachedContent();
    }
    return super.getParameter(name);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    if (this.cachedContent.size() == 0 && isFormPost()) {
      writeRequestParametersToCachedContent();
    }
    return super.getParameterMap();
  }

  @Override
  public Enumeration<String> getParameterNames() {
    if (this.cachedContent.size() == 0 && isFormPost()) {
      writeRequestParametersToCachedContent();
    }
    return super.getParameterNames();
  }

  @Override
  public String[] getParameterValues(String name) {
    if (this.cachedContent.size() == 0 && isFormPost()) {
      writeRequestParametersToCachedContent();
    }
    return super.getParameterValues(name);
  }

  private boolean isFormPost() {
    String contentType = getContentType();
    return (contentType != null && contentType.contains(FORM_CONTENT_TYPE) &&
            HttpMethod.POST.matches(getMethod()));
  }

  private void writeRequestParametersToCachedContent() {
    try {
      if (this.cachedContent.size() == 0) {
        String requestEncoding = getCharacterEncoding();
        Map<String, String[]> form = super.getParameterMap();
        for (Iterator<String> nameIterator = form.keySet().iterator(); nameIterator.hasNext(); ) {
          String name = nameIterator.next();
          List<String> values = Arrays.asList(form.get(name));
          for (Iterator<String> valueIterator = values.iterator(); valueIterator.hasNext(); ) {
            String value = valueIterator.next();
            this.cachedContent.write(URLEncoder.encode(name, requestEncoding).getBytes());
            if (value != null) {
              this.cachedContent.write('=');
              this.cachedContent.write(URLEncoder.encode(value, requestEncoding).getBytes());
              if (valueIterator.hasNext()) {
                this.cachedContent.write('&');
              }
            }
          }
          if (nameIterator.hasNext()) {
            this.cachedContent.write('&');
          }
        }
      }
    }
    catch (IOException ex) {
      throw new IllegalStateException("Failed to write request parameters to cached content", ex);
    }
  }

  /**
   * Return the cached request content as a byte array.
   * <p>The returned array will never be larger than the content cache limit.
   * <p><strong>Note:</strong> The byte array returned from this method
   * reflects the amount of content that has has been read at the time when it
   * is called. If the application does not read the content, this method
   * returns an empty array.
   *
   * @see #ContentCachingRequestWrapper(HttpServletRequest, int)
   */
  public byte[] getContentAsByteArray() {
    return this.cachedContent.toByteArray();
  }

  /**
   * Template method for handling a content overflow: specifically, a request
   * body being read that exceeds the specified content cache limit.
   * <p>The default implementation is empty. Subclasses may override this to
   * throw a payload-too-large exception or the like.
   *
   * @param contentCacheLimit the maximum number of bytes to cache per request
   * which has just been exceeded
   * @see #ContentCachingRequestWrapper(HttpServletRequest, int)
   */
  protected void handleContentOverflow(int contentCacheLimit) { }

  private class ContentCachingInputStream extends ServletInputStream {

    private final ServletInputStream is;

    private boolean overflow = false;

    public ContentCachingInputStream(ServletInputStream is) {
      this.is = is;
    }

    @Override
    public int read() throws IOException {
      int ch = this.is.read();
      if (ch != -1 && !this.overflow) {
        if (contentCacheLimit != null && cachedContent.size() == contentCacheLimit) {
          this.overflow = true;
          handleContentOverflow(contentCacheLimit);
        }
        else {
          cachedContent.write(ch);
        }
      }
      return ch;
    }

    @Override
    public int read(byte[] b) throws IOException {
      int count = this.is.read(b);
      writeToCache(b, 0, count);
      return count;
    }

    private void writeToCache(final byte[] b, final int off, int count) {
      if (!this.overflow && count > 0) {
        if (contentCacheLimit != null &&
                count + cachedContent.size() > contentCacheLimit) {
          this.overflow = true;
          cachedContent.write(b, off, contentCacheLimit - cachedContent.size());
          handleContentOverflow(contentCacheLimit);
          return;
        }
        cachedContent.write(b, off, count);
      }
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
      int count = this.is.read(b, off, len);
      writeToCache(b, off, count);
      return count;
    }

    @Override
    public int readLine(final byte[] b, final int off, final int len) throws IOException {
      int count = this.is.readLine(b, off, len);
      writeToCache(b, off, count);
      return count;
    }

    @Override
    public boolean isFinished() {
      return this.is.isFinished();
    }

    @Override
    public boolean isReady() {
      return this.is.isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
      this.is.setReadListener(readListener);
    }
  }

}
