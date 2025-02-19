/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.taketoday.expression;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import cn.taketoday.core.TypeDescriptor;
import cn.taketoday.core.conversion.ConversionService;
import cn.taketoday.core.conversion.MatchingConverter;
import cn.taketoday.core.conversion.support.DefaultConversionService;
import cn.taketoday.expression.lang.EvaluationContext;

/**
 * Context information for expression parsing and evaluation.
 *
 * <p>
 * To parse or evaluate an {@link Expression}, an <code>ELContext</code> must be
 * provided. The <code>ELContext</code> holds:
 * <ul>
 * <li>a reference to {@link FunctionMapper} that will be used to resolve EL
 * Functions. This is used only in parsing.</li>
 * <li>a reference to {@link VariableMapper} that will be used to resolve EL
 * Variables. This is used only in parsing.</li>
 * <li>a reference to the base {@link ExpressionResolver} that will be consulted to
 * resolve model objects and their properties</li>
 * <li>a collection of all the relevant context objects for use by
 * <code>ELResolver</code>s</li>
 * <li>state information during the evaluation of an expression, such as whether
 * a property has been resolved yet</li>
 * <li>a reference to {@link ImportHandler} that will be consulted to resolve
 * classes that have been imported</li>
 * <li>a reference to the arguments for the active
 * {@link LambdaExpression}s</li>
 * <li>a reference to the list of registered evaluation listeners</li>
 * </ul>
 * </p>
 *
 * <p>
 * The collection of context objects is necessary because each
 * <code>ELResolver</code> may need access to a different context object. For
 * example, JSP and Faces resolvers need access to a jakarta.servlet.jsp.JspContext
 * </p>
 *
 * <p>
 * When used in a web container, the creation of <code>ELContext</code> objects
 * is controlled through the underlying technology. For example, in JSP the
 * <code>JspContext.getELContext()</code> factory method is used. Some
 * technologies provide the ability to add an ELContextListener so that
 * applications and frameworks can ensure their own context objects are attached
 * to any newly created <code>ELContext</code>.
 * </p>
 *
 * <p>
 * When used in a stand-alone environment, {@link StandardExpressionContext} provides a
 * default <code>ELContext</code>, which is managed and modified by
 * {@link ExpressionManager}.
 *
 * <p>
 * Because it stores state during expression evaluation, an
 * <code>ELContext</code> object is not thread-safe. Care should be taken to
 * never share an <code>ELContext</code> instance between two or more threads.
 * </p>
 *
 * @see ExpressionResolver
 * @see FunctionMapper
 * @see VariableMapper
 * @see ImportHandler
 * @see LambdaExpression
 * @see StandardExpressionContext
 * @since EL 2.1 and EL 3.0
 */
public abstract class ExpressionContext {

  private boolean resolved;
  private HashMap<Class<?>, Object> map;
  private Stack<Map<String, Object>> lambdaArgs;

  private ImportHandler importHandler;

  /**
   * Holds value of property locale.
   */
  private Locale locale;

  /** @since 3.0.4 */
  private ConversionService conversionService;

  /**
   * Called to indicate that a <code>ELResolver</code> has successfully resolved a
   * given (base, property) pair. Use {@link #setPropertyResolved(Object, Object)}
   * if resolved is true.
   *
   * <p>
   * The {@link ExpressionResolverComposite} checks this property to determine whether it
   * should consider or skip other component resolvers.
   * </p>
   *
   * @param resolved true if the property has been resolved, or false if not.
   * @see ExpressionResolverComposite
   */
  public void setPropertyResolved(boolean resolved) {
    this.resolved = resolved;
  }

  /**
   * Called to indicate that a <code>ELResolver</code> has successfully resolved a
   * given (base, property) pair.
   *
   * <p>
   * The {@link ExpressionResolverComposite} checks this property to determine whether it
   * should consider or skip other component resolvers.
   * </p>
   *
   * @param base The base object
   * @param property The property object
   * @see ExpressionResolverComposite
   * @since EL 3.0
   */
  public void setPropertyResolved(Object base, Object property) {
    // Don't set the variable here, for 2.2 users ELContext may be overridden or delegated.
    setPropertyResolved(true);
  }

  /**
   * Returns whether an {@link ExpressionResolver} has successfully resolved a given
   * (base, property) pair.
   *
   * <p>
   * The {@link ExpressionResolverComposite} checks this property to determine whether it
   * should consider or skip other component resolvers.
   * </p>
   *
   * @return true if the property has been resolved, or false if not.
   * @see ExpressionResolverComposite
   */
  public boolean isPropertyResolved() {
    return resolved;
  }

  /**
   * Associates a context object with this <code>ELContext</code>.
   *
   * <p>
   * The <code>ELContext</code> maintains a collection of context objects relevant
   * to the evaluation of an expression. These context objects are used by
   * <code>ELResolver</code>s. This method is used to add a context object to that
   * collection.
   * </p>
   *
   * <p>
   * By convention, the <code>contextObject</code> will be of the type specified
   * by the <code>key</code>. However, this is not required and the key is used
   * strictly as a unique identifier.
   * </p>
   *
   * @param key The key used by an @{link ELResolver} to identify this context
   * object.
   * @param contextObject The context object to add to the collection.
   * @throws NullPointerException if key is null or contextObject is null.
   */
  public void putContext(Class<?> key, Object contextObject) {
    if (key == null || contextObject == null) {
      throw new NullPointerException();
    }
    if (map == null) {
      map = new HashMap<>();
    }
    map.put(key, contextObject);
  }

  /**
   * Returns the context object associated with the given key.
   *
   * <p>
   * The <code>ELContext</code> maintains a collection of context objects relevant
   * to the evaluation of an expression. These context objects are used by
   * <code>ELResolver</code>s. This method is used to retrieve the context with
   * the given key from the collection.
   * </p>
   *
   * <p>
   * By convention, the object returned will be of the type specified by the
   * <code>key</code>. However, this is not required and the key is used strictly
   * as a unique identifier.
   * </p>
   *
   * @param key The unique identifier that was used to associate the context
   * object with this <code>ELContext</code>.
   * @return The context object associated with the given key, or null if no such
   * context was found.
   * @throws NullPointerException if key is null.
   */
  public Object getContext(Class<?> key) {
    if (map == null || key == null) {
      return null;
    }
    return map.get(key);
  }

  /**
   * Retrieves the <code>ELResolver</code> associated with this context.
   *
   * <p>
   * The <code>ELContext</code> maintains a reference to the
   * <code>ELResolver</code> that will be consulted to resolve variables and
   * properties during an expression evaluation. This method retrieves the
   * reference to the resolver.
   * </p>
   *
   * <p>
   * Once an <code>ELContext</code> is constructed, the reference to the
   * <code>ELResolver</code> associated with the context cannot be changed.
   * </p>
   *
   * @return The resolver to be consulted for variable and property resolution
   * during expression evaluation.
   */
  public abstract ExpressionResolver getResolver();

  /**
   * Retrieves the <code>ImportHandler</code> associated with this
   * <code>ELContext</code>.
   *
   * @return The import handler to manage imports of classes and packages.
   * @since EL 3.0
   */
  public ImportHandler getImportHandler() {
    if (importHandler == null) {
      importHandler = ImportHandler.getInstance();
    }
    return importHandler;
  }

  /**
   * @param importHandler The import handler to manage imports of classes and packages
   * @since 4.0
   */
  public void setImportHandler(ImportHandler importHandler) {
    this.importHandler = importHandler;
  }

  /**
   * Retrieves the <code>FunctionMapper</code> associated with this
   * <code>ELContext</code>.
   *
   * @return The function mapper to be consulted for the resolution of EL
   * functions.
   */
  public abstract FunctionMapper getFunctionMapper();

  /**
   * Get the <code>Locale</code> stored by a previous invocation to
   * {@link #setLocale}. If this method returns non <code>null</code>, this
   * <code>Locale</code> must be used for all localization needs in the
   * implementation. The <code>Locale</code> must not be cached to allow for
   * applications that change <code>Locale</code> dynamically.
   *
   * @return The <code>Locale</code> in which this instance is operating. Used
   * primarily for message localization.
   */
  public Locale getLocale() {
    return this.locale;
  }

  /**
   * Sets the <code>Locale</code> for this instance. This method may be called by
   * the party creating the instance, such as JavaServer Faces or JSP, to enable
   * the EL implementation to provide localized messages to the user. If no
   * <code>Locale</code> is set, the implementation must use the locale returned
   * by <code>Locale.getDefault( )</code>.
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * Retrieves the <code>VariableMapper</code> associated with this
   * <code>ELContext</code>.
   *
   * @return The variable mapper to be consulted for the resolution of EL
   * variables.
   */
  public abstract VariableMapper getVariableMapper();

  /**
   * Inquires if the name is a LambdaArgument
   *
   * @param arg A possible Lambda formal parameter name
   * @return true if arg is a LambdaArgument, false otherwise.
   */
  public boolean isLambdaArgument(String arg) {
    if (lambdaArgs == null) {
      return false;
    }

    final int size = lambdaArgs.size();
    for (int i = size - 1; i >= 0; i--) {
      if (lambdaArgs.elementAt(i).containsKey(arg)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Retrieves the Lambda argument associated with a formal parameter. If the
   * Lambda expression is nested within other Lambda expressions, the arguments
   * for the current Lambda expression is first searched, and if not found, the
   * arguments for the immediate nesting Lambda expression then searched, and so
   * on.
   *
   * @param arg The formal parameter for the Lambda argument
   * @return The object associated with formal parameter. Null if no object has
   * been associated with the parameter.
   * @since EL 3.0
   */
  public Object getLambdaArgument(String arg) {
    if (lambdaArgs == null) {
      return null;
    }

    final int size = lambdaArgs.size();
    for (int i = size - 1; i >= 0; i--) {
      final Object v = lambdaArgs.elementAt(i).get(arg);
      if (v != null) {
        return v;
      }
    }
    return null;
  }

  /**
   * Installs a Lambda argument map, in preparation for the evaluation of a Lambda
   * expression. The arguments in the map will be in scope during the evaluation
   * of the Lambda expression.
   *
   * @param args The Lambda arguments map
   * @since EL 3.0
   */
  public void enterLambdaScope(Map<String, Object> args) {
    if (lambdaArgs == null) {
      lambdaArgs = new Stack<>();
    }
    lambdaArgs.push(args);
  }

  /**
   * Exits the Lambda expression evaluation. The Lambda argument map that was
   * previously installed is removed.
   *
   * @since EL 3.0
   */
  public void exitLambdaScope() {
    if (lambdaArgs != null) {
      lambdaArgs.pop();
    }
  }

  /**
   * Converts an object to a specific type. If a custom converter in the
   * <code>ELResolver</code> handles this conversion, it is used. Otherwise the
   * standard coercions is applied.
   *
   * <p>
   * An <code>ELException</code> is thrown if an error occurs during the
   * conversion.
   * </p>
   *
   * @param obj The object to convert.
   * @param targetType The target type for the conversion.
   * @throws ExpressionException thrown if errors occur.
   * @since EL 3.0
   */
  public Object convertToType(Object obj, Class<?> targetType) {
    final boolean propertyResolvedSave = isPropertyResolved();
    try {
      setPropertyResolved(false);
      if (targetType.isInstance(obj)) {
        return obj;
      }
      // @since 3.0.4
      final TypeDescriptor targetDescriptor = TypeDescriptor.valueOf(targetType);

      if (conversionService == null) { // @since 4.0
        conversionService = DefaultConversionService.getSharedInstance();
      }

      final MatchingConverter matchingConverter = conversionService.getConverter(obj.getClass(), targetDescriptor);
      if (matchingConverter != null) {
        return matchingConverter.convert(targetDescriptor, obj);
      }
      final ExpressionResolver elResolver = getResolver();
      if (elResolver != null) {
        Object res = elResolver.convertToType(this, obj, targetType);
        if (isPropertyResolved()) {
          return res;
        }
      }
    }
    finally {
      setPropertyResolved(propertyResolvedSave);
    }
    return ExpressionFactory.getSharedInstance().coerceToType(obj, targetType);
  }

  /**
   * @since today-context 3.0
   */
  public Object handlePropertyNotResolved(Object base, Object property, EvaluationContext ctx) throws ExpressionException {
    if (base == null) {
      throw new PropertyNotFoundException(
              "ExpressionResolver cannot handle a null base Object with identifier '" + property + "'");
    }
    throw new PropertyNotFoundException(
            "ExpressionResolver did not handle type: " + base.getClass() + " with property of '" + property + "'");
  }

  /**
   * set conversionService If input conversionService is null
   * use the default {@link DefaultConversionService}
   *
   * @see DefaultConversionService
   * @since 3.0.4
   */
  public void setConversionService(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  /**
   * @since 3.0.4
   */
  public ConversionService getConversionService() {
    return conversionService;
  }

}
