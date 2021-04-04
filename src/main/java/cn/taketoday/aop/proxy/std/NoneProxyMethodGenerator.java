/*
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
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

package cn.taketoday.aop.proxy.std;

import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;

import cn.taketoday.aop.TargetSource;
import cn.taketoday.aop.proxy.AdvisedSupport;
import cn.taketoday.context.asm.Type;
import cn.taketoday.context.cglib.core.CglibReflectUtils;
import cn.taketoday.context.cglib.core.ClassEmitter;
import cn.taketoday.context.cglib.core.CodeEmitter;
import cn.taketoday.context.cglib.core.CodeGenerationException;
import cn.taketoday.context.cglib.core.EmitUtils;
import cn.taketoday.context.cglib.core.MethodInfo;
import cn.taketoday.context.cglib.core.Signature;
import cn.taketoday.context.utils.ObjectUtils;

/**
 * @author TODAY 2021/3/7 20:19
 * @since 3.0
 */
public class NoneProxyMethodGenerator implements ProxyMethodGenerator {

  static final Signature targetSourceGetTarget;
  static final Type targetSourceType = Type.getType(TargetSource.class);

  static {
    try {
      targetSourceGetTarget = new Signature(TargetSource.class.getDeclaredMethod("getTarget"));
    }
    catch (NoSuchMethodException e) {
      throw new CodeGenerationException(e);
    }
  }

  @Override
  public boolean generate(Method method, GeneratorContext context) {
    final AdvisedSupport config = context.getConfig();
    final MethodInterceptor[] interceptors = context.getConfig().getInterceptors(method, context.getTargetClass());

    if (ObjectUtils.isEmpty(interceptors)) {
      final TargetSource targetSource = config.getTargetSource();
      if (targetSource.isStatic()) {
        invokeStaticTarget(method, context);
      }
      else {
        invokeTargetFromTargetSource(method, context);
      }
      return true;
    }
    return false;
  }

  /**
   * <pre class="code">
   *   void none() {
   *     ((Bean) target).none();
   *   }
   * </pre>
   */
  protected void invokeStaticTarget(Method method, GeneratorContext context) {
    final ClassEmitter emitter = context.getClassEmitter();
    MethodInfo methodInfo = CglibReflectUtils.getMethodInfo(method);
    final CodeEmitter codeEmitter = EmitUtils.beginMethod(emitter, methodInfo, method.getModifiers());

    codeEmitter.load_this();

    codeEmitter.getfield(FIELD_TARGET);

    codeEmitter.load_args();
    codeEmitter.invoke(methodInfo);
    codeEmitter.return_value();

    codeEmitter.unbox_or_zero(Type.getType(method.getReturnType()));
    codeEmitter.end_method();
  }

  /**
   * <pre class="code">
   *   void noneStatic() {
   *     ((Bean) this.targetSource.getTarget()).noneStatic();
   *   }
   * </pre>
   */
  protected void invokeTargetFromTargetSource(Method method, GeneratorContext context) {
    final ClassEmitter emitter = context.getClassEmitter();
    MethodInfo methodInfo = CglibReflectUtils.getMethodInfo(method);
    final CodeEmitter codeEmitter = EmitUtils.beginMethod(emitter, methodInfo, method.getModifiers());

    // this.targetSource.getTarget()

    codeEmitter.load_this();
    codeEmitter.getfield(FIELD_TARGET_SOURCE);
    codeEmitter.invoke_interface(targetSourceType, targetSourceGetTarget);

    // cast

    codeEmitter.checkcast(context.getTargetType());
    codeEmitter.load_args();
    codeEmitter.invoke(methodInfo);
    codeEmitter.return_value();

    codeEmitter.unbox_or_zero(Type.getType(method.getReturnType()));
    codeEmitter.end_method();
  }
}
