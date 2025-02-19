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

package cn.taketoday.transaction.support;

import java.util.List;

import cn.taketoday.aop.scope.ScopedObject;
import cn.taketoday.core.InfrastructureProxy;
import cn.taketoday.lang.Assert;
import cn.taketoday.lang.Nullable;
import cn.taketoday.logging.Logger;
import cn.taketoday.logging.LoggerFactory;
import cn.taketoday.util.ClassUtils;

/**
 * Utility methods for triggering specific {@link TransactionSynchronization}
 * callback methods on all currently registered synchronizations.
 *
 * @author Juergen Hoeller
 * @see TransactionSynchronization
 * @see TransactionSynchronizationManager#getSynchronizations()
 * @since 4.0
 */
public abstract class TransactionSynchronizationUtils {
  private static final Logger log = LoggerFactory.getLogger(TransactionSynchronizationUtils.class);

  private static final boolean aopAvailable = ClassUtils.isPresent(
          "cn.taketoday.aop.scope.ScopedObject", TransactionSynchronizationUtils.class.getClassLoader());

  /**
   * Check whether the given resource transaction manager refers to the given
   * (underlying) resource factory.
   *
   * @see ResourceTransactionManager#getResourceFactory()
   * @see InfrastructureProxy#getWrappedObject()
   */
  public static boolean sameResourceFactory(ResourceTransactionManager tm, Object resourceFactory) {
    return unwrapResourceIfNecessary(tm.getResourceFactory()).equals(unwrapResourceIfNecessary(resourceFactory));
  }

  /**
   * Unwrap the given resource handle if necessary; otherwise return
   * the given handle as-is.
   *
   * @see InfrastructureProxy#getWrappedObject()
   */
  public static Object unwrapResourceIfNecessary(Object resource) {
    Assert.notNull(resource, "Resource must not be null");
    Object resourceRef = resource;
    // unwrap infrastructure proxy
    if (resourceRef instanceof InfrastructureProxy) {
      resourceRef = ((InfrastructureProxy) resourceRef).getWrappedObject();
    }
    if (aopAvailable) {
      // now unwrap scoped proxy
      resourceRef = ScopedProxyUnwrapper.unwrapIfNecessary(resourceRef);
    }
    return resourceRef;
  }

  /**
   * Trigger {@code flush} callbacks on all currently registered synchronizations.
   *
   * @throws RuntimeException if thrown by a {@code flush} callback
   * @see TransactionSynchronization#flush()
   */
  public static void triggerFlush() {
    for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
      synchronization.flush();
    }
  }

  /**
   * Trigger {@code beforeCommit} callbacks on all currently registered synchronizations.
   *
   * @param readOnly whether the transaction is defined as read-only transaction
   * @throws RuntimeException if thrown by a {@code beforeCommit} callback
   * @see TransactionSynchronization#beforeCommit(boolean)
   */
  public static void triggerBeforeCommit(boolean readOnly) {
    for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
      synchronization.beforeCommit(readOnly);
    }
  }

  /**
   * Trigger {@code beforeCompletion} callbacks on all currently registered synchronizations.
   *
   * @see TransactionSynchronization#beforeCompletion()
   */
  public static void triggerBeforeCompletion() {
    for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
      try {
        synchronization.beforeCompletion();
      }
      catch (Throwable ex) {
        log.debug("TransactionSynchronization.beforeCompletion threw exception", ex);
      }
    }
  }

  /**
   * Trigger {@code afterCommit} callbacks on all currently registered synchronizations.
   *
   * @throws RuntimeException if thrown by a {@code afterCommit} callback
   * @see TransactionSynchronizationManager#getSynchronizations()
   * @see TransactionSynchronization#afterCommit()
   */
  public static void triggerAfterCommit() {
    invokeAfterCommit(TransactionSynchronizationManager.getSynchronizations());
  }

  /**
   * Actually invoke the {@code afterCommit} methods of the
   * given Framework TransactionSynchronization objects.
   *
   * @param synchronizations a List of TransactionSynchronization objects
   * @see TransactionSynchronization#afterCommit()
   */
  public static void invokeAfterCommit(@Nullable List<TransactionSynchronization> synchronizations) {
    if (synchronizations != null) {
      for (TransactionSynchronization synchronization : synchronizations) {
        synchronization.afterCommit();
      }
    }
  }

  /**
   * Trigger {@code afterCompletion} callbacks on all currently registered synchronizations.
   *
   * @param completionStatus the completion status according to the
   * constants in the TransactionSynchronization interface
   * @see TransactionSynchronizationManager#getSynchronizations()
   * @see TransactionSynchronization#afterCompletion(int)
   * @see TransactionSynchronization#STATUS_COMMITTED
   * @see TransactionSynchronization#STATUS_ROLLED_BACK
   * @see TransactionSynchronization#STATUS_UNKNOWN
   */
  public static void triggerAfterCompletion(int completionStatus) {
    List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
    invokeAfterCompletion(synchronizations, completionStatus);
  }

  /**
   * Actually invoke the {@code afterCompletion} methods of the
   * given Framework TransactionSynchronization objects.
   *
   * @param synchronizations a List of TransactionSynchronization objects
   * @param completionStatus the completion status according to the
   * constants in the TransactionSynchronization interface
   * @see TransactionSynchronization#afterCompletion(int)
   * @see TransactionSynchronization#STATUS_COMMITTED
   * @see TransactionSynchronization#STATUS_ROLLED_BACK
   * @see TransactionSynchronization#STATUS_UNKNOWN
   */
  public static void invokeAfterCompletion(
          @Nullable List<TransactionSynchronization> synchronizations, int completionStatus) {

    if (synchronizations != null) {
      for (TransactionSynchronization synchronization : synchronizations) {
        try {
          synchronization.afterCompletion(completionStatus);
        }
        catch (Throwable ex) {
          log.debug("TransactionSynchronization.afterCompletion threw exception", ex);
        }
      }
    }
  }

  /**
   * Inner class to avoid hard-coded dependency on AOP module.
   */
  private static class ScopedProxyUnwrapper {

    public static Object unwrapIfNecessary(Object resource) {
      if (resource instanceof ScopedObject) {
        return ((ScopedObject) resource).getTargetObject();
      }
      else {
        return resource;
      }
    }
  }

}
