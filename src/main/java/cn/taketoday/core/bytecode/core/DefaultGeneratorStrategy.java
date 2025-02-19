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
package cn.taketoday.core.bytecode.core;

import cn.taketoday.core.bytecode.ClassWriter;

public class DefaultGeneratorStrategy implements GeneratorStrategy {

  public static final DefaultGeneratorStrategy INSTANCE = new DefaultGeneratorStrategy();

  public byte[] generate(ClassGenerator cg) throws Exception {
    DebuggingClassWriter cw = getClassVisitor();
    transform(cg).generateClass(cw);
    return transform(cw.toByteArray());
  }

  protected DebuggingClassWriter getClassVisitor() {
    return new DebuggingClassWriter(ClassWriter.COMPUTE_FRAMES);
  }

  protected byte[] transform(byte[] b) throws Exception {
    return b;
  }

  protected ClassGenerator transform(ClassGenerator cg) throws Exception {
    return cg;
  }
}
