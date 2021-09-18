// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package cn.taketoday.asm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import cn.taketoday.asm.commons.MethodSignature;
import cn.taketoday.core.Constant;
import cn.taketoday.util.CollectionUtils;

/**
 * A Java field or method type. This class can be used to make it easier to manipulate type and
 * method descriptors.
 *
 * @author Eric Bruneton
 * @author Chris Nokleberg
 */
public final class Type {

  /** The sort of the {@code void} type. See {@link #getSort}. */
  public static final int VOID = 0;

  /** The sort of the {@code boolean} type. See {@link #getSort}. */
  public static final int BOOLEAN = 1;

  /** The sort of the {@code char} type. See {@link #getSort}. */
  public static final int CHAR = 2;

  /** The sort of the {@code byte} type. See {@link #getSort}. */
  public static final int BYTE = 3;

  /** The sort of the {@code short} type. See {@link #getSort}. */
  public static final int SHORT = 4;

  /** The sort of the {@code int} type. See {@link #getSort}. */
  public static final int INT = 5;

  /** The sort of the {@code float} type. See {@link #getSort}. */
  public static final int FLOAT = 6;

  /** The sort of the {@code long} type. See {@link #getSort}. */
  public static final int LONG = 7;

  /** The sort of the {@code double} type. See {@link #getSort}. */
  public static final int DOUBLE = 8;

  /** The sort of array reference types. See {@link #getSort}. */
  public static final int ARRAY = 9;

  /** The sort of object reference types. See {@link #getSort}. */
  public static final int OBJECT = 10;

  /** The sort of method types. See {@link #getSort}. */
  public static final int METHOD = 11;

  /** The (private) sort of object reference types represented with an internal name. */
  private static final int INTERNAL = 12;

  /** The descriptors of the primitive types. */
  private static final String PRIMITIVE_DESCRIPTORS = "VZCBSIFJD";

  /** The {@code void} type. */
  public static final Type VOID_TYPE = new Type(VOID, PRIMITIVE_DESCRIPTORS, VOID, VOID + 1);

  /** The {@code boolean} type. */
  public static final Type BOOLEAN_TYPE =
          new Type(BOOLEAN, PRIMITIVE_DESCRIPTORS, BOOLEAN, BOOLEAN + 1);

  /** The {@code char} type. */
  public static final Type CHAR_TYPE = new Type(CHAR, PRIMITIVE_DESCRIPTORS, CHAR, CHAR + 1);

  /** The {@code byte} type. */
  public static final Type BYTE_TYPE = new Type(BYTE, PRIMITIVE_DESCRIPTORS, BYTE, BYTE + 1);

  /** The {@code short} type. */
  public static final Type SHORT_TYPE = new Type(SHORT, PRIMITIVE_DESCRIPTORS, SHORT, SHORT + 1);

  /** The {@code int} type. */
  public static final Type INT_TYPE = new Type(INT, PRIMITIVE_DESCRIPTORS, INT, INT + 1);

  /** The {@code float} type. */
  public static final Type FLOAT_TYPE = new Type(FLOAT, PRIMITIVE_DESCRIPTORS, FLOAT, FLOAT + 1);

  /** The {@code long} type. */
  public static final Type LONG_TYPE = new Type(LONG, PRIMITIVE_DESCRIPTORS, LONG, LONG + 1);

  /** The {@code double} type. */
  public static final Type DOUBLE_TYPE =
          new Type(DOUBLE, PRIMITIVE_DESCRIPTORS, DOUBLE, DOUBLE + 1);

  private static final HashMap<String, String> transforms = new HashMap<>();
  private static final HashMap<String, String> rtransforms = new HashMap<>();

  static {
    transforms.put("void", "V");
    transforms.put("byte", "B");
    transforms.put("char", "C");
    transforms.put("double", "D");
    transforms.put("float", "F");
    transforms.put("int", "I");
    transforms.put("long", "J");
    transforms.put("short", "S");
    transforms.put("boolean", "Z");

    CollectionUtils.reverse(transforms, rtransforms);
  }

  public static final Type TYPE_CONSTANT = Type.fromClass(Constant.class);
  public static final Type TYPE_OBJECT_ARRAY = Type.parse("Object[]");
  public static final Type TYPE_CLASS_ARRAY = Type.parse("Class[]");
  public static final Type TYPE_STRING_ARRAY = Type.parse("String[]");
  public static final Type TYPE_TYPE = Type.fromClass(Type.class);
  public static final Type TYPE_ERROR = Type.parse("Error");
  public static final Type TYPE_SYSTEM = Type.parse("System");
  public static final Type TYPE_LONG = Type.parse("Long");
  public static final Type TYPE_BYTE = Type.parse("Byte");
  public static final Type TYPE_CLASS = Type.parse("Class");
  public static final Type TYPE_FLOAT = Type.parse("Float");
  public static final Type TYPE_SHORT = Type.parse("Short");
  public static final Type TYPE_OBJECT = Type.parse("Object");
  public static final Type TYPE_DOUBLE = Type.parse("Double");
  public static final Type TYPE_STRING = Type.parse("String");
  public static final Type TYPE_NUMBER = Type.parse("Number");
  public static final Type TYPE_BOOLEAN = Type.parse("Boolean");
  public static final Type TYPE_INTEGER = Type.parse("Integer");
  public static final Type TYPE_CHARACTER = Type.parse("Character");
  public static final Type TYPE_THROWABLE = Type.parse("Throwable");
  public static final Type TYPE_CLASS_LOADER = Type.parse("ClassLoader");
  public static final Type TYPE_STRING_BUFFER = Type.parse("StringBuffer");
  public static final Type TYPE_BIG_INTEGER = Type.parse("java.math.BigInteger");
  public static final Type TYPE_BIG_DECIMAL = Type.parse("java.math.BigDecimal");
  public static final Type TYPE_RUNTIME_EXCEPTION = Type.parse("RuntimeException");
  public static final Type TYPE_SIGNATURE = Type.fromClass(MethodSignature.class);

  // -----------------------------------------------------------------------------------------------
  // Fields
  // -----------------------------------------------------------------------------------------------

  /**
   * The sort of this type. Either {@link #VOID}, {@link #BOOLEAN}, {@link #CHAR}, {@link #BYTE},
   * {@link #SHORT}, {@link #INT}, {@link #FLOAT}, {@link #LONG}, {@link #DOUBLE}, {@link #ARRAY},
   * {@link #OBJECT}, {@link #METHOD} or {@link #INTERNAL}.
   */
  private final int sort;

  /**
   * A buffer containing the value of this field or method type. This value is an internal name for
   * {@link #OBJECT} and {@link #INTERNAL} types, and a field or method descriptor in the other
   * cases.
   *
   * <p>For {@link #OBJECT} types, this field also contains the descriptor: the characters in
   * [{@link #valueBegin},{@link #valueEnd}) contain the internal name, and those in [{@link
   * #valueBegin} - 1, {@link #valueEnd} + 1) contain the descriptor.
   */
  private final String valueBuffer;

  /**
   * The beginning index, inclusive, of the value of this Java field or method type in {@link
   * #valueBuffer}. This value is an internal name for {@link #OBJECT} and {@link #INTERNAL} types,
   * and a field or method descriptor in the other cases.
   */
  private final int valueBegin;

  /**
   * The end index, exclusive, of the value of this Java field or method type in {@link
   * #valueBuffer}. This value is an internal name for {@link #OBJECT} and {@link #INTERNAL} types,
   * and a field or method descriptor in the other cases.
   */
  private final int valueEnd;

  /**
   * Constructs a reference type.
   *
   * @param sort
   *         the sort of this type, see {@link #sort}.
   * @param valueBuffer
   *         a buffer containing the value of this field or method type.
   * @param valueBegin
   *         the beginning index, inclusive, of the value of this field or method type in
   *         valueBuffer.
   * @param valueEnd
   *         the end index, exclusive, of the value of this field or method type in
   *         valueBuffer.
   */
  private Type(final int sort, final String valueBuffer, final int valueBegin, final int valueEnd) {
    this.sort = sort;
    this.valueBuffer = valueBuffer;
    this.valueBegin = valueBegin;
    this.valueEnd = valueEnd;
  }

  // -----------------------------------------------------------------------------------------------
  // Methods to get Type(s) from a descriptor, a reflected Method or Constructor, other types, etc.
  // -----------------------------------------------------------------------------------------------

  /**
   * @since 4.0
   */
  public static Type parse(String s) {
    return fromDescriptor(map(s));
  }

  public static String map(String type) {
    if (Constant.BLANK.equals(type)) {
      return type;
    }
    String t = transforms.get(type);
    if (t != null) {
      return t;
    }
    else if (type.indexOf('.') < 0) {
      return map("java.lang." + type);
    }
    else {
      StringBuilder sb = new StringBuilder();
      int index = 0;
      while ((index = type.indexOf("[]", index) + 1) > 0) {
        sb.append('[');
      }
      type = type.substring(0, type.length() - sb.length() * 2);
      sb.append('L').append(type.replace('.', '/')).append(';');
      return sb.toString();
    }
  }

  /**
   * Returns the {@link Type} corresponding to the given type descriptor.
   *
   * @param typeDescriptor
   *         a field or method type descriptor.
   *
   * @return the {@link Type} corresponding to the given type descriptor.
   */
  public static Type fromDescriptor(final String typeDescriptor) {
    return getTypeInternal(typeDescriptor, 0, typeDescriptor.length());
  }

  /**
   * Returns the {@link Type} corresponding to the given class.
   *
   * @param clazz
   *         a class.
   *
   * @return the {@link Type} corresponding to the given class.
   */
  public static Type fromClass(final Class<?> clazz) {
    if (clazz.isPrimitive()) {
      if (clazz == Integer.TYPE) {
        return INT_TYPE;
      }
      else if (clazz == Void.TYPE) {
        return VOID_TYPE;
      }
      else if (clazz == Boolean.TYPE) {
        return BOOLEAN_TYPE;
      }
      else if (clazz == Byte.TYPE) {
        return BYTE_TYPE;
      }
      else if (clazz == Character.TYPE) {
        return CHAR_TYPE;
      }
      else if (clazz == Short.TYPE) {
        return SHORT_TYPE;
      }
      else if (clazz == Double.TYPE) {
        return DOUBLE_TYPE;
      }
      else if (clazz == Float.TYPE) {
        return FLOAT_TYPE;
      }
      else if (clazz == Long.TYPE) {
        return LONG_TYPE;
      }
      else {
        throw new AssertionError();
      }
    }
    else {
      return fromDescriptor(getDescriptor(clazz));
    }
  }

  /**
   * Returns the method {@link Type} corresponding to the given constructor.
   *
   * @param constructor
   *         a {@link Constructor} object.
   *
   * @return the method {@link Type} corresponding to the given constructor.
   */
  public static Type fromConstructor(final Constructor<?> constructor) {
    return fromDescriptor(getConstructorDescriptor(constructor));
  }

  /**
   * Returns the method {@link Type} corresponding to the given method.
   *
   * @param method
   *         a {@link Method} object.
   *
   * @return the method {@link Type} corresponding to the given method.
   */
  public static Type fromMethod(final Method method) {
    return fromDescriptor(getMethodDescriptor(method));
  }

  /**
   * Returns the type of the elements of this array type. This method should only be used for an
   * array type.
   *
   * @return Returns the type of the elements of this array type.
   */
  public Type getElementType() {
    final int numDimensions = getDimensions();
    return getTypeInternal(valueBuffer, valueBegin + numDimensions, valueEnd);
  }

  /**
   * Returns the {@link Type} corresponding to the given internal name.
   *
   * @param internalName
   *         an internal name.
   *
   * @return the {@link Type} corresponding to the given internal name.
   */
  public static Type fromInternalName(final String internalName) {
    return new Type(
            internalName.charAt(0) == '[' ? ARRAY : INTERNAL, internalName, 0, internalName.length());
  }

  /**
   * Returns the {@link Type}s corresponding to the given internal name.
   *
   * @param internalNames
   *         internal name. if null returns null
   *
   * @return the {@link Type}s corresponding to the given internal name.
   */
  public static Type[] getObjectTypes(final String[] internalNames) {
    if (internalNames == null) {
      return null;
    }
    Type[] ret = new Type[internalNames.length];
    int i = 0;
    for (final String internalName : internalNames) {
      ret[i++] = fromInternalName(internalName);
    }
    return ret;
  }

  /**
   * Returns the {@link Type} corresponding to the given method descriptor. Equivalent to <code>
   * Type.getType(methodDescriptor)</code>.
   *
   * @param methodDescriptor
   *         a method descriptor.
   *
   * @return the {@link Type} corresponding to the given method descriptor.
   */
  public static Type fromMethod(final String methodDescriptor) {
    return new Type(METHOD, methodDescriptor, 0, methodDescriptor.length());
  }

  /**
   * Returns the method {@link Type} corresponding to the given argument and return types.
   *
   * @param returnType
   *         the return type of the method.
   * @param argumentTypes
   *         the argument types of the method.
   *
   * @return the method {@link Type} corresponding to the given argument and return types.
   */
  public static Type fromMethod(final Type returnType, final Type... argumentTypes) {
    return fromDescriptor(getMethodDescriptor(returnType, argumentTypes));
  }

  /**
   * Returns the argument types of methods of this type. This method should only be used for method
   * types.
   *
   * @return the argument types of methods of this type.
   */
  public Type[] getArgumentTypes() {
    return getArgumentTypes(getDescriptor());
  }

  /**
   * Returns the {@link Type} values corresponding to the argument types of the given method
   * descriptor.
   *
   * @param methodDescriptor
   *         a method descriptor.
   *
   * @return the {@link Type} values corresponding to the argument types of the given method
   * descriptor.
   */
  public static Type[] getArgumentTypes(final String methodDescriptor) {
    // First step: compute the number of argument types in methodDescriptor.
    int numArgumentTypes = 0;
    // Skip the first character, which is always a '('.
    int currentOffset = 1;
    // Parse the argument types, one at a each loop iteration.
    while (methodDescriptor.charAt(currentOffset) != ')') {
      while (methodDescriptor.charAt(currentOffset) == '[') {
        currentOffset++;
      }
      if (methodDescriptor.charAt(currentOffset++) == 'L') {
        // Skip the argument descriptor content.
        int semiColumnOffset = methodDescriptor.indexOf(';', currentOffset);
        currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
      }
      ++numArgumentTypes;
    }

    // Second step: create a Type instance for each argument type.
    Type[] argumentTypes = new Type[numArgumentTypes];
    // Skip the first character, which is always a '('.
    currentOffset = 1;
    // Parse and create the argument types, one at each loop iteration.
    int currentArgumentTypeIndex = 0;
    while (methodDescriptor.charAt(currentOffset) != ')') {
      final int currentArgumentTypeOffset = currentOffset;
      while (methodDescriptor.charAt(currentOffset) == '[') {
        currentOffset++;
      }
      if (methodDescriptor.charAt(currentOffset++) == 'L') {
        // Skip the argument descriptor content.
        int semiColumnOffset = methodDescriptor.indexOf(';', currentOffset);
        currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
      }
      argumentTypes[currentArgumentTypeIndex++] =
              getTypeInternal(methodDescriptor, currentArgumentTypeOffset, currentOffset);
    }
    return argumentTypes;
  }

  /**
   * Returns the {@link Type} values corresponding to the argument types of the given method.
   *
   * @param method
   *         a method.
   *
   * @return the {@link Type} values corresponding to the argument types of the given method.
   */
  public static Type[] getArgumentTypes(final Method method) {
    Class<?>[] classes = method.getParameterTypes();
    Type[] types = new Type[classes.length];
    for (int i = classes.length - 1; i >= 0; --i) {
      types[i] = fromClass(classes[i]);
    }
    return types;
  }

  /**
   * Returns the return type of methods of this type. This method should only be used for method
   * types.
   *
   * @return the return type of methods of this type.
   */
  public Type getReturnType() {
    return forReturnType(getDescriptor());
  }

  /**
   * Returns the {@link Type} corresponding to the return type of the given method descriptor.
   *
   * @param methodDescriptor
   *         a method descriptor.
   *
   * @return the {@link Type} corresponding to the return type of the given method descriptor.
   */
  public static Type forReturnType(final String methodDescriptor) {
    return getTypeInternal(
            methodDescriptor, getReturnTypeOffset(methodDescriptor), methodDescriptor.length());
  }

  /**
   * Returns the {@link Type} corresponding to the return type of the given method.
   *
   * @param method
   *         a method.
   *
   * @return the {@link Type} corresponding to the return type of the given method.
   */
  public static Type forReturnType(final Method method) {
    return fromClass(method.getReturnType());
  }

  /**
   * Returns the start index of the return type of the given method descriptor.
   *
   * @param methodDescriptor
   *         a method descriptor.
   *
   * @return the start index of the return type of the given method descriptor.
   */
  static int getReturnTypeOffset(final String methodDescriptor) {
    // Skip the first character, which is always a '('.
    int currentOffset = 1;
    // Skip the argument types, one at a each loop iteration.
    while (methodDescriptor.charAt(currentOffset) != ')') {
      while (methodDescriptor.charAt(currentOffset) == '[') {
        currentOffset++;
      }
      if (methodDescriptor.charAt(currentOffset++) == 'L') {
        // Skip the argument descriptor content.
        int semiColumnOffset = methodDescriptor.indexOf(';', currentOffset);
        currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
      }
    }
    return currentOffset + 1;
  }

  /**
   * Returns the {@link Type} corresponding to the given field or method descriptor.
   *
   * @param descriptorBuffer
   *         a buffer containing the field or method descriptor.
   * @param descriptorBegin
   *         the beginning index, inclusive, of the field or method descriptor in
   *         descriptorBuffer.
   * @param descriptorEnd
   *         the end index, exclusive, of the field or method descriptor in
   *         descriptorBuffer.
   *
   * @return the {@link Type} corresponding to the given type descriptor.
   */
  private static Type getTypeInternal(
          final String descriptorBuffer, final int descriptorBegin, final int descriptorEnd) {
    switch (descriptorBuffer.charAt(descriptorBegin)) {
      case 'V':
        return VOID_TYPE;
      case 'Z':
        return BOOLEAN_TYPE;
      case 'C':
        return CHAR_TYPE;
      case 'B':
        return BYTE_TYPE;
      case 'S':
        return SHORT_TYPE;
      case 'I':
        return INT_TYPE;
      case 'F':
        return FLOAT_TYPE;
      case 'J':
        return LONG_TYPE;
      case 'D':
        return DOUBLE_TYPE;
      case '[':
        return new Type(ARRAY, descriptorBuffer, descriptorBegin, descriptorEnd);
      case 'L':
        return new Type(OBJECT, descriptorBuffer, descriptorBegin + 1, descriptorEnd - 1);
      case '(':
        return new Type(METHOD, descriptorBuffer, descriptorBegin, descriptorEnd);
      default:
        throw new IllegalArgumentException();
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Methods to get class names, internal names or descriptors.
  // -----------------------------------------------------------------------------------------------

  /**
   * Returns the binary name of the class corresponding to this type. This method must not be used
   * on method types.
   *
   * @return the binary name of the class corresponding to this type.
   */
  public String getClassName() {
    switch (sort) {
      case VOID:
        return "void";
      case BOOLEAN:
        return "boolean";
      case CHAR:
        return "char";
      case BYTE:
        return "byte";
      case SHORT:
        return "short";
      case INT:
        return "int";
      case FLOAT:
        return "float";
      case LONG:
        return "long";
      case DOUBLE:
        return "double";
      case ARRAY:
        StringBuilder stringBuilder = new StringBuilder(getElementType().getClassName());
        for (int i = getDimensions(); i > 0; --i) {
          stringBuilder.append("[]");
        }
        return stringBuilder.toString();
      case OBJECT:
      case INTERNAL:
        return valueBuffer.substring(valueBegin, valueEnd).replace('/', '.');
      default:
        throw new AssertionError();
    }
  }

  /**
   * Returns the internal name of the class corresponding to this object or array type. The internal
   * name of a class is its fully qualified name (as returned by Class.getName(), where '.' are
   * replaced by '/'). This method should only be used for an object or array type.
   *
   * @return the internal name of the class corresponding to this object type.
   */
  public String getInternalName() {
    return valueBuffer.substring(valueBegin, valueEnd);
  }

  /**
   * Returns the internal name of the given class. The internal name of a class is its fully
   * qualified name, as returned by Class.getName(), where '.' are replaced by '/'.
   *
   * @param clazz
   *         an object or array class.
   *
   * @return the internal name of the given class.
   */
  public static String getInternalName(final Class<?> clazz) {
    return clazz.getName().replace('.', '/');
  }

  /**
   * Returns the descriptor corresponding to this type.
   *
   * @return the descriptor corresponding to this type.
   */
  public String getDescriptor() {
    if (sort == OBJECT) {
      return valueBuffer.substring(valueBegin - 1, valueEnd + 1);
    }
    else if (sort == INTERNAL) {
      return 'L' + valueBuffer.substring(valueBegin, valueEnd) + ';';
    }
    else {
      return valueBuffer.substring(valueBegin, valueEnd);
    }
  }

  /**
   * Returns the descriptor corresponding to the given class.
   *
   * @param clazz
   *         an object class, a primitive class or an array class.
   *
   * @return the descriptor corresponding to the given class.
   */
  public static String getDescriptor(final Class<?> clazz) {
    StringBuilder stringBuilder = new StringBuilder();
    appendDescriptor(clazz, stringBuilder);
    return stringBuilder.toString();
  }

  /**
   * Returns the descriptor corresponding to the given constructor.
   *
   * @param constructor
   *         a {@link Constructor} object.
   *
   * @return the descriptor of the given constructor.
   */
  public static String getConstructorDescriptor(final Constructor<?> constructor) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append('(');
    Class<?>[] parameters = constructor.getParameterTypes();
    for (Class<?> parameter : parameters) {
      appendDescriptor(parameter, stringBuilder);
    }
    return stringBuilder.append(")V").toString();
  }

  /**
   * Returns the descriptor corresponding to the given argument and return types.
   *
   * @param returnType
   *         the return type of the method.
   * @param argumentTypes
   *         the argument types of the method.
   *
   * @return the descriptor corresponding to the given argument and return types.
   */
  public static String getMethodDescriptor(final Type returnType, final Type... argumentTypes) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append('(');
    for (Type argumentType : argumentTypes) {
      argumentType.appendDescriptor(stringBuilder);
    }
    stringBuilder.append(')');
    returnType.appendDescriptor(stringBuilder);
    return stringBuilder.toString();
  }

  /**
   * Returns the descriptor corresponding to the given method.
   *
   * @param method
   *         a {@link Method} object.
   *
   * @return the descriptor of the given method.
   */
  public static String getMethodDescriptor(final Method method) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append('(');
    Class<?>[] parameters = method.getParameterTypes();
    for (Class<?> parameter : parameters) {
      appendDescriptor(parameter, stringBuilder);
    }
    stringBuilder.append(')');
    appendDescriptor(method.getReturnType(), stringBuilder);
    return stringBuilder.toString();
  }

  /**
   * Appends the descriptor corresponding to this type to the given string buffer.
   *
   * @param stringBuilder
   *         the string builder to which the descriptor must be appended.
   */
  private void appendDescriptor(final StringBuilder stringBuilder) {
    if (sort == OBJECT) {
      stringBuilder.append(valueBuffer, valueBegin - 1, valueEnd + 1);
    }
    else if (sort == INTERNAL) {
      stringBuilder.append('L').append(valueBuffer, valueBegin, valueEnd).append(';');
    }
    else {
      stringBuilder.append(valueBuffer, valueBegin, valueEnd);
    }
  }

  /**
   * Appends the descriptor of the given class to the given string builder.
   *
   * @param clazz
   *         the class whose descriptor must be computed.
   * @param stringBuilder
   *         the string builder to which the descriptor must be appended.
   */
  private static void appendDescriptor(final Class<?> clazz, final StringBuilder stringBuilder) {
    Class<?> currentClass = clazz;
    while (currentClass.isArray()) {
      stringBuilder.append('[');
      currentClass = currentClass.getComponentType();
    }
    if (currentClass.isPrimitive()) {
      char descriptor;
      if (currentClass == Integer.TYPE) {
        descriptor = 'I';
      }
      else if (currentClass == Void.TYPE) {
        descriptor = 'V';
      }
      else if (currentClass == Boolean.TYPE) {
        descriptor = 'Z';
      }
      else if (currentClass == Byte.TYPE) {
        descriptor = 'B';
      }
      else if (currentClass == Character.TYPE) {
        descriptor = 'C';
      }
      else if (currentClass == Short.TYPE) {
        descriptor = 'S';
      }
      else if (currentClass == Double.TYPE) {
        descriptor = 'D';
      }
      else if (currentClass == Float.TYPE) {
        descriptor = 'F';
      }
      else if (currentClass == Long.TYPE) {
        descriptor = 'J';
      }
      else {
        throw new AssertionError();
      }
      stringBuilder.append(descriptor);
    }
    else {
      stringBuilder.append('L').append(getInternalName(currentClass)).append(';');
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Methods to get the sort, dimension, size, and opcodes corresponding to a Type or descriptor.
  // -----------------------------------------------------------------------------------------------

  /**
   * Returns the sort of this type.
   *
   * @return {@link #VOID}, {@link #BOOLEAN}, {@link #CHAR}, {@link #BYTE}, {@link #SHORT}, {@link
   * #INT}, {@link #FLOAT}, {@link #LONG}, {@link #DOUBLE}, {@link #ARRAY}, {@link #OBJECT} or
   * {@link #METHOD}.
   */
  public int getSort() {
    return sort == INTERNAL ? OBJECT : sort;
  }

  /**
   * Returns the number of dimensions of this array type. This method should only be used for an
   * array type.
   *
   * @return the number of dimensions of this array type.
   */
  public int getDimensions() {
    int numDimensions = 1;
    while (valueBuffer.charAt(valueBegin + numDimensions) == '[') {
      numDimensions++;
    }
    return numDimensions;
  }

  /**
   * Returns the size of values of this type. This method must not be used for method types.
   *
   * @return the size of values of this type, i.e., 2 for {@code long} and {@code double}, 0 for
   * {@code void} and 1 otherwise.
   */
  public int getSize() {
    switch (sort) {
      case VOID:
        return 0;
      case BOOLEAN:
      case CHAR:
      case BYTE:
      case SHORT:
      case INT:
      case FLOAT:
      case ARRAY:
      case OBJECT:
      case INTERNAL:
        return 1;
      case LONG:
      case DOUBLE:
        return 2;
      default:
        throw new AssertionError();
    }
  }

  /**
   * Returns the size of the arguments and of the return value of methods of this type. This method
   * should only be used for method types.
   *
   * @return the size of the arguments of the method (plus one for the implicit this argument),
   * argumentsSize, and the size of its return value, returnSize, packed into a single int i =
   * {@code (argumentsSize &lt;&lt; 2) | returnSize} (argumentsSize is therefore equal to {@code
   * i &gt;&gt; 2}, and returnSize to {@code i &amp; 0x03}).
   */
  public int getArgumentsAndReturnSizes() {
    return getArgumentsAndReturnSizes(getDescriptor());
  }

  /**
   * Computes the size of the arguments and of the return value of a method.
   *
   * @param methodDescriptor
   *         a method descriptor.
   *
   * @return the size of the arguments of the method (plus one for the implicit this argument),
   * argumentsSize, and the size of its return value, returnSize, packed into a single int i =
   * {@code (argumentsSize &lt;&lt; 2) | returnSize} (argumentsSize is therefore equal to {@code
   * i &gt;&gt; 2}, and returnSize to {@code i &amp; 0x03}).
   */
  public static int getArgumentsAndReturnSizes(final String methodDescriptor) {
    int argumentsSize = 1;
    // Skip the first character, which is always a '('.
    int currentOffset = 1;
    int currentChar = methodDescriptor.charAt(currentOffset);
    // Parse the argument types and compute their size, one at a each loop iteration.
    while (currentChar != ')') {
      if (currentChar == 'J' || currentChar == 'D') {
        currentOffset++;
        argumentsSize += 2;
      }
      else {
        while (methodDescriptor.charAt(currentOffset) == '[') {
          currentOffset++;
        }
        if (methodDescriptor.charAt(currentOffset++) == 'L') {
          // Skip the argument descriptor content.
          int semiColumnOffset = methodDescriptor.indexOf(';', currentOffset);
          currentOffset = Math.max(currentOffset, semiColumnOffset + 1);
        }
        argumentsSize += 1;
      }
      currentChar = methodDescriptor.charAt(currentOffset);
    }
    currentChar = methodDescriptor.charAt(currentOffset + 1);
    if (currentChar == 'V') {
      return argumentsSize << 2;
    }
    else {
      int returnSize = (currentChar == 'J' || currentChar == 'D') ? 2 : 1;
      return argumentsSize << 2 | returnSize;
    }
  }

  /**
   * Returns a JVM instruction opcode adapted to this {@link Type}. This method must not be used for
   * method types.
   *
   * @param opcode
   *         a JVM instruction opcode. This opcode must be one of ILOAD, ISTORE, IALOAD,
   *         IASTORE, IADD, ISUB, IMUL, IDIV, IREM, INEG, ISHL, ISHR, IUSHR, IAND, IOR, IXOR and
   *         IRETURN.
   *
   * @return an opcode that is similar to the given opcode, but adapted to this {@link Type}. For
   * example, if this type is {@code float} and {@code opcode} is IRETURN, this method returns
   * FRETURN.
   */
  public int getOpcode(final int opcode) {
    if (opcode == Opcodes.IALOAD || opcode == Opcodes.IASTORE) {
      switch (sort) {
        case BOOLEAN:
        case BYTE:
          return opcode + (Opcodes.BALOAD - Opcodes.IALOAD);
        case CHAR:
          return opcode + (Opcodes.CALOAD - Opcodes.IALOAD);
        case SHORT:
          return opcode + (Opcodes.SALOAD - Opcodes.IALOAD);
        case INT:
          return opcode;
        case FLOAT:
          return opcode + (Opcodes.FALOAD - Opcodes.IALOAD);
        case LONG:
          return opcode + (Opcodes.LALOAD - Opcodes.IALOAD);
        case DOUBLE:
          return opcode + (Opcodes.DALOAD - Opcodes.IALOAD);
        case ARRAY:
        case OBJECT:
        case INTERNAL:
          return opcode + (Opcodes.AALOAD - Opcodes.IALOAD);
        case METHOD:
        case VOID:
          throw new UnsupportedOperationException();
        default:
          throw new AssertionError();
      }
    }
    else {
      switch (sort) {
        case VOID:
          if (opcode != Opcodes.IRETURN) {
            throw new UnsupportedOperationException();
          }
          return Opcodes.RETURN;
        case BOOLEAN:
        case BYTE:
        case CHAR:
        case SHORT:
        case INT:
          return opcode;
        case FLOAT:
          return opcode + (Opcodes.FRETURN - Opcodes.IRETURN);
        case LONG:
          return opcode + (Opcodes.LRETURN - Opcodes.IRETURN);
        case DOUBLE:
          return opcode + (Opcodes.DRETURN - Opcodes.IRETURN);
        case ARRAY:
        case OBJECT:
        case INTERNAL:
          if (opcode != Opcodes.ILOAD && opcode != Opcodes.ISTORE && opcode != Opcodes.IRETURN) {
            throw new UnsupportedOperationException();
          }
          return opcode + (Opcodes.ARETURN - Opcodes.IRETURN);
        case METHOD:
          throw new UnsupportedOperationException();
        default:
          throw new AssertionError();
      }
    }
  }

  /**
   * To {@link Type} array
   *
   * @param items
   *         array item
   *
   * @return {@link Type} array
   */
  public static Type[] array(final Type... items) {
    return items;
  }

  public static Type[] array(final Class<?>... items) {
    if (items == null) {
      return null;
    }
    int i = 0;
    Type[] ret = new Type[items.length];
    for (final Class<?> item : items) {
      ret[i++] = fromClass(item);
    }
    return ret;
  }

  public static Type[] array(String... items) {
    if (items == null) {
      return null;
    }
    int i = 0;
    Type[] ret = new Type[items.length];
    for (final String item : items) {
      ret[i++] = fromDescriptor(item);
    }
    return ret;
  }

  // isArray

  // @since 4.0
  public boolean isArray() {
    return sort == ARRAY;
  }

  // @since 4.0
  public boolean isPrimitive() {
    final int sort = getSort();
    return sort != ARRAY && sort != OBJECT;
  }

  // -----------------------------------------------------------------------------------------------
  // Equals, hashCode and toString.
  // -----------------------------------------------------------------------------------------------

  /**
   * Tests if the given object is equal to this type.
   *
   * @param object
   *         the object to be compared to this type.
   *
   * @return {@literal true} if the given object is equal to this type.
   */
  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof Type)) {
      return false;
    }
    Type other = (Type) object;
    if ((sort == INTERNAL ? OBJECT : sort) != (other.sort == INTERNAL ? OBJECT : other.sort)) {
      return false;
    }
    int begin = valueBegin;
    int end = valueEnd;
    int otherBegin = other.valueBegin;
    // Compare the values.
    if (end - begin != other.valueEnd - otherBegin) {
      return false;
    }
    final String thisValueBuffer = this.valueBuffer;
    final String otherValueBuffer = other.valueBuffer;
    for (int i = begin, j = otherBegin; i < end; i++, j++) {
      if (thisValueBuffer.charAt(i) != otherValueBuffer.charAt(j)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns a hash code value for this type.
   *
   * @return a hash code value for this type.
   */
  @Override
  public int hashCode() {
    int hashCode = 13 * (sort == INTERNAL ? OBJECT : sort);
    if (sort >= ARRAY) {
      final String valueBuffer = this.valueBuffer;
      for (int i = valueBegin, end = valueEnd; i < end; i++) {
        hashCode = 17 * (hashCode + valueBuffer.charAt(i));
      }
    }
    return hashCode;
  }

  /**
   * Returns a string representation of this type.
   *
   * @return the descriptor of this type.
   */
  @Override
  public String toString() {
    return getDescriptor();
  }
}
