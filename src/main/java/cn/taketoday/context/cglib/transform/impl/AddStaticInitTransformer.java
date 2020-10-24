/*
 * Copyright 2003,2004 The Apache Software Foundation
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
package cn.taketoday.context.cglib.transform.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import cn.taketoday.context.Constant;
import cn.taketoday.context.asm.Type;
import cn.taketoday.context.cglib.core.CodeEmitter;
import cn.taketoday.context.cglib.core.EmitUtils;
import cn.taketoday.context.cglib.core.MethodInfo;
import cn.taketoday.context.cglib.core.CglibReflectUtils;
import cn.taketoday.context.cglib.transform.ClassEmitterTransformer;

/**
 * @author Juozas Baliuka, Chris Nokleberg
 */
public class AddStaticInitTransformer extends ClassEmitterTransformer {

  private final MethodInfo info;

  public AddStaticInitTransformer(Method classInit) {
    info = CglibReflectUtils.getMethodInfo(classInit);
    if (!Modifier.isStatic(info.getModifiers())) {
      throw new IllegalArgumentException(classInit + " is not static");
    }
    Type[] types = info.getSignature().getArgumentTypes();
    if (types.length != 1 || !types[0].equals(Constant.TYPE_CLASS) || !info.getSignature().getReturnType().equals(Type.VOID_TYPE)) {
      throw new IllegalArgumentException(classInit + " illegal signature");
    }
  }

  protected void init() {
    if (!Modifier.isInterface(getAccess())) {
      CodeEmitter e = getStaticHook();
      EmitUtils.loadClassThis(e);
      e.invoke(info);
    }
  }
}
