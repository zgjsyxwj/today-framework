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

package cn.taketoday.context.event;

import org.junit.jupiter.api.Test;

import java.util.EventObject;

import cn.taketoday.beans.factory.annotation.Value;
import cn.taketoday.context.support.StandardApplicationContext;
import cn.taketoday.lang.Singleton;
import lombok.ToString;

/**
 * @author TODAY 2021/3/17 12:40
 */
class MethodEventDrivenPostProcessorTests {

  @Test
  void testMethodEventDrivenPostProcessor() {

    try (StandardApplicationContext context = new StandardApplicationContext()) {

      context.addBeanFactoryPostProcessor(new MethodEventDrivenPostProcessor(context));

      context.register(EventBean.class);
      context.refresh();

      context.publishEvent(new Event("test event"));
      context.publishEvent(new SubEvent("test SubEvent"));
      context.publishEvent(new StaticEvent());
      context.publishEvent(new EventObjectEvent("test event object"));

    }
  }

  @EnableMethodEventDriven
  static class Config {

    @Singleton
    EventBean eventBean() {
      return new EventBean();
    }
  }

  @Test
  void enableMethodEventDriven() {

    try (StandardApplicationContext context = new StandardApplicationContext()) {
      context.register(Config.class);

      context.scan("cn.taketoday.context.event");
      context.refresh();

      context.publishEvent(new Event("test event"));
      context.publishEvent(new SubEvent("test SubEvent"));
      context.publishEvent(new StaticEvent());
      context.publishEvent(new EventObjectEvent("test event object"));

    }
  }

  static class EventBean {

    @EventListener
    public void listener(Event event) {
      System.out.println(event);
    }

    @EventListener(SubEvent.class)
    public void listener(Event event, EventBean bean) {
      System.out.println(event);
      System.out.println(bean);
    }

    @EventListener(StaticEvent.class)
    static void staticListener(EventBean bean, StaticEvent event) {
      System.out.println(event);
      System.out.println(bean);
    }

    @EventListener
    static void staticListener(StaticEvent event/* event must declared first place*/, EventBean bean) {
      System.out.println(event);
      System.out.println(bean);
    }

    @EventListener
    public void listener(@Value("#{1+2}") int value, EventObjectEvent event/* EventObject No need to declare first place*/) {
      assert value == 3;
    }

    @EventListener(EventObjectEvent.class)
    public void listener(@Value("#{1+2}") int value/*You can also not use event*/) {
      assert value == 3;
    }

  }

  @ToString
  static class Event {
    String name;

    Event(String name) {
      this.name = name;
    }
  }

  @ToString
  static class SubEvent extends Event {

    SubEvent(String name) {
      super(name);
    }
  }

  @ToString
  static class StaticEvent {

  }

  static class EventObjectEvent extends EventObject {

    public EventObjectEvent(Object source) {
      super(source);
    }
  }
}
