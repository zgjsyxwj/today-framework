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

package cn.taketoday.util.concurrent;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author Arjen Poutsma
 * @author Sebastien Deleuze
 */
@SuppressWarnings("unchecked")
class ListenableFutureTaskTests {

  @Test
  void success() throws Exception {
    final String s = "Hello World";
    Callable<String> callable = () -> s;

    ListenableFutureTask<String> task = new ListenableFutureTask<>(callable);
    task.addCallback(new ListenableFutureCallback<String>() {
      @Override
      public void onSuccess(String result) {
        assertThat(result).isEqualTo(s);
      }

      @Override
      public void onFailure(Throwable ex) {
        throw new AssertionError(ex.getMessage(), ex);
      }
    });
    task.run();

    assertThat(task.get()).isSameAs(s);
    assertThat(task.completable().get()).isSameAs(s);
    task.completable().thenAccept(v -> assertThat(v).isSameAs(s));
  }

  @Test
  void failure() throws Exception {
    final String s = "Hello World";
    Callable<String> callable = () -> {
      throw new IOException(s);
    };

    ListenableFutureTask<String> task = new ListenableFutureTask<>(callable);
    task.addCallback(new ListenableFutureCallback<String>() {
      @Override
      public void onSuccess(String result) {
        fail("onSuccess not expected");
      }

      @Override
      public void onFailure(Throwable ex) {
        assertThat(ex.getMessage()).isEqualTo(s);
      }
    });
    task.run();

    assertThatExceptionOfType(ExecutionException.class)
            .isThrownBy(task::get)
            .havingCause()
            .withMessage(s);
    assertThatExceptionOfType(ExecutionException.class)
            .isThrownBy(task.completable()::get)
            .havingCause()
            .withMessage(s);
  }

  @Test
  void successWithLambdas() throws Exception {
    final String s = "Hello World";
    Callable<String> callable = () -> s;

    SuccessCallback<String> successCallback = mock(SuccessCallback.class);
    FailureCallback failureCallback = mock(FailureCallback.class);
    ListenableFutureTask<String> task = new ListenableFutureTask<>(callable);
    task.addCallback(successCallback, failureCallback);
    task.run();
    verify(successCallback).onSuccess(s);
    verifyNoInteractions(failureCallback);

    assertThat(task.get()).isSameAs(s);
    assertThat(task.completable().get()).isSameAs(s);
    task.completable().thenAccept(v -> assertThat(v).isSameAs(s));
  }

  @Test
  void failureWithLambdas() throws Exception {
    final String s = "Hello World";
    IOException ex = new IOException(s);
    Callable<String> callable = () -> {
      throw ex;
    };

    SuccessCallback<String> successCallback = mock(SuccessCallback.class);
    FailureCallback failureCallback = mock(FailureCallback.class);
    ListenableFutureTask<String> task = new ListenableFutureTask<>(callable);
    task.addCallback(successCallback, failureCallback);
    task.run();
    verify(failureCallback).onFailure(ex);
    verifyNoInteractions(successCallback);

    assertThatExceptionOfType(ExecutionException.class).isThrownBy(
                    task::get)
            .satisfies(e -> assertThat(e.getCause().getMessage()).isEqualTo(s));
    assertThatExceptionOfType(ExecutionException.class).isThrownBy(
                    task.completable()::get)
            .satisfies(e -> assertThat(e.getCause().getMessage()).isEqualTo(s));
  }

}
