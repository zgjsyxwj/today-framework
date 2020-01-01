/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2020 All Rights Reserved.
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.taketoday.context.utils;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import cn.taketoday.context.Ordered;
import cn.taketoday.context.annotation.Order;

/**
 * @author TODAY <br>
 *         2018-11-08 19:02
 */
public abstract class OrderUtils {

    /**
     * Get the order of the {@link AnnotatedElement}
     * 
     * @param annotated
     *            {@link AnnotatedElement}
     * @return The order
     */
    public static int getOrder(final AnnotatedElement annotated) {
        final Order order = Objects.requireNonNull(annotated).getAnnotation(Order.class);
        if (order != null) {
            return order.value();
        }
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Get the order of the object
     * 
     * @param obj
     *            object
     * @return The order
     */
    public static int getOrder(final Object obj) {
        if (obj instanceof Ordered) {
            return ((Ordered) obj).getOrder();
        }
        if (obj instanceof AnnotatedElement) {
            return getOrder((AnnotatedElement) obj);
        }
        return getOrder(obj.getClass());
    }

    /**
     * Get Reversed Comparator
     * 
     * @return Reversed Comparator
     */
    public static Comparator<Object> getReversedComparator() {
        return getComparator().reversed();
    }

    /**
     * Get Comparator
     * 
     * @return Comparator
     * @since 2.1.7
     */
    public static Comparator<Object> getComparator() {
        return (c1, c2) -> Integer.compare(getOrder(c1), getOrder(c2));
    } 

    /**
     * Reversed sort list
     * 
     * @param list
     *            Input list
     */
    public static <T> List<T> reversedSort(List<T> list) {
        Objects.requireNonNull(list).sort(getReversedComparator());
        return list;
    }

    /**
     * Sort list
     * 
     * @param list
     *            Input list
     * @since 2.1.7
     */
    public static <T> List<T> sort(List<T> list) {
        Objects.requireNonNull(list).sort(getComparator());
        return list;
    }

    /**
     * Sort array
     * 
     * @param array
     *            Input array
     * @since 2.1.7
     */
    public static <T> T[] sort(T[] array) {
        Arrays.sort(array, getComparator());
        return array;
    }

    /**
     * Reversed sort array
     * 
     * @param array
     *            Input list
     */
    public static <T> T[] reversedSort(T[] array) {
        Arrays.sort(array, getReversedComparator());
        return array;
    }
}
