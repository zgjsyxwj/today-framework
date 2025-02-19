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

package cn.taketoday.cache.interceptor;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Date;

import cn.taketoday.aop.support.AnnotationMatchingPointcut;
import cn.taketoday.aop.support.DefaultPointcutAdvisor;
import cn.taketoday.aop.support.annotation.AspectAutoProxyCreator;
import cn.taketoday.cache.Cache;
import cn.taketoday.cache.support.CaffeineCacheManager;
import cn.taketoday.cache.annotation.CacheConfiguration;
import cn.taketoday.cache.annotation.CachePut;
import cn.taketoday.context.support.StandardApplicationContext;
import cn.taketoday.context.annotation.Import;
import cn.taketoday.context.annotation.Configuration;
import cn.taketoday.lang.Singleton;
import test.demo.config.User;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author TODAY 2021/4/21 21:45
 */
class CachePutInterceptorTests {

  @Import({
          CacheUserService.class,
          CachePutInterceptor.class,
          CaffeineCacheManager.class,
          AspectAutoProxyCreator.class,
          DefaultCacheExceptionResolver.class
  })
  @Configuration
  static class CachePutConfig {

    @Singleton
    public DefaultPointcutAdvisor cachePutAdvisor(CachePutInterceptor interceptor) {
      AnnotationMatchingPointcut matchingPointcut
              = AnnotationMatchingPointcut.forMethodAnnotation(CachePut.class);
      return new DefaultPointcutAdvisor(matchingPointcut, interceptor);
    }

  }

  @Test
  void testInContext() throws Exception {
    CacheExpressionOperations operations = new CacheExpressionOperations();

    try (StandardApplicationContext context = new StandardApplicationContext()) {
      context.register(CachePutConfig.class);
      context.refresh();

      CachePutInterceptor interceptor = context.getBean(CachePutInterceptor.class);
      Method save = CacheUserService.class.getDeclaredMethod("save", User.class);
      // CachePut
      MethodKey methodKey = new MethodKey(save, CachePut.class);
      interceptor.setExpressionOperations(operations);
      CacheConfiguration cachePut = operations.getConfig(methodKey);
      Cache users = interceptor.getCache("users", cachePut);

      User today = new User(1, "TODAY", 20, "666", "666", "男", new Date());
      CacheUserService userService = context.getBean(CacheUserService.class);
      userService.save(today);

      Object by_id_666 = users.get("by_id_666");
      assertThat(by_id_666)
              .isEqualTo(today);

      User user = userService.getUser("666");
      assertThat(today).isEqualTo(user);
      user = userService.getUser("666");
      assertThat(today).isEqualTo(user);
      user = userService.getUser("666");
      assertThat(today).isEqualTo(user);
      user = userService.getUser("666");
      assertThat(today).isEqualTo(user);

      assertThat(today).isEqualTo(user).isEqualTo(by_id_666);
      // access time     no Cacheable Interceptor
      assertThat(userService.getAccessTime()).isEqualTo(4);
    }

  }

  @Test
  void testContextConditional() throws Exception {
    CacheExpressionOperations operations = new CacheExpressionOperations();

    try (StandardApplicationContext context = new StandardApplicationContext()) {
      context.register(CachePutConfig.class);
      context.refresh();

      CachePutInterceptor interceptor = context.getBean(CachePutInterceptor.class);

      Method save = CacheUserService.class.getDeclaredMethod("save", User.class);
      // CachePut
      MethodKey methodKey = new MethodKey(save, CachePut.class);
      interceptor.setExpressionOperations(operations);
      CacheConfiguration cachePut = operations.getConfig(methodKey);
      Cache users = interceptor.getCache("users", cachePut);

      User today = new User(1, "TODAY", 20, "666", "666", "男", new Date());
      CacheUserService userService = context.getBean(CacheUserService.class);
      userService.save(today);

      Object by_id_666 = users.get("by_id_666");
      assertThat(by_id_666)
              .isEqualTo(today);

      User today_6666 = new User(1, "TODAY", 20, "6666", "6666", "男", new Date());
      userService.save(today_6666);

      // condition
      Object by_id_today_6666 = users.get("by_id_6666");
      assertThat(by_id_today_6666).isNull();
    }

  }

}
