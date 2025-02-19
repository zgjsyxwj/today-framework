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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import cn.taketoday.beans.factory.support.ConfigurableBeanFactory;
import cn.taketoday.lang.Nullable;

/**
 * A simple thread-backed {@link Scope} implementation.
 *
 * <p>
 * <b>NOTE:</b> This thread scope is not registered by default in common
 * contexts. Instead, you need to explicitly assign it to a scope key in your
 * setup, either through {@link ConfigurableBeanFactory#registerScope} or
 * through a {@link CustomScopeConfigurer} bean.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author TODAY <br>
 * 2020-04-02 21:09
 * @since 2.1.7
 */
public class SimpleThreadScope implements Scope {

  private final ThreadLocal<Map<String, Object>> threadScope = ThreadLocal.withInitial(HashMap::new);

  @Override
  public Object remove(String name) {
    Map<String, Object> scope = this.threadScope.get();
    return scope.remove(name);
  }

  @Override
  public void registerDestructionCallback(String name, Runnable callback) {
    // TODO
  }

  @Override
  public Object get(final String beanName, final Supplier<?> objectFactory) {
    Map<String, Object> scope = this.threadScope.get();
    Object scopedObject = scope.get(beanName);
    if (scopedObject == null) {
      scopedObject = objectFactory.get();
      scope.put(beanName, scopedObject);
    }
    return scopedObject;
  }

  @Override
  @Nullable
  public Object resolveContextualObject(String key) {
    return null;
  }

  @Override
  public String getConversationId() {
    return Thread.currentThread().getName();
  }

}
