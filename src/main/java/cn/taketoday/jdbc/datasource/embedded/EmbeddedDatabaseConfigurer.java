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

package cn.taketoday.jdbc.datasource.embedded;

import javax.sql.DataSource;

import cn.taketoday.lang.Assert;

/**
 * {@code EmbeddedDatabaseConfigurer} encapsulates the configuration required to
 * create, connect to, and shut down a specific type of embedded database such as
 * HSQL, H2, or Derby.
 *
 * @author Keith Donald
 * @author Sam Brannen
 * @since 4.0
 */
public interface EmbeddedDatabaseConfigurer {

  /**
   * Configure the properties required to create and connect to the embedded database.
   *
   * @param properties connection properties to configure
   * @param databaseName the name of the embedded database
   */
  void configureConnectionProperties(ConnectionProperties properties, String databaseName);

  /**
   * Shut down the embedded database instance that backs the supplied {@link DataSource}.
   *
   * @param dataSource the corresponding {@link DataSource}
   * @param databaseName the name of the database being shut down
   */
  void shutdown(DataSource dataSource, String databaseName);

  /**
   * Return a configurer instance for the given embedded database type.
   *
   * @param type the embedded database type (HSQL, H2 or Derby)
   * @return the configurer instance
   * @throws IllegalStateException if the driver for the specified database type is not available
   */
  static EmbeddedDatabaseConfigurer from(EmbeddedDatabaseType type) throws IllegalStateException {
    Assert.notNull(type, "EmbeddedDatabaseType is required");
    try {
      return switch (type) {
        case HSQL -> HsqlEmbeddedDatabaseConfigurer.getInstance();
        case H2 -> H2EmbeddedDatabaseConfigurer.getInstance();
        case DERBY -> DerbyEmbeddedDatabaseConfigurer.getInstance();
      };
    }
    catch (ClassNotFoundException | NoClassDefFoundError ex) {
      throw new IllegalStateException("Driver for test database type [" + type + "] is not available", ex);
    }
  }

}
