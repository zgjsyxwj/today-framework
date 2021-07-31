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
package cn.taketoday.context.cglib.reflect;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import cn.taketoday.context.Constant;
import cn.taketoday.context.asm.ClassVisitor;
import cn.taketoday.context.asm.Label;
import cn.taketoday.context.asm.Opcodes;
import cn.taketoday.context.asm.Type;
import cn.taketoday.context.cglib.core.Block;
import cn.taketoday.context.cglib.core.CglibReflectUtils;
import cn.taketoday.context.cglib.core.ClassEmitter;
import cn.taketoday.context.cglib.core.CodeEmitter;
import cn.taketoday.context.cglib.core.CglibCollectionUtils;
import cn.taketoday.context.cglib.core.DuplicatesPredicate;
import cn.taketoday.context.cglib.core.EmitUtils;
import cn.taketoday.context.cglib.core.MethodInfo;
import cn.taketoday.context.cglib.core.MethodInfoTransformer;
import cn.taketoday.context.cglib.core.ObjectSwitchCallback;
import cn.taketoday.context.cglib.core.ProcessSwitchCallback;
import cn.taketoday.context.cglib.core.Signature;
import cn.taketoday.context.cglib.core.Transformer;
import cn.taketoday.context.cglib.core.TypeUtils;
import cn.taketoday.context.cglib.core.VisibilityPredicate;

import static cn.taketoday.context.Constant.SWITCH_STYLE_HASH;

/**
 * @author TODAY <br>
 * 2018-11-08 15:08
 */
@SuppressWarnings("all")
class FastClassEmitter extends ClassEmitter {

  private static final Signature CSTRUCT_CLASS = TypeUtils.parseConstructor("Class");
  private static final Signature METHOD_GET_INDEX = TypeUtils.parseSignature("int getIndex(String, Class[])");
  private static final Signature SIGNATURE_GET_INDEX = new Signature("getIndex", Type.INT_TYPE, new Type[] { Constant.TYPE_SIGNATURE });
  private static final Signature TO_STRING = TypeUtils.parseSignature("String toString()");
  private static final Signature CONSTRUCTOR_GET_INDEX = TypeUtils.parseSignature("int getIndex(Class[])");
  private static final Signature INVOKE = TypeUtils.parseSignature("Object invoke(int, Object, Object[])");
  private static final Signature NEW_INSTANCE = TypeUtils.parseSignature("Object newInstance(int, Object[])");
  private static final Signature GET_MAX_INDEX = TypeUtils.parseSignature("int getMaxIndex()");

  private static final Signature GET_SIGNATURE_WITHOUT_RETURN_TYPE = //
          TypeUtils.parseSignature("String getSignatureWithoutReturnType(String, Class[])");

  private static final Type FAST_CLASS = TypeUtils.parseType(FastClass.class);
  private static final Type ILLEGAL_ARGUMENT_EXCEPTION = TypeUtils.parseType("IllegalArgumentException");

  private static final Type INVOCATION_TARGET_EXCEPTION = //
          TypeUtils.parseType("java.lang.reflect.InvocationTargetException");

  private static final Type[] INVOCATION_TARGET_EXCEPTION_ARRAY = { INVOCATION_TARGET_EXCEPTION };

  public FastClassEmitter(ClassVisitor v, String className, Class type) {
    super(v);

    Type base = Type.getType(type);
    beginClass(Opcodes.JAVA_VERSION, Opcodes.ACC_PUBLIC, className, FAST_CLASS, null, Constant.SOURCE_FILE);

    // constructor
    CodeEmitter e = beginMethod(Opcodes.ACC_PUBLIC, CSTRUCT_CLASS);
    e.load_this();
    e.load_args();
    e.super_invoke_constructor(CSTRUCT_CLASS);
    e.return_value();
    e.end_method();

    VisibilityPredicate vp = new VisibilityPredicate(type, false);
    List<Method> methods = CglibReflectUtils.addAllMethods(type, new ArrayList<>());
    CglibCollectionUtils.filter(methods, vp);
    CglibCollectionUtils.filter(methods, new DuplicatesPredicate());

    ArrayList<Member> constructors = new ArrayList<>();
    Collections.addAll(constructors, type.getDeclaredConstructors());
    CglibCollectionUtils.filter(constructors, vp);

    // getIndex(String)
    emitIndexBySignature(methods);

    // getIndex(String, Class[])
    emitIndexByClassArray(methods);

    // getIndex(Class[])
    e = beginMethod(Opcodes.ACC_PUBLIC, CONSTRUCTOR_GET_INDEX);
    e.load_args();
    List<MethodInfo> info = CglibCollectionUtils.transform(constructors, MethodInfoTransformer.getInstance());
    EmitUtils.constructorSwitch(e, info, new GetIndexCallback(e, info));
    e.end_method();

    // invoke(int, Object, Object[])
    e = beginMethod(Opcodes.ACC_PUBLIC, INVOKE, INVOCATION_TARGET_EXCEPTION_ARRAY);
    e.load_arg(1);
    e.checkcast(base);
    e.load_arg(0);
    invokeSwitchHelper(e, methods, 2, base);
    e.end_method();

    // newInstance(int, Object[])
    e = beginMethod(Opcodes.ACC_PUBLIC, NEW_INSTANCE, INVOCATION_TARGET_EXCEPTION_ARRAY);
    e.new_instance(base);
    e.dup();
    e.load_arg(0);
    invokeSwitchHelper(e, constructors, 1, base);
    e.end_method();

    // getMaxIndex()
    e = beginMethod(Opcodes.ACC_PUBLIC, GET_MAX_INDEX);
    e.push(methods.size() - 1);
    e.return_value();
    e.end_method();

    endClass();
  }

  // TODO: support constructor indices ("<init>")
  private void emitIndexBySignature(List<Method> methods) {
    CodeEmitter e = beginMethod(Opcodes.ACC_PUBLIC, SIGNATURE_GET_INDEX);
    List<String> signatures = CglibCollectionUtils.transform(methods, new Transformer<Method, String>() {
      public String transform(Method obj) {
        return CglibReflectUtils.getSignature(obj).toString();
      }
    });
    e.load_arg(0);
    e.invoke_virtual(Constant.TYPE_OBJECT, TO_STRING);
    signatureSwitchHelper(e, signatures);
    e.end_method();
  }

  private static final int TOO_MANY_METHODS = 100; // TODO

  private void emitIndexByClassArray(List methods) {
    CodeEmitter e = beginMethod(Opcodes.ACC_PUBLIC, METHOD_GET_INDEX);
    if (methods.size() > TOO_MANY_METHODS) {
      // hack for big classes
      List<String> signatures = CglibCollectionUtils.transform(methods, new Transformer<Method, String>() {
        public String transform(Method obj) {
          final String s = CglibReflectUtils.getSignature(obj).toString();
          return s.substring(0, s.lastIndexOf(')') + 1);
        }
      });
      e.load_args();
      e.invoke_static(FAST_CLASS, GET_SIGNATURE_WITHOUT_RETURN_TYPE);
      signatureSwitchHelper(e, signatures);
    }
    else {
      e.load_args();
      List<MethodInfo> info = CglibCollectionUtils.transform(methods, MethodInfoTransformer.getInstance());
      EmitUtils.methodSwitch(e, info, new GetIndexCallback(e, info));
    }
    e.end_method();
  }

  private void signatureSwitchHelper(final CodeEmitter e, final List<String> signatures) {
    ObjectSwitchCallback callback = new ObjectSwitchCallback() {
      public void processCase(Object key, Label end) {
        // TODO: remove linear indexOf
        e.push(signatures.indexOf(key));
        e.return_value();
      }

      public void processDefault() {
        e.push(-1);
        e.return_value();
      }
    };
    EmitUtils.stringSwitch(e, signatures.toArray(new String[signatures.size()]), SWITCH_STYLE_HASH, callback);
  }

  private static void invokeSwitchHelper(final CodeEmitter e, List members, final int arg, final Type base) {
    final List<MethodInfo> info = CglibCollectionUtils.transform(members, MethodInfoTransformer.getInstance());
    final Label illegalArg = e.make_label();
    final Block block = e.begin_block();
    e.process_switch(getIntRange(info.size()), new ProcessSwitchCallback() {
      public void processCase(int key, Label end) {
        MethodInfo method = info.get(key);
        Type[] types = method.getSignature().getArgumentTypes();
        for (int i = 0; i < types.length; i++) {
          e.load_arg(arg);
          e.aaload(i);
          e.unbox(types[i]);
        }
        // TODO: change method lookup process so MethodInfo will already reference base
        // instead of superclass when superclass method is inaccessible
        e.invoke(method, base);
        if (!TypeUtils.isConstructor(method)) {
          e.box(method.getSignature().getReturnType());
        }
        e.return_value();
      }

      public void processDefault() {
        e.goTo(illegalArg);
      }
    });
    block.end();
    EmitUtils.wrapThrowable(block, INVOCATION_TARGET_EXCEPTION);
    e.mark(illegalArg);
    e.throw_exception(ILLEGAL_ARGUMENT_EXCEPTION, "Cannot find matching method/constructor");
  }

  private static class GetIndexCallback implements ObjectSwitchCallback {
    private CodeEmitter e;
    private HashMap<Object, Integer> indexes = new HashMap<>();

    public GetIndexCallback(CodeEmitter e, List methods) {
      this.e = e;
      int index = 0;
      for (Object object : methods) {
        indexes.put(object, Integer.valueOf(index++));
      }
    }

    public void processCase(Object key, Label end) {
      e.push(indexes.get(key).intValue());
      e.return_value();
    }

    public void processDefault() {
      e.push(-1);
      e.return_value();
    }
  }

  private static int[] getIntRange(int length) {
    int[] range = new int[length];
    for (int i = 0; i < length; i++) {
      range[i] = i;
    }
    return range;
  }
}
