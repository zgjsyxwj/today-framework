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

package cn.taketoday.dao.support;

import java.util.Collection;

import cn.taketoday.dao.DataAccessException;
import cn.taketoday.dao.EmptyResultDataAccessException;
import cn.taketoday.dao.IncorrectResultSizeDataAccessException;
import cn.taketoday.dao.TypeMismatchDataAccessException;
import cn.taketoday.lang.Assert;
import cn.taketoday.lang.Nullable;
import cn.taketoday.util.CollectionUtils;
import cn.taketoday.util.NumberUtils;

/**
 * Miscellaneous utility methods for DAO implementations.
 * Useful with any data access technology.
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
public abstract class DataAccessUtils {

  /**
   * Return a single result object from the given Collection.
   * <p>Returns {@code null} if 0 result objects found;
   * throws an exception if more than 1 element found.
   *
   * @param results the result Collection (can be {@code null})
   * @return the single result object, or {@code null} if none
   * @throws IncorrectResultSizeDataAccessException if more than one
   * element has been found in the given Collection
   */
  @Nullable
  public static <T> T singleResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
    if (CollectionUtils.isEmpty(results)) {
      return null;
    }
    if (results.size() > 1) {
      throw new IncorrectResultSizeDataAccessException(1, results.size());
    }
    return results.iterator().next();
  }

  /**
   * Return a single result object from the given Collection.
   * <p>Throws an exception if 0 or more than 1 element found.
   *
   * @param results the result Collection (can be {@code null}
   * but is not expected to contain {@code null} elements)
   * @return the single result object
   * @throws IncorrectResultSizeDataAccessException if more than one
   * element has been found in the given Collection
   * @throws EmptyResultDataAccessException if no element at all
   * has been found in the given Collection
   */
  public static <T> T requiredSingleResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
    if (CollectionUtils.isEmpty(results)) {
      throw new EmptyResultDataAccessException(1);
    }
    if (results.size() > 1) {
      throw new IncorrectResultSizeDataAccessException(1, results.size());
    }
    return results.iterator().next();
  }

  /**
   * Return a single result object from the given Collection.
   * <p>Throws an exception if 0 or more than 1 element found.
   *
   * @param results the result Collection (can be {@code null}
   * and is also expected to contain {@code null} elements)
   * @return the single result object
   * @throws IncorrectResultSizeDataAccessException if more than one
   * element has been found in the given Collection
   * @throws EmptyResultDataAccessException if no element at all
   * has been found in the given Collection
   * @since 4.0
   */
  @Nullable
  public static <T> T nullableSingleResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
    // This is identical to the requiredSingleResult implementation but differs in the
    // semantics of the incoming Collection (which we currently can't formally express)
    if (CollectionUtils.isEmpty(results)) {
      throw new EmptyResultDataAccessException(1);
    }
    if (results.size() > 1) {
      throw new IncorrectResultSizeDataAccessException(1, results.size());
    }
    return results.iterator().next();
  }

  /**
   * Return a unique result object from the given Collection.
   * <p>Returns {@code null} if 0 result objects found;
   * throws an exception if more than 1 instance found.
   *
   * @param results the result Collection (can be {@code null})
   * @return the unique result object, or {@code null} if none
   * @throws IncorrectResultSizeDataAccessException if more than one
   * result object has been found in the given Collection
   * @see cn.taketoday.util.CollectionUtils#hasUniqueObject
   */
  @Nullable
  public static <T> T uniqueResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
    if (CollectionUtils.isEmpty(results)) {
      return null;
    }
    if (!CollectionUtils.hasUniqueObject(results)) {
      throw new IncorrectResultSizeDataAccessException(1, results.size());
    }
    return results.iterator().next();
  }

  /**
   * Return a unique result object from the given Collection.
   * <p>Throws an exception if 0 or more than 1 instance found.
   *
   * @param results the result Collection (can be {@code null}
   * but is not expected to contain {@code null} elements)
   * @return the unique result object
   * @throws IncorrectResultSizeDataAccessException if more than one
   * result object has been found in the given Collection
   * @throws EmptyResultDataAccessException if no result object at all
   * has been found in the given Collection
   * @see cn.taketoday.util.CollectionUtils#hasUniqueObject
   */
  public static <T> T requiredUniqueResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
    if (CollectionUtils.isEmpty(results)) {
      throw new EmptyResultDataAccessException(1);
    }
    if (!CollectionUtils.hasUniqueObject(results)) {
      throw new IncorrectResultSizeDataAccessException(1, results.size());
    }
    return results.iterator().next();
  }

  /**
   * Return a unique result object from the given Collection.
   * Throws an exception if 0 or more than 1 result objects found,
   * of if the unique result object is not convertible to the
   * specified required type.
   *
   * @param results the result Collection (can be {@code null}
   * but is not expected to contain {@code null} elements)
   * @return the unique result object
   * @throws IncorrectResultSizeDataAccessException if more than one
   * result object has been found in the given Collection
   * @throws EmptyResultDataAccessException if no result object
   * at all has been found in the given Collection
   * @throws TypeMismatchDataAccessException if the unique object does
   * not match the specified required type
   */
  @SuppressWarnings("unchecked")
  public static <T> T objectResult(@Nullable Collection<?> results, @Nullable Class<T> requiredType)
          throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {

    Object result = requiredUniqueResult(results);
    if (requiredType != null && !requiredType.isInstance(result)) {
      if (String.class == requiredType) {
        result = result.toString();
      }
      else if (Number.class.isAssignableFrom(requiredType) && result instanceof Number) {
        try {
          result = NumberUtils.convertNumberToTargetClass(((Number) result), (Class<? extends Number>) requiredType);
        }
        catch (IllegalArgumentException ex) {
          throw new TypeMismatchDataAccessException(ex.getMessage());
        }
      }
      else {
        throw new TypeMismatchDataAccessException(
                "Result object is of type [" + result.getClass().getName() +
                        "] and could not be converted to required type [" + requiredType.getName() + "]");
      }
    }
    return (T) result;
  }

  /**
   * Return a unique int result from the given Collection.
   * Throws an exception if 0 or more than 1 result objects found,
   * of if the unique result object is not convertible to an int.
   *
   * @param results the result Collection (can be {@code null}
   * but is not expected to contain {@code null} elements)
   * @return the unique int result
   * @throws IncorrectResultSizeDataAccessException if more than one
   * result object has been found in the given Collection
   * @throws EmptyResultDataAccessException if no result object
   * at all has been found in the given Collection
   * @throws TypeMismatchDataAccessException if the unique object
   * in the collection is not convertible to an int
   */
  public static int intResult(@Nullable Collection<?> results)
          throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {

    return objectResult(results, Number.class).intValue();
  }

  /**
   * Return a unique long result from the given Collection.
   * Throws an exception if 0 or more than 1 result objects found,
   * of if the unique result object is not convertible to a long.
   *
   * @param results the result Collection (can be {@code null}
   * but is not expected to contain {@code null} elements)
   * @return the unique long result
   * @throws IncorrectResultSizeDataAccessException if more than one
   * result object has been found in the given Collection
   * @throws EmptyResultDataAccessException if no result object
   * at all has been found in the given Collection
   * @throws TypeMismatchDataAccessException if the unique object
   * in the collection is not convertible to a long
   */
  public static long longResult(@Nullable Collection<?> results)
          throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {

    return objectResult(results, Number.class).longValue();
  }

  /**
   * Return a translated exception if this is appropriate,
   * otherwise return the given exception as-is.
   *
   * @param rawException an exception that we may wish to translate
   * @param pet the PersistenceExceptionTranslator to use to perform the translation
   * @return a translated persistence exception if translation is possible,
   * or the raw exception if it is not
   */
  public static RuntimeException translateIfNecessary(
          RuntimeException rawException, PersistenceExceptionTranslator pet) {

    Assert.notNull(pet, "PersistenceExceptionTranslator must not be null");
    DataAccessException dae = pet.translateExceptionIfPossible(rawException);
    return (dae != null ? dae : rawException);
  }

}
