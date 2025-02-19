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
package cn.taketoday.cache;

import cn.taketoday.lang.NullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractCacheTest {

  protected Cache cache;

  public AbstractCacheTest(final Cache cache) {
    this.cache = cache;
    cache.setName("test");
  }

  public void setCache(final Cache cache) {
    this.cache = cache;
  }

  protected Cache getCache() {
    return this.cache;
  }

  @BeforeEach
  public void setUp() throws Exception {
    getCache().put("key1", "value1");
    getCache().put("key2", "value2");
    getCache().put("key3", 3);
    getCache().put("key128", 128);
  }

  @Test
  public void testGetName() {
    assertEquals(getCache().getName(), "test");
  }

  @Test
  public void testSetName() {
    getCache().setName("test1");
    assertEquals(getCache().getName(), "test1");
  }

  @Test
  public void testGet() {
    final Cache cache = getCache();
    assertThat(cache.get("key")).isNull();
    assertThat(cache.get("key1")).isEqualTo("value1");
    assertThat(cache.get("key2")).isEqualTo("value2");
    assertThat(cache.get("key3")).isEqualTo(3);
    assertThat(cache.get("key128")).isEqualTo(128);

    // ----------------------String,Class
    assertThat(cache.get("key1", String.class)).isEqualTo("value1").isEqualTo(cache.get("key1"));
    Class<Integer> c = null;
    try {
      cache.get("key1", c);
      String value1 = String.class.cast(cache.get("key1", c));
      assertThat(value1).isEqualTo("value1").isEqualTo(cache.get("key1"));
    }
    catch (ClassCastException e) {
      fail("Type assert error");
    }
    try {
      cache.get("key1", c);
    }
    catch (ClassCastException e) {
      fail("Type assert error");
    }

    try {
      assertThat(cache.get("key1", int.class)).isEqualTo("value1");
      fail("Type assert error");
    }
    catch (IllegalStateException e) { }

    // --------------------------Object key, CacheCallback<T> valueLoader

    assertThat(cache.get("key", () -> {
      return "value";
    })).isEqualTo("value");
    assertThat(cache.get("key---dddddddd", (CacheCallback<String>) () -> null)).isNull();
  }

  @Test
  public void testLookupValue() {
    final Cache cache = getCache();
    cache.put("null", null);

    final Object value = cache.doGet("key");
    assertNull(value);

    final Object nullValue = cache.doGet("null");
    assertNotNull(nullValue);
    assertEquals(nullValue, NullValue.INSTANCE);
  }

  @Test
  public void testToStoreValue() {
    assertEquals(Cache.toStoreValue(null), NullValue.INSTANCE);
    assertEquals(Cache.toStoreValue("null"), "null");
  }

  @Test
  public void testToRealValue() {
    assertNull(Cache.toRealValue(null));
    assertNull(Cache.toRealValue(NullValue.INSTANCE));
    assertEquals(Cache.toRealValue("null"), "null");
  }

  @Test
  public void testEvict() {
    final Cache cache = getCache();

    cache.evict("key1");
    cache.evict("key2");
    cache.evict("key3");
    assertNull(cache.get("key1"));
    assertNull(cache.get("key2"));
    assertNull(cache.get("key3"));
  }

  @Test
  public void testClear() {
    final Cache cache = getCache();

    cache.clear();
    assertNull(cache.get("key1"));
    assertNull(cache.get("key2"));
    assertNull(cache.get("key3"));
  }

}
