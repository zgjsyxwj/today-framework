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

package cn.taketoday.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * @author TODAY 2021/3/6 9:34
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
class StopWatchTests {

  private static final String ID = "myId";

  private static final String name1 = "Task 1";
  private static final String name2 = "Task 2";

  private static final long duration1 = 200;
  private static final long duration2 = 100;
  private static final long fudgeFactor = 50;

  private final StopWatch stopWatch = new StopWatch(ID);

  @Test
  void failureToStartBeforeGettingTimings() {
    assertThatIllegalStateException().isThrownBy(stopWatch::getLastTaskTimeMillis);
  }

  @Test
  void failureToStartBeforeStop() {
    assertThatIllegalStateException().isThrownBy(stopWatch::stop);
  }

  @Test
  void rejectsStartTwice() {
    stopWatch.start();
    assertThat(stopWatch.isRunning()).isTrue();
    stopWatch.stop();
    assertThat(stopWatch.isRunning()).isFalse();

    stopWatch.start();
    assertThat(stopWatch.isRunning()).isTrue();
    assertThatIllegalStateException().isThrownBy(stopWatch::start);
  }

  @Test
  void validUsage() throws Exception {
    assertThat(stopWatch.isRunning()).isFalse();

    stopWatch.start(name1);
    Thread.sleep(duration1);
    assertThat(stopWatch.isRunning()).isTrue();
    assertThat(stopWatch.currentTaskName()).isEqualTo(name1);
    stopWatch.stop();
    assertThat(stopWatch.isRunning()).isFalse();
    assertThat(stopWatch.getLastTaskTimeNanos())
            .as("last task time in nanoseconds for task #1")
            .isGreaterThanOrEqualTo(millisToNanos(duration1 - fudgeFactor))
            .isLessThanOrEqualTo(millisToNanos(duration1 + fudgeFactor));
    assertThat(stopWatch.getTotalTimeMillis())
            .as("total time in milliseconds for task #1")
            .isGreaterThanOrEqualTo(duration1 - fudgeFactor)
            .isLessThanOrEqualTo(duration1 + fudgeFactor);
    assertThat(stopWatch.getTotalTimeSeconds())
            .as("total time in seconds for task #1")
            .isGreaterThanOrEqualTo((duration1 - fudgeFactor) / 1000.0)
            .isLessThanOrEqualTo((duration1 + fudgeFactor) / 1000.0);

    stopWatch.start(name2);
    Thread.sleep(duration2);
    assertThat(stopWatch.isRunning()).isTrue();
    assertThat(stopWatch.currentTaskName()).isEqualTo(name2);
    stopWatch.stop();
    assertThat(stopWatch.isRunning()).isFalse();
    assertThat(stopWatch.getLastTaskTimeNanos())
            .as("last task time in nanoseconds for task #2")
            .isGreaterThanOrEqualTo(millisToNanos(duration2))
            .isLessThanOrEqualTo(millisToNanos(duration2 + fudgeFactor));
    assertThat(stopWatch.getTotalTimeMillis())
            .as("total time in milliseconds for tasks #1 and #2")
            .isGreaterThanOrEqualTo(duration1 + duration2 - fudgeFactor)
            .isLessThanOrEqualTo(duration1 + duration2 + fudgeFactor);
    assertThat(stopWatch.getTotalTimeSeconds())
            .as("total time in seconds for task #2")
            .isGreaterThanOrEqualTo((duration1 + duration2 - fudgeFactor) / 1000.0)
            .isLessThanOrEqualTo((duration1 + duration2 + fudgeFactor) / 1000.0);

    assertThat(stopWatch.getTaskCount()).isEqualTo(2);
    assertThat(stopWatch.prettyPrint()).contains(name1, name2);
    assertThat(stopWatch.getTaskInfo()).extracting(StopWatch.TaskInfo::getTaskName).containsExactly(name1, name2);
    assertThat(stopWatch.toString()).contains(ID, name1, name2);
    assertThat(stopWatch.getId()).isEqualTo(ID);
  }

  @Test
  void validUsageDoesNotKeepTaskList() throws Exception {
    stopWatch.setKeepTaskList(false);

    stopWatch.start(name1);
    Thread.sleep(duration1);
    assertThat(stopWatch.currentTaskName()).isEqualTo(name1);
    stopWatch.stop();

    stopWatch.start(name2);
    Thread.sleep(duration2);
    assertThat(stopWatch.currentTaskName()).isEqualTo(name2);
    stopWatch.stop();

    assertThat(stopWatch.getTaskCount()).isEqualTo(2);
    assertThat(stopWatch.prettyPrint()).contains("No task info kept");
    assertThat(stopWatch.toString()).doesNotContain(name1, name2);
    assertThatExceptionOfType(UnsupportedOperationException.class)
            .isThrownBy(stopWatch::getTaskInfo)
            .withMessage("Task info is not being kept!");
  }

  private static long millisToNanos(long duration) {
    return MILLISECONDS.toNanos(duration);
  }

}
