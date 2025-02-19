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

import junit.framework.Test;
import junit.framework.TestSuite;

import cn.taketoday.core.bytecode.transform.AbstractTransformTest;
import cn.taketoday.core.bytecode.transform.ClassTransformer;
import cn.taketoday.core.bytecode.transform.ClassTransformerFactory;

/**
 * @author baliuka
 */
public class TestAddDelegate extends AbstractTransformTest {

  /** Creates a new instance of TestAddDelegate */
  public TestAddDelegate(String name) {
    super(name);
  }

  public interface Interface {

    Object getDelegte();

    Object getTarget();

  }

  public void test() {

    Interface i = (Interface) this;
    assertEquals(i.getTarget(), this);

  }

  public static class ImplExclude implements Interface {

    private Object target;

    public ImplExclude(Object target) {
      this.target = target;
    }

    public Object getDelegte() {
      return this;
    }

    public Object getTarget() {
      return target;
    }
  }

  public TestAddDelegate() {
    super(null);
  }

  protected ClassTransformerFactory getTransformer() throws Exception {

    return new ClassTransformerFactory() {

      public ClassTransformer newTransformer() {

        return new AddDelegateTransformer(new Class[] { Interface.class }, ImplExclude.class);

      }

    };

  }

  public static void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() throws Exception {

    return new TestSuite(new TestAddDelegate().transform());

  }

}
