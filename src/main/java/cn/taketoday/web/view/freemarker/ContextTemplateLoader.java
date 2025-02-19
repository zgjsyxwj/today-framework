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

package cn.taketoday.web.view.freemarker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import cn.taketoday.core.io.Resource;
import cn.taketoday.core.io.ResourceLoader;
import cn.taketoday.lang.Nullable;
import cn.taketoday.logging.Logger;
import cn.taketoday.logging.LoggerFactory;
import freemarker.cache.TemplateLoader;

/**
 * FreeMarker {@link TemplateLoader} adapter that loads via a Framework {@link ResourceLoader}.
 * Used by {@link FreeMarkerConfigurationFactory} for any resource loader path that cannot
 * be resolved to a {@link java.io.File}.
 *
 * @author Juergen Hoeller
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @see FreeMarkerConfigurationFactory#setTemplateLoaderPath
 * @see freemarker.template.Configuration#setDirectoryForTemplateLoading
 * @since 4.0 2022/2/5 13:03
 */
public class ContextTemplateLoader implements TemplateLoader {
  private static final Logger log = LoggerFactory.getLogger(ContextTemplateLoader.class);

  private final String templateLoaderPath;
  private final ResourceLoader resourceLoader;

  /**
   * Create a new ContextTemplateLoader.
   *
   * @param resourceLoader the ResourceLoader to use
   * @param templateLoaderPath the template loader path to use
   */
  public ContextTemplateLoader(ResourceLoader resourceLoader, String templateLoaderPath) {
    this.resourceLoader = resourceLoader;
    if (!templateLoaderPath.endsWith("/")) {
      templateLoaderPath += "/";
    }
    this.templateLoaderPath = templateLoaderPath;
    if (log.isDebugEnabled()) {
      log.debug("TemplateLoader for FreeMarker: using resource loader [{}] and template loader path [{}]",
              resourceLoader, templateLoaderPath);
    }
  }

  @Override
  @Nullable
  public Object findTemplateSource(String name) {
    if (log.isDebugEnabled()) {
      log.debug("Looking for FreeMarker template with name [{}]", name);
    }
    Resource resource = resourceLoader.getResource(this.templateLoaderPath + name);
    return resource.exists() ? resource : null;
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    Resource resource = (Resource) templateSource;
    try {
      return new InputStreamReader(resource.getInputStream(), encoding);
    }
    catch (IOException ex) {
      log.debug("Could not find FreeMarker template: {}", resource);
      throw ex;
    }
  }

  @Override
  public long getLastModified(Object templateSource) {
    Resource resource = (Resource) templateSource;
    try {
      return resource.lastModified();
    }
    catch (IOException ex) {
      log.debug("Could not obtain last-modified timestamp for FreeMarker template in {} : {}",
              resource, ex.toString());
      return -1;
    }
  }

  @Override
  public void closeTemplateSource(Object templateSource) throws IOException { }

}

