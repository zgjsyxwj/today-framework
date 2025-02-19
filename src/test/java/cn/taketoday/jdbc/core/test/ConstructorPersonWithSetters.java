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

package cn.taketoday.jdbc.core.test;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Juergen Hoeller
 */
public class ConstructorPersonWithSetters {

  private String name;

  private long age;

  private Date birth_date;

  private BigDecimal balance;

  public ConstructorPersonWithSetters(String name, long age, Date birth_date, BigDecimal balance) {
    this.name = name.toUpperCase();
    this.age = age;
    this.birth_date = birth_date;
    this.balance = balance;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAge(long age) {
    this.age = age;
  }

  public void setBirth_date(Date birth_date) {
    this.birth_date = birth_date;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public String name() {
    return this.name;
  }

  public long age() {
    return this.age;
  }

  public Date birth_date() {
    return this.birth_date;
  }

  public BigDecimal balance() {
    return this.balance;
  }

}
