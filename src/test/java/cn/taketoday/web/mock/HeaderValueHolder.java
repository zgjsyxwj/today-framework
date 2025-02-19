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

package cn.taketoday.web.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cn.taketoday.util.ObjectUtils;

/**
 * Internal helper class that serves as a value holder for request headers.
 *
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0.1
 */
class HeaderValueHolder {

  private final List<Object> values = new LinkedList<>();

  void setValue(Object value) {
    this.values.clear();
    if (value != null) {
      this.values.add(value);
    }
  }

  void addValue(Object value) {
    this.values.add(value);
  }

  void addValues(Collection<?> values) {
    this.values.addAll(values);
  }

  void addValueArray(Object values) {
    mergeArrayIntoCollection(values, this.values);
  }

  public static <E> void mergeArrayIntoCollection(Object array, Collection<E> collection) {
    Object[] arr = ObjectUtils.toObjectArray(array);
    for (Object elem : arr) {
      collection.add((E) elem);
    }
  }

  List<Object> getValues() {
    return Collections.unmodifiableList(this.values);
  }

  List<String> getStringValues() {
    List<String> stringList = new ArrayList<>(this.values.size());
    for (Object value : this.values) {
      stringList.add(value.toString());
    }
    return Collections.unmodifiableList(stringList);
  }

  Object getValue() {
    return (!this.values.isEmpty() ? this.values.get(0) : null);
  }

  String getStringValue() {
    return (!this.values.isEmpty() ? String.valueOf(this.values.get(0)) : null);
  }

  @Override
  public String toString() {
    return this.values.toString();
  }

}
