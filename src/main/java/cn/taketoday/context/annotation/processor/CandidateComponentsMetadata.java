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

package cn.taketoday.context.annotation.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Meta-data for candidate components.
 *
 * @author Stephane Nicoll
 * @since 4.0
 */
class CandidateComponentsMetadata {

  private final List<ItemMetadata> items;

  public CandidateComponentsMetadata() {
    this.items = new ArrayList<>();
  }

  public void add(ItemMetadata item) {
    this.items.add(item);
  }

  public List<ItemMetadata> getItems() {
    return Collections.unmodifiableList(this.items);
  }

  @Override
  public String toString() {
    return "CandidateComponentsMetadata{" + "items=" + this.items + '}';
  }

}
