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

package cn.taketoday.beans.factory;

import java.io.Serializable;
import java.util.Objects;

/**
 * Object to hold information and value for an individual bean property.
 *
 * @author TODAY 2021/3/21 15:49
 */
public class PropertyValue implements Serializable {

  private String name;
  private Object value;

  public PropertyValue() { }

  public PropertyValue(String name, Object value) {
    this.name = name;
    this.value = value;
  }

  public PropertyValue(PropertyValue pv) {
    this.name = pv.name;
    this.value = pv.value;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof PropertyValue that))
      return false;
    return Objects.equals(name, that.name) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public String toString() {
    return "PropertyValue{" +
            "name='" + name + '\'' +
            ", value=" + value +
            '}';
  }
}
