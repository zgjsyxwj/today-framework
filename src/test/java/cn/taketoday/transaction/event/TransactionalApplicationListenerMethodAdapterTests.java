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

package cn.taketoday.transaction.event;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import cn.taketoday.context.event.ApplicationListenerMethodAdapter;
import cn.taketoday.core.ResolvableType;
import cn.taketoday.core.annotation.AnnotatedElementUtils;
import cn.taketoday.transaction.support.TransactionSynchronization;
import cn.taketoday.transaction.support.TransactionSynchronizationManager;
import cn.taketoday.util.ClassUtils;
import cn.taketoday.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @author Oliver Drotbohm
 */
public class TransactionalApplicationListenerMethodAdapterTests {

  @Test
  public void defaultPhase() {
    Method m = ReflectionUtils.findMethod(SampleEvents.class, "defaultPhase", String.class);
    assertPhase(m, TransactionPhase.AFTER_COMMIT);
  }

  @Test
  public void phaseSet() {
    Method m = ReflectionUtils.findMethod(SampleEvents.class, "phaseSet", String.class);
    assertPhase(m, TransactionPhase.AFTER_ROLLBACK);
  }

  @Test
  public void phaseAndClassesSet() {
    Method m = ReflectionUtils.findMethod(SampleEvents.class, "phaseAndClassesSet");
    assertPhase(m, TransactionPhase.AFTER_COMPLETION);
    supportsEventType(true, m, createGenericEventType(String.class));
    supportsEventType(true, m, createGenericEventType(Integer.class));
    supportsEventType(false, m, createGenericEventType(Double.class));
  }

  @Test
  public void valueSet() {
    Method m = ReflectionUtils.findMethod(SampleEvents.class, "valueSet");
    assertPhase(m, TransactionPhase.AFTER_COMMIT);
    supportsEventType(true, m, createGenericEventType(String.class));
    supportsEventType(false, m, createGenericEventType(Double.class));
  }

  @Test
  public void invokesCompletionCallbackOnSuccess() {
    Method m = ReflectionUtils.findMethod(SampleEvents.class, "defaultPhase", String.class);
    CapturingSynchronizationCallback callback = new CapturingSynchronizationCallback();
    Object event = new Object();

    TransactionalApplicationListenerMethodAdapter adapter = createTestInstance(m);
    adapter.addCallback(callback);
    runInTransaction(() -> adapter.onApplicationEvent(event));

    assertThat(callback.preEvent).isEqualTo(event);
    assertThat(callback.postEvent).isEqualTo(event);
    assertThat(callback.ex).isNull();
    assertThat(adapter.getTransactionPhase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
    assertThat(adapter.getListenerId()).endsWith("SampleEvents.defaultPhase(java.lang.String)");
  }

  @Test
  public void invokesExceptionHandlerOnException() {
    Method m = ReflectionUtils.findMethod(SampleEvents.class, "throwing", String.class);
    CapturingSynchronizationCallback callback = new CapturingSynchronizationCallback();
    String event = "event";

    TransactionalApplicationListenerMethodAdapter adapter = createTestInstance(m);
    adapter.addCallback(callback);

    assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> runInTransaction(() -> adapter.onApplicationEvent(event)))
            .withMessage("event");

    assertThat(callback.preEvent).isEqualTo(event);
    assertThat(callback.postEvent).isEqualTo(event);
    assertThat(callback.ex).isInstanceOf(RuntimeException.class);
    assertThat(callback.ex.getMessage()).isEqualTo("event");
    assertThat(adapter.getTransactionPhase()).isEqualTo(TransactionPhase.BEFORE_COMMIT);
    assertThat(adapter.getListenerId()).isEqualTo(ClassUtils.getQualifiedMethodName(m) + "(java.lang.String)");
  }

  @Test
  public void usesAnnotatedIdentifier() {
    Method m = ReflectionUtils.findMethod(SampleEvents.class, "identified", String.class);
    CapturingSynchronizationCallback callback = new CapturingSynchronizationCallback();
    String event = "event";

    TransactionalApplicationListenerMethodAdapter adapter = createTestInstance(m);
    adapter.addCallback(callback);
    runInTransaction(() -> adapter.onApplicationEvent(event));

    assertThat(callback.preEvent).isEqualTo(event);
    assertThat(callback.postEvent).isEqualTo(event);
    assertThat(callback.ex).isNull();
    assertThat(adapter.getTransactionPhase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
    assertThat(adapter.getListenerId()).endsWith("identifier");
  }

  private static void assertPhase(Method method, TransactionPhase expected) {
    assertThat(method).as("Method must not be null").isNotNull();
    TransactionalEventListener annotation =
            AnnotatedElementUtils.findMergedAnnotation(method, TransactionalEventListener.class);
    assertThat(annotation.phase()).as("Wrong phase for '" + method + "'").isEqualTo(expected);
  }

  private static void supportsEventType(boolean match, Method method, ResolvableType eventType) {
    ApplicationListenerMethodAdapter adapter = createTestInstance(method);
    assertThat(adapter.supportsEventType(eventType)).as("Wrong match for event '" + eventType + "' on " + method).isEqualTo(match);
  }

  private static TransactionalApplicationListenerMethodAdapter createTestInstance(Method m) {
    return new TransactionalApplicationListenerMethodAdapter("test", SampleEvents.class, m) {
      @Override
      protected Object getTargetBean() {
        return new SampleEvents();
      }
    };
  }

  private static ResolvableType createGenericEventType(Class<?> payloadType) {
    return ResolvableType.fromClass(payloadType);
  }

  private static void runInTransaction(Runnable runnable) {
    TransactionSynchronizationManager.setActualTransactionActive(true);
    TransactionSynchronizationManager.initSynchronization();
    try {
      runnable.run();
      TransactionSynchronizationManager.getSynchronizations().forEach(it -> {
        it.beforeCommit(false);
        it.afterCommit();
        it.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
      });
    }
    finally {
      TransactionSynchronizationManager.clearSynchronization();
      TransactionSynchronizationManager.setActualTransactionActive(false);
    }
  }

  static class SampleEvents {

    @TransactionalEventListener
    public void defaultPhase(String data) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void phaseSet(String data) {
    }

    @TransactionalEventListener(event = { String.class, Integer.class },
                                phase = TransactionPhase.AFTER_COMPLETION)
    public void phaseAndClassesSet() {
    }

    @TransactionalEventListener(String.class)
    public void valueSet() {
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void throwing(String data) {
      throw new RuntimeException(data);
    }

    @TransactionalEventListener(id = "identifier")
    public void identified(String data) {
    }
  }

}
