/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.taketoday.cglib.util;

import cn.taketoday.asm.ClassVisitor;
import cn.taketoday.asm.Opcodes;
import cn.taketoday.asm.Type;
import cn.taketoday.asm.commons.MethodSignature;
import cn.taketoday.cglib.core.ClassEmitter;
import cn.taketoday.cglib.core.CodeEmitter;
import cn.taketoday.cglib.core.EmitUtils;
import cn.taketoday.asm.commons.Local;

import static cn.taketoday.asm.Opcodes.ACC_PUBLIC;
import static cn.taketoday.asm.Opcodes.JAVA_VERSION;
import static cn.taketoday.core.Constant.SOURCE_FILE;

class ParallelSorterEmitter extends ClassEmitter {

  private static final Type PARALLEL_SORTER = Type.fromClass(ParallelSorter.class);
  private static final MethodSignature CSTRUCT_OBJECT_ARRAY = MethodSignature.forConstructor("Object[]");
  private static final MethodSignature NEW_INSTANCE = new MethodSignature(PARALLEL_SORTER, "newInstance", Type.TYPE_OBJECT_ARRAY);
  private static final MethodSignature SWAP = MethodSignature.from("void swap(int, int)");

  public ParallelSorterEmitter(ClassVisitor v, String className, Object[] arrays) {
    super(v);
    beginClass(JAVA_VERSION, ACC_PUBLIC, className, PARALLEL_SORTER, null, SOURCE_FILE);
    EmitUtils.nullConstructor(this);
    EmitUtils.factoryMethod(this, NEW_INSTANCE);
    generateConstructor(arrays);
    generateSwap(arrays);
    endClass();
  }

  private String getFieldName(int index) {
    return "FIELD_" + index;
  }

  private void generateConstructor(Object[] arrays) {
    CodeEmitter e = beginMethod(ACC_PUBLIC, CSTRUCT_OBJECT_ARRAY);
    e.load_this();
    e.super_invoke_constructor();
    e.load_this();
    e.load_arg(0);
    e.super_putfield("a", Type.TYPE_OBJECT_ARRAY);
    for (int i = 0; i < arrays.length; i++) {
      Type type = Type.fromClass(arrays[i].getClass());
      declare_field(Opcodes.ACC_PRIVATE, getFieldName(i), type, null);
      e.load_this();
      e.load_arg(0);
      e.push(i);
      e.aaload();
      e.checkCast(type);
      e.putfield(getFieldName(i));
    }
    e.return_value();
    e.end_method();
  }

  private void generateSwap(final Object[] arrays) {
    CodeEmitter e = beginMethod(ACC_PUBLIC, SWAP);
    for (int i = 0; i < arrays.length; i++) {
      Type type = Type.fromClass(arrays[i].getClass());
      Type component = type.getComponentType();
      Local T = e.newLocal(type);

      e.load_this();
      e.getfield(getFieldName(i));
      e.store_local(T);

      e.load_local(T);
      e.load_arg(0);

      e.load_local(T);
      e.load_arg(1);
      e.arrayLoad(component);

      e.load_local(T);
      e.load_arg(1);

      e.load_local(T);
      e.load_arg(0);
      e.arrayLoad(component);

      e.arrayStore(component);
      e.arrayStore(component);
    }
    e.return_value();
    e.end_method();
  }
}
