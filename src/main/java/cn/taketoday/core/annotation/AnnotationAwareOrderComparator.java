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

package cn.taketoday.core.annotation;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

import cn.taketoday.core.DecoratingProxy;
import cn.taketoday.core.Order;
import cn.taketoday.core.OrderComparator;
import cn.taketoday.core.annotation.MergedAnnotations.SearchStrategy;
import cn.taketoday.lang.Nullable;

/**
 * {@code AnnotationAwareOrderComparator} is an extension of
 * {@link OrderComparator} that supports
 * {@link cn.taketoday.core.Ordered} interface as well as the
 * {@link Order @Order} and {@link jakarta.annotation.Priority @Priority}
 * annotations, with an order value provided by an {@code Ordered}
 * instance overriding a statically defined annotation value (if any).
 *
 * <p>Consult the Javadoc for {@link OrderComparator} for details on the
 * sort semantics for non-ordered objects.
 *
 * @author Juergen Hoeller
 * @author Oliver Gierke
 * @author Stephane Nicoll
 * @author TODAY 2021/9/12 11:35
 * @see cn.taketoday.core.Ordered
 * @see cn.taketoday.core.Order
 * @see jakarta.annotation.Priority
 * @since 4.0
 */
public class AnnotationAwareOrderComparator extends OrderComparator {

  /**
   * Shared default instance of {@code AnnotationAwareOrderComparator}.
   */
  public static final AnnotationAwareOrderComparator INSTANCE = new AnnotationAwareOrderComparator();

  /**
   * This implementation checks for {@link Order @Order} or
   * {@link jakarta.annotation.Priority @Priority} on various kinds of
   * elements, in addition to the {@link cn.taketoday.core.Ordered}
   * check in the superclass.
   */
  @Override
  @Nullable
  protected Integer findOrder(Object obj) {
    Integer order = super.findOrder(obj);
    if (order != null) {
      return order;
    }
    return findOrderFromAnnotation(obj);
  }

  @Nullable
  private Integer findOrderFromAnnotation(Object obj) {
    AnnotatedElement element = obj instanceof AnnotatedElement ? (AnnotatedElement) obj : obj.getClass();
    MergedAnnotations annotations = MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY);
    Integer order = OrderUtils.getOrderFromAnnotations(element, annotations);
    if (order == null && obj instanceof DecoratingProxy) {
      return findOrderFromAnnotation(((DecoratingProxy) obj).getDecoratedClass());
    }
    return order;
  }

  /**
   * This implementation retrieves an @{@link jakarta.annotation.Priority}
   * value, allowing for additional semantics over the regular @{@link Order}
   * annotation: typically, selecting one object over another in case of
   * multiple matches but only one object to be returned.
   */
  @Override
  @Nullable
  public Integer getPriority(Object obj) {
    if (obj instanceof Class) {
      return OrderUtils.getPriority((Class<?>) obj);
    }
    Integer priority = OrderUtils.getPriority(obj.getClass());
    if (priority == null && obj instanceof DecoratingProxy) {
      return getPriority(((DecoratingProxy) obj).getDecoratedClass());
    }
    return priority;
  }

  /**
   * Sort the given list with a default {@link AnnotationAwareOrderComparator}.
   * <p>Optimized to skip sorting for lists with size 0 or 1,
   * in order to avoid unnecessary array extraction.
   *
   * @param list the List to sort
   * @see java.util.List#sort(java.util.Comparator)
   */
  public static void sort(List<?> list) {
    if (list.size() > 1) {
      list.sort(INSTANCE);
    }
  }

  /**
   * Sort the given array with a default AnnotationAwareOrderComparator.
   * <p>Optimized to skip sorting for lists with size 0 or 1,
   * in order to avoid unnecessary array extraction.
   *
   * @param array the array to sort
   * @see java.util.Arrays#sort(Object[], java.util.Comparator)
   */
  public static void sort(Object[] array) {
    if (array.length > 1) {
      Arrays.sort(array, INSTANCE);
    }
  }

  /**
   * Sort the given array or List with a default AnnotationAwareOrderComparator,
   * if necessary. Simply skips sorting when given any other value.
   * <p>Optimized to skip sorting for lists with size 0 or 1,
   * in order to avoid unnecessary array extraction.
   *
   * @param value the array or List to sort
   * @see java.util.Arrays#sort(Object[], java.util.Comparator)
   */
  public static void sortIfNecessary(Object value) {
    if (value instanceof Object[]) {
      sort((Object[]) value);
    }
    else if (value instanceof List) {
      sort((List<?>) value);
    }
  }

}
