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
package cn.taketoday.core.bytecode.transform.impl;

/**
 * @author Chris Nokleberg
 */
public class AbstractInterceptFieldCallback implements InterceptFieldCallback {

  public int writeInt(Object obj, String name, int oldValue, int newValue) {
    return newValue;
  }

  public char writeChar(Object obj, String name, char oldValue, char newValue) {
    return newValue;
  }

  public byte writeByte(Object obj, String name, byte oldValue, byte newValue) {
    return newValue;
  }

  public boolean writeBoolean(Object obj, String name, boolean oldValue, boolean newValue) {
    return newValue;
  }

  public short writeShort(Object obj, String name, short oldValue, short newValue) {
    return newValue;
  }

  public float writeFloat(Object obj, String name, float oldValue, float newValue) {
    return newValue;
  }

  public double writeDouble(Object obj, String name, double oldValue, double newValue) {
    return newValue;
  }

  public long writeLong(Object obj, String name, long oldValue, long newValue) {
    return newValue;
  }

  public Object writeObject(Object obj, String name, Object oldValue, Object newValue) {
    return newValue;
  }

  public int readInt(Object obj, String name, int oldValue) {
    return oldValue;
  }

  public char readChar(Object obj, String name, char oldValue) {
    return oldValue;
  }

  public byte readByte(Object obj, String name, byte oldValue) {
    return oldValue;
  }

  public boolean readBoolean(Object obj, String name, boolean oldValue) {
    return oldValue;
  }

  public short readShort(Object obj, String name, short oldValue) {
    return oldValue;
  }

  public float readFloat(Object obj, String name, float oldValue) {
    return oldValue;
  }

  public double readDouble(Object obj, String name, double oldValue) {
    return oldValue;
  }

  public long readLong(Object obj, String name, long oldValue) {
    return oldValue;
  }

  public Object readObject(Object obj, String name, Object oldValue) {
    return oldValue;
  }
}
