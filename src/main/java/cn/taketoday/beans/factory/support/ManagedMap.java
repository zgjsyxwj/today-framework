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

package cn.taketoday.beans.factory.support;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.taketoday.beans.BeanMetadataElement;
import cn.taketoday.beans.Mergeable;
import cn.taketoday.lang.Nullable;

/**
 * Tag collection class used to hold managed Map values, which may
 * include runtime bean references (to be resolved into bean objects).
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 4.0
 */
@SuppressWarnings("serial")
public class ManagedMap<K, V> extends LinkedHashMap<K, V> implements Mergeable, BeanMetadataElement {

  @Nullable
  private Object source;

  @Nullable
  private String keyTypeName;

  @Nullable
  private String valueTypeName;

  private boolean mergeEnabled;

  public ManagedMap() {
  }

  public ManagedMap(int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Set the configuration source {@code Object} for this metadata element.
   * <p>The exact type of the object will depend on the configuration mechanism used.
   */
  public void setSource(@Nullable Object source) {
    this.source = source;
  }

  @Override
  @Nullable
  public Object getSource() {
    return this.source;
  }

  /**
   * Set the default key type name (class name) to be used for this map.
   */
  public void setKeyTypeName(@Nullable String keyTypeName) {
    this.keyTypeName = keyTypeName;
  }

  /**
   * Return the default key type name (class name) to be used for this map.
   */
  @Nullable
  public String getKeyTypeName() {
    return this.keyTypeName;
  }

  /**
   * Set the default value type name (class name) to be used for this map.
   */
  public void setValueTypeName(@Nullable String valueTypeName) {
    this.valueTypeName = valueTypeName;
  }

  /**
   * Return the default value type name (class name) to be used for this map.
   */
  @Nullable
  public String getValueTypeName() {
    return this.valueTypeName;
  }

  /**
   * Set whether merging should be enabled for this collection,
   * in case of a 'parent' collection value being present.
   */
  public void setMergeEnabled(boolean mergeEnabled) {
    this.mergeEnabled = mergeEnabled;
  }

  @Override
  public boolean isMergeEnabled() {
    return this.mergeEnabled;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object merge(@Nullable Object parent) {
    if (!this.mergeEnabled) {
      throw new IllegalStateException("Not allowed to merge when the 'mergeEnabled' property is set to 'false'");
    }
    if (parent == null) {
      return this;
    }
    if (!(parent instanceof Map)) {
      throw new IllegalArgumentException("Cannot merge with object of type [" + parent.getClass() + "]");
    }
    Map<K, V> merged = new ManagedMap<>();
    merged.putAll((Map<K, V>) parent);
    merged.putAll(this);
    return merged;
  }

}
