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

package cn.taketoday.beans.factory.dependency;

import java.lang.reflect.Executable;

import cn.taketoday.beans.factory.BeanFactory;
import cn.taketoday.beans.factory.support.BeanDefinition;
import cn.taketoday.lang.Nullable;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang 2021/11/16 22:42</a>
 * @since 4.0
 */
public class DependencyResolvingContext {

  @Nullable
  private final Executable executable;

  @Nullable
  private final BeanFactory beanFactory;

  // dependency instance
  @Nullable
  private Object dependency;

  private boolean terminate;

  private String beanName;

  private BeanDefinition definition;

  private boolean dependencyResolved;

  public void setDependencyResolved(Object dependency) {
    this.dependency = dependency;
    setDependencyResolved(true);
  }

  public void setDependencyResolved(boolean resolved) {
    this.dependencyResolved = resolved;
  }

  public boolean isDependencyResolved() {
    return dependencyResolved;
  }

  public void setDefinition(BeanDefinition definition) {
    this.definition = definition;
  }

  public BeanDefinition getDefinition() {
    return definition;
  }

  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

  public String getBeanName() {
    return beanName;
  }

  public DependencyResolvingContext(
          @Nullable Executable executable, @Nullable BeanFactory beanFactory) {
    this.executable = executable;
    this.beanFactory = beanFactory;
  }

  @Nullable
  public Executable getExecutable() {
    return executable;
  }

  @Nullable
  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  public boolean hasBeanFactory() {
    return beanFactory != null;
  }

  @Nullable
  public Object getDependency() {
    return dependency;
  }

  public void setDependency(@Nullable Object dependency) {
    this.dependency = dependency;
  }

  public boolean hasDependency() {
    return dependency != null;
  }

  @Override
  public String toString() {
    return "DependencyResolvingContext{" +
            "executable=" + executable +
            ", beanFactory=" + beanFactory +
            ", dependency=" + dependency +
            '}';
  }
}
