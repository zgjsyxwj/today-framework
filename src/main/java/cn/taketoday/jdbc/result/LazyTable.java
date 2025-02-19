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

package cn.taketoday.jdbc.result;

import java.util.List;

/**
 * @author aldenquimby@gmail.com
 */
public final class LazyTable implements AutoCloseable {
  private String name;
  private ResultSetIterable<Row> rows;
  private List<Column> columns;

  public String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  public Iterable<Row> rows() {
    return rows;
  }

  public void setRows(ResultSetIterable<Row> rows) {
    this.rows = rows;
  }

  public List<Column> columns() {
    return columns;
  }

  void setColumns(List<Column> columns) {
    this.columns = columns;
  }

  @Override
  public void close() {
    this.rows.close();
  }
}
