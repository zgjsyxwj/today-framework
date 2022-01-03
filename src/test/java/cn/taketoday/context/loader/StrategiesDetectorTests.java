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

package cn.taketoday.context.loader;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import cn.taketoday.beans.factory.dependency.DependencyDescriptor;
import cn.taketoday.beans.factory.dependency.DependencyResolvingContext;
import cn.taketoday.beans.factory.dependency.DependencyResolvingStrategy;
import cn.taketoday.core.MultiValueMap;
import cn.taketoday.core.StrategiesDetector;
import cn.taketoday.core.YamlStrategiesReader;
import cn.taketoday.lang.TodayStrategies;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author TODAY 2021/7/17 22:17
 */
class StrategiesDetectorTests {

  @Test
  void load() {

    final StrategiesDetector loader = new StrategiesDetector();
    loader.setStrategiesLocation(TodayStrategies.STRATEGIES_LOCATION);
    final List<DependencyResolvingStrategy> strategy = loader.getStrategies(DependencyResolvingStrategy.class);

    assertThat(strategy)
            .hasSize(0);

    assertThat(loader.getStrategies())
            .containsKey("cn.taketoday.beans.dependency.DependencyResolvingStrategy")
            .hasSize(1);

    final Collection<String> strategies = loader.getStrategies("cn.taketoday.beans.dependency.DependencyResolvingStrategy");

    assertThat(strategies)
            .hasSize(4);
  }

  @Test
  void testYaml() {
    final StrategiesDetector loader = new StrategiesDetector(
            new YamlStrategiesReader(), "classpath:META-INF/today.strategies.yaml");
    loader.loadStrategies();
    final MultiValueMap<String, String> strategies = loader.getStrategies();

    final List<DependencyResolvingStrategy> strategy = loader.getStrategies(DependencyResolvingStrategy.class);

    assertThat(strategy)
            .hasSize(0);

    assertThat(strategies)
            .hasSize(1)
            .containsKey("cn.taketoday.beans.dependency.DependencyResolvingStrategy");

    final List<String> strings = strategies.get("cn.taketoday.beans.dependency.DependencyResolvingStrategy");

    assertThat(strings)
            .hasSize(4)
            .contains("cn.taketoday.context.loader.StrategiesDetectorTests$MyPropertyValueResolver");
  }

  public static class MyPropertyValueResolver implements DependencyResolvingStrategy {

    @Override
    public boolean supports(Field field) {
      return false;
    }

    @Override
    public boolean supports(Executable method) {
      return false;
    }

    @Override
    public void resolveDependency(DependencyDescriptor descriptor, DependencyResolvingContext context) {

    }
  }

  public static class MyPropertyValueResolver1 implements DependencyResolvingStrategy {

    @Override
    public boolean supports(Field field) {
      return false;
    }

    @Override
    public boolean supports(Executable method) {
      return false;
    }

    @Override
    public void resolveDependency(DependencyDescriptor descriptor, DependencyResolvingContext context) {

    }
  }

}
