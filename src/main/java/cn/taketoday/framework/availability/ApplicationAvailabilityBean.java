/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2022 All Rights Reserved.
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
package cn.taketoday.framework.availability;

import java.util.HashMap;
import java.util.Map;

import cn.taketoday.context.event.ApplicationEventPublisher;
import cn.taketoday.context.event.ApplicationListener;
import cn.taketoday.lang.Assert;
import cn.taketoday.logging.Logger;
import cn.taketoday.logging.LoggerFactory;

/**
 * Bean that provides an {@link ApplicationAvailability} implementation by listening for
 * {@link AvailabilityChangeEvent change events}.
 *
 * @author Brian Clozel
 * @author Phillip Webb
 * @see ApplicationAvailability
 * @since 4.0
 */
public class ApplicationAvailabilityBean
        implements ApplicationAvailability, ApplicationListener<AvailabilityChangeEvent<?>> {

  private final Map<Class<? extends AvailabilityState>, AvailabilityChangeEvent<?>> events = new HashMap<>();

  private final Logger logger;

  public ApplicationAvailabilityBean() {
    this(LoggerFactory.getLogger(ApplicationAvailabilityBean.class));
  }

  ApplicationAvailabilityBean(Logger logger) {
    this.logger = logger;
  }

  @Override
  public <S extends AvailabilityState> S getState(Class<S> stateType, S defaultState) {
    Assert.notNull(stateType, "StateType must not be null");
    Assert.notNull(defaultState, "DefaultState must not be null");
    S state = getState(stateType);
    return (state != null) ? state : defaultState;
  }

  @Override
  public <S extends AvailabilityState> S getState(Class<S> stateType) {
    AvailabilityChangeEvent<S> event = getLastChangeEvent(stateType);
    return (event != null) ? event.getState() : null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <S extends AvailabilityState> AvailabilityChangeEvent<S> getLastChangeEvent(Class<S> stateType) {
    return (AvailabilityChangeEvent<S>) this.events.get(stateType);
  }

  @Override
  public void onApplicationEvent(AvailabilityChangeEvent<?> event) {
    Class<? extends AvailabilityState> type = getStateType(event.getState());
    if (this.logger.isDebugEnabled()) {
      this.logger.debug(getLogMessage(type, event));
    }
    this.events.put(type, event);
  }

  private <S extends AvailabilityState> Object getLogMessage(Class<S> type, AvailabilityChangeEvent<?> event) {
    AvailabilityChangeEvent<S> lastChangeEvent = getLastChangeEvent(type);
    StringBuilder message = new StringBuilder(
            "Application availability state " + type.getSimpleName() + " changed");
    message.append((lastChangeEvent != null) ? " from " + lastChangeEvent.getState() : "");
    message.append(" to ").append(event.getState());
    message.append(getSourceDescription(event.getSource()));
    return message;
  }

  private String getSourceDescription(Object source) {
    if (source == null || source instanceof ApplicationEventPublisher) {
      return "";
    }
    return ": " + ((source instanceof Throwable) ? source : source.getClass().getName());
  }

  @SuppressWarnings("unchecked")
  private Class<? extends AvailabilityState> getStateType(AvailabilityState state) {
    Class<?> type = (state instanceof Enum) ? ((Enum<?>) state).getDeclaringClass() : state.getClass();
    return (Class<? extends AvailabilityState>) type;
  }

}
