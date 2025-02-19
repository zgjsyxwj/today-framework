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
package test.context.props;

import jakarta.annotation.PostConstruct;

import cn.taketoday.beans.factory.DisposableBean;
import cn.taketoday.beans.factory.InitializingBean;
import cn.taketoday.context.annotation.Props;
import cn.taketoday.beans.factory.annotation.Autowired;
import cn.taketoday.logging.Logger;
import cn.taketoday.logging.LoggerFactory;

/**
 * @author TODAY <br>
 *         2019-03-05 18:39
 */
public class PropsBean implements InitializingBean, DisposableBean {
  private static final Logger log = LoggerFactory.getLogger(PropsBean.class);

  final Bean bean;

  @Autowired
  public PropsBean(@Props(prefix = "site.") Bean bean) {
    this.bean = bean;
  }

  @PostConstruct
  public void initData() {
    log.info("@PostConstruct : {}", this);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    log.info("After Property Set: {}", this);
  }

  @Override
  public void destroy() throws Exception {
    log.info("Destroy: {}", this);
  }

  public static class Bean {
    String cdn;
    String icp;
    String host;
    String index;
  }

}
