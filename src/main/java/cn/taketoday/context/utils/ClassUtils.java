/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2019 All Rights Reserved.
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.taketoday.context.utils;

import cn.taketoday.context.AnnotationAttributes;
import cn.taketoday.context.Constant;
import cn.taketoday.context.asm.ClassReader;
import cn.taketoday.context.asm.ClassVisitor;
import cn.taketoday.context.asm.Label;
import cn.taketoday.context.asm.MethodVisitor;
import cn.taketoday.context.asm.Opcodes;
import cn.taketoday.context.asm.Type;
import cn.taketoday.context.exception.ContextException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Today <br>
 *         2018-06-0? ?
 */
public abstract class ClassUtils {

	private static final Logger log = LoggerFactory.getLogger(ClassUtils.class);

	/** all the class in class path */
	private static Collection<Class<?>> classesCache;
	/** class loader **/
	private static ClassLoader classLoader;

	static {
		classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader == null) {
			classLoader = ClassUtils.class.getClassLoader();
		}
		if (classLoader == null) {
			classLoader = ClassLoader.getSystemClassLoader();
		}
	}

	/**
	 * @since 2.1.1
	 * @off
	 */
	@SuppressWarnings("serial")
	private static final Set<Class<? extends Annotation>> IGNORE_ANNOTATION_CLASS = //
		new HashSet<Class<? extends Annotation>>(4, 1.0f) {{
			add(Target.class);
			add(Inherited.class);
			add(Retention.class);
			add(Documented.class);
		}
	};
	
	//@on
	public static void addIgnoreAnnotationClass(Class<? extends Annotation> annotationClass) {
		IGNORE_ANNOTATION_CLASS.add(annotationClass);
	}

	/**
	 * 
	 * @param cls
	 * @return
	 */
	public static final boolean isCollection(Class<?> cls) {
		return Collection.class.isAssignableFrom(cls);
	}

	/**
	 * 
	 * @param className
	 * @return
	 */
	public static boolean isPresent(String className) {
		Objects.requireNonNull(className, "class name can't be null");
		try {

			forName(className);
			return true;
		}
		catch (Throwable ex) {
			return false;
		}
	}

	/**
	 * Load class
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class<?> forName(String name) throws ClassNotFoundException {
		return classLoader.loadClass(name);
	}

	/**
	 * Find class by annotation.
	 * 
	 * @param annotationClass
	 *            annotation class
	 * @return the set of class
	 */
	public static Collection<Class<?>> getAnnotatedClasses(Class<? extends Annotation> annotationClass) {
		return filter(clazz -> clazz.isAnnotationPresent(annotationClass));
	}

	/**
	 * @param superClass
	 * @return
	 */
	public static Collection<Class<?>> getImplClasses(Class<?> superClass) {
		return filter(clazz -> superClass.isAssignableFrom(clazz) && superClass != clazz);
	}

	/**
	 * 
	 * @param superClass
	 * @param packageName
	 * @return
	 */
	public static Collection<Class<?>> getImplClasses(Class<?> superClass, String packageName) {
		return filter(clazz -> clazz.getName().startsWith(packageName) && superClass.isAssignableFrom(clazz));
	}

	/**
	 * @param predicate
	 * @return
	 */
	public static final <T> Collection<Class<?>> filter(Predicate<Class<?>> predicate) {
		return getClassCache()//
//				.stream()//
				.parallelStream()//
				.filter(predicate)//
				.collect(Collectors.toSet());
	}

	/**
	 * @param packageName
	 * @return
	 */
	public static Collection<Class<?>> getClasses(String... packages) {

		if (StringUtils.isArrayEmpty(packages) || //
				(packages.length == 1 && StringUtils.isEmpty(packages[0]))) //
		{
			return getClassCache();
		}

		return filter(clazz -> {
			String name = clazz.getName();
			for (String prefix : packages) {
				if (StringUtils.isEmpty(prefix) || name.startsWith(prefix) || name.startsWith(Constant.FREAMWORK_PACKAGE)) {
					return true;
				}
			}
			return false;
		});
	}

	/**
	 * scan class with given package.
	 * 
	 * @param packages
	 *            the packages to scan
	 * @return class set
	 */
	public static Collection<Class<?>> scan(String... packages) {
		Objects.requireNonNull(packages, "scan package can't be null");

		if (classesCache == null || classesCache.isEmpty()) {
			Collection<Class<?>> scanClasses = new HashSet<>(2048, 1.0f);
			for (String package_ : packages) {
				// scan
				scan(scanClasses, package_);
			}
			if (packages == null || packages.length != 1 || StringUtils.isNotEmpty(packages[0])) {
				// Framework classes
				scan(scanClasses, Constant.FREAMWORK_PACKAGE);
			}
			setClassCache(scanClasses);
			return scanClasses;
		}
		return getClasses(packages);
	}

	/**
	 * @param scanClasses
	 * @param packageName
	 */
	public static void scan(Collection<Class<?>> scanClasses, String packageName) {

		packageName = StringUtils.isEmpty(packageName) //
				? Constant.BLANK //
				: packageName.replace('.', '/');
		try {
//			log.debug("package: [{}]", packageName);

			Enumeration<URL> uri = classLoader.getResources(packageName);

			while (uri.hasMoreElements()) {
				URL url = uri.nextElement();
//				log.debug("with uri: [{}]", url);
				final String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					findAllClassWithPackage(packageName, StringUtils.decodeUrl(url.getFile()), scanClasses);
				}
				else if ("jar".equals(protocol)) {
					JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
					if (jarURLConnection == null) {
						log.warn("Can't get the connection of the url: [{}]", url);
						continue;
					}
					JarFile jarFile = jarURLConnection.getJarFile();
					if (jarFile == null) {
						continue;
					}
					Enumeration<JarEntry> jarEntries = jarFile.entries();
					while (jarEntries.hasMoreElements()) {
						loadClassInJar(jarEntries.nextElement(), packageName, scanClasses);
					}
				}
				else {
					log.warn("This Protocol: [{}] is not supported.", protocol);
				}
			}
		}
		catch (IOException e) {
			log.error("IO exception occur With Msg: [{}]", e.getMessage(), e);
			throw new ContextException(e);
		}
	}

	/**
	 * @param jarEntry
	 * @param scanClasses
	 */
	public static void loadClassInJar(JarEntry jarEntry, String packageName, Collection<Class<?>> scanClasses) {
		final String jarEntryName = jarEntry.getName();

		if (jarEntry.isDirectory() || //
				jarEntryName.startsWith("module-info") || //
				jarEntryName.startsWith("package-info") || //
				!jarEntryName.endsWith(Constant.CLASS_FILE_SUFFIX)) {
			return;
		}

		if (StringUtils.isNotEmpty(packageName) && //
				!jarEntryName.startsWith(packageName) && //
				!jarEntryName.startsWith(Constant.FREAMWORK_PACKAGE)) {
			return;
		}

		try {
			scanClasses.add(classLoader.loadClass(//
					jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".")//
			));
		}
		catch (NoClassDefFoundError | ClassNotFoundException | UnsupportedClassVersionError e) {
//			log.warn("[{}] Occur , With Msg:[{}]", e, e.getMessage());
		}
	}

	/**
	 * 
	 * @param packageName
	 *            the name of package
	 * @param packagePath
	 *            the package physical path
	 * @param scanClasses
	 *            class set
	 */
	private static void findAllClassWithPackage(String packageName, String packagePath, Collection<Class<?>> scanClasses) {

		File directory = new File(packagePath);

		if (!directory.exists() || !directory.isDirectory()) {
			log.error("The package -> [{}] you provided that contains nothing", packageName);
			return;
		}

//		log.debug("enter package -> [{}]", packageName);
		File[] directoryFiles = directory.listFiles(CLASS_FILE_FILTER);

		if (directoryFiles == null) {
			log.error("The package -> [{}] you provided that contains nothing", packageName);
			return;
		}
		// exists
		final ClassLoader classLoader = getClassLoader();
		final int length = Constant.CLASS_FILE_SUFFIX.length();

		for (File file : directoryFiles) { //

			String fileName = file.getName();

			if (file.isDirectory()) { // recursive

				String scanPackage = packageName + "." + fileName;
				if (scanPackage.startsWith(".")) {
					scanPackage = scanPackage.replaceFirst("[.]", "");
				}
				findAllClassWithPackage(scanPackage, file.getAbsolutePath(), scanClasses);
				continue;
			}
			if (fileName.contains("package-info")) {
				continue;
			}
			String className = new StringBuilder()//
					.append(packageName)//
					.append(".")//
					.append(fileName.substring(0, fileName.length() - length))//
					.toString()//
					.replaceAll("/", ".");

			try {

				scanClasses.add(classLoader.loadClass(className)); // add
			}
			catch (ClassNotFoundException | NoClassDefFoundError | UnsupportedClassVersionError e) {
//				log.warn("Can't find class: [{}]", className);
			}
		}
	}

	/**
	 * Class file filter
	 */
	private static final FileFilter CLASS_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return (file.isDirectory()) || (file.getName().endsWith(Constant.CLASS_FILE_SUFFIX));
		}
	};

	/**
	 * Compare whether the parameter type is consistent.
	 *
	 * @param types
	 *            the type of the asm({@link Type})
	 * @param classes
	 *            java type({@link Class})
	 * @return return param type equals
	 */
	private static boolean sameType(Type[] types, Class<?>[] classes) {
		if (types.length != classes.length)
			return false;
		for (int i = 0; i < types.length; i++) {
			if (!Type.getType(classes[i]).equals(types[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Find method parameter list, and cache it.
	 * 
	 * @param clazz
	 *            target class
	 * @param method
	 *            target method
	 * @return method parameter list
	 * @throws IOException
	 * @since 1.0.0
	 */
	public static String[] getMethodArgsNames(Method method) throws IOException {

		String[] paramNames = new String[method.getParameterCount()];

		try (InputStream resourceAsStream = //
				classLoader.getResourceAsStream(method.getDeclaringClass().getName().replace('.', '/') + ".class")) {

			new ClassReader(resourceAsStream).accept(new ClassVisitor(Opcodes.ASM7) {
				@Override
				public MethodVisitor visitMethod(int access, String name, //
						String desc, String signature, String[] exceptions) //
				{
					final Type[] args = Type.getArgumentTypes(desc);
					// method name and parameter same at the same time
					if (!name.equals(method.getName()) || !sameType(args, method.getParameterTypes())) {
						return super.visitMethod(access, name, desc, signature, exceptions);
					}
					return new MethodVisitor(Opcodes.ASM7, super.visitMethod(access, name, desc, signature, exceptions)) {
						@Override
						public void visitLocalVariable(String name, String desc, //
								String signature, Label start, Label end, int index) //
						{
							// if it is a static method, the first is the parameter
							// if it's not a static method, the first one is "this" and then the parameter
							// of the method
							if (!Modifier.isStatic(method.getModifiers())) {
								index = index - 1;
							}

							if (index >= 0 && index < paramNames.length) {
								paramNames[index] = name;
							}
							super.visitLocalVariable(name, desc, signature, start, end, index);
						}
					};
				}
			}, 0);
		}
		return paramNames;
	}

	/**
	 * 
	 * @param annotatedElement
	 * @param annotationClass
	 * @param implClass
	 * @return
	 * @since 2.1.1
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T[] getAnnotationArray(AnnotatedElement annotatedElement, //
			Class<T> annotationClass, Class<? extends T> implClass) //
	{
		return getAnnotation(annotatedElement, annotationClass, implClass).toArray((T[]) Array.newInstance(annotationClass, 0));
	}

	/**
	 * @param annotatedElement
	 * @param annotationClass
	 * @return
	 * @since 2.1.1
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T[] getAnnotationArray(AnnotatedElement annotatedElement, Class<T> annotationClass) {
		return getAnnotation(annotatedElement, annotationClass).toArray((T[]) Array.newInstance(annotationClass, 0));
	}

	/**
	 * Get Annotation by reflect
	 * 
	 * @param annotatedElement
	 *            The annotated element
	 * @param annotationClass
	 *            The annotation class
	 * @param implClass
	 *            The implementation class
	 * @return
	 * @since 2.0.x
	 */
	public static <T extends Annotation> Collection<T> getAnnotation(AnnotatedElement annotatedElement, //
			Class<T> annotationClass, Class<? extends T> implClass) //
	{
		try {

			Collection<T> result = new LinkedList<>();
			// @off
			for (AnnotationAttributes attributes : getAnnotationAttributes(annotatedElement, annotationClass)) {
				T instance = implClass.getConstructor().newInstance();
				for (Method method : annotationClass.getDeclaredMethods()) {
					// method name must == field name
					final String name = method.getName();
					final Field declaredField = implClass.getDeclaredField(name);
//					final boolean accessible = declaredField.isAccessible(); // access able ?
//					try {
//						if (!accessible) {
							declaredField.setAccessible(true);
//						}
						declaredField.set(instance, attributes.get(name));
//					} finally {
//						declaredField.setAccessible(accessible);
//					}
				}
				result.add(instance);
			}
			//@on
			return result;
		}
		catch (NoSuchFieldException e) {
			log.error("You must specify a field: [{}] in class: [{}]", e.getMessage(), implClass.getName(), e);
			throw new ContextException(e);
		}
		catch (Throwable ex) {
			ex = ExceptionUtils.unwrapThrowable(ex);
			log.error("An Exception Occurred When Getting Annotation, With Msg: [{}]", ex.getMessage(), ex);
			throw new ContextException(ex);
		}
	}

	/**
	 * Get Annotation Attributes from an annotation instance
	 * 
	 * @param annotation
	 *            annotation instance
	 * @return
	 * @since 2.1.1
	 */
	public static AnnotationAttributes getAnnotationAttributes(Annotation annotation) {

		AnnotationAttributes attributes = null;
		try {

			Method[] declaredMethods = annotation.annotationType().getDeclaredMethods();
			attributes = new AnnotationAttributes(annotation.annotationType(), declaredMethods.length);

			for (Method method : declaredMethods) {
				attributes.put(method.getName(), method.invoke(annotation));
			}
		}
		catch (Throwable ex) {
			ex = ExceptionUtils.unwrapThrowable(ex);
			log.error("An Exception Occurred When Getting Annotation Attributes, With Msg: [{}]", ex.getMessage(), ex);
			throw new ContextException(ex);
		}
		return attributes;
	}

	/**
	 * Get Annotation by proxy
	 * 
	 * @param annotatedElement
	 *            The annotated element
	 * @param annotationClass
	 *            The annotation class
	 * @return
	 * @since 2.1.1
	 */
	public static <T extends Annotation> Collection<T> getAnnotation(AnnotatedElement annotatedElement, //
			Class<T> annotationClass) //
	{
		Objects.requireNonNull(annotationClass, "annotation class can't be null");

		Collection<T> annotations = new LinkedList<>();
		for (AnnotationAttributes attributes : getAnnotationAttributes(annotatedElement, annotationClass)) {
			annotations.add(getAnnotationProxy(annotationClass, attributes));
		}
		return annotations;
	}

	/**
	 * Get Annotation by proxy
	 * 
	 * @param annotationClass
	 *            The annotation class
	 * @param attributes
	 *            The annotation attributes key-value
	 * @return
	 * @since 2.1.1
	 */
	public static <T extends Annotation> T getAnnotationProxy(Class<T> annotationClass, AnnotationAttributes attributes) {
		return annotationClass.cast(Proxy.newProxyInstance(classLoader, new Class[] { annotationClass, Annotation.class }, //
				(Object proxy, Method method, Object[] args) -> {
					switch (method.getName())
					{
						case Constant.EQUALS :
							return eq(attributes, args);
						case Constant.HASH_CODE :
							return attributes.hashCode();
						case Constant.TO_STRING :
							return attributes.toString();
						case Constant.ANNOTATION_TYPE :
							return annotationClass;
					}
					return attributes.get(method.getName());
				}//
		));
	}

	/**
	 * Equals
	 * 
	 * @param attributes
	 *            key-value attributes
	 * @param args
	 *            InvocationHandler args
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @since 2.1.1
	 */
	private static Object eq(AnnotationAttributes attributes, Object[] args) throws IllegalAccessException, InvocationTargetException {
		Object object = args[0];
		if (attributes == object) {
			return true;
		}
		if (object instanceof Annotation) {
			for (Method method_ : object.getClass().getDeclaredMethods()) {
				Object value_ = attributes.get(method_.getName());
				Object value = method_.invoke(object);
				if (value == null || value_ == null || !value.equals(value_)) {
					return false;
				}
			}
			return true;
		}
		if (object instanceof AnnotationAttributes) {
			return object.equals(attributes);
		}
		return false;
	}

	/**
	 * Get attributes the 'key-value' of annotations
	 * 
	 * @param annotatedElement
	 *            The annotated element
	 * @param annotationClass
	 *            The annotation class
	 * @return
	 * @since 2.1.1
	 */
	public static <T extends Annotation> Collection<AnnotationAttributes> //
			getAnnotationAttributes(AnnotatedElement annotatedElement, Class<T> annotationClass) //
	{
		Objects.requireNonNull(annotatedElement, "annotated element can't be null");

		Collection<AnnotationAttributes> result = new HashSet<>();
		for (Annotation annotation : annotatedElement.getDeclaredAnnotations()) {
			AnnotationAttributes annotationAttributes = getAnnotationAttributes(annotation, annotationClass);
			if (annotationAttributes != null) {
				result.add(annotationAttributes);
			}
		}
		return result;
	}

	/**
	 * 
	 * @param annotationClass
	 *            The annotation class
	 * @param annotation
	 *            The annotation instance
	 * @return
	 * @since 2.1.1
	 */
	public static <T extends Annotation> AnnotationAttributes getAnnotationAttributes(Annotation annotation, //
			Class<T> annotationClass) //
	{
		try {
			if (annotation == null) {
				return null;
			}
			Class<? extends Annotation> annotationType = annotation.annotationType();

			if (annotationType == annotationClass) {// 如果等于对象注解就直接添加
				return getAnnotationAttributes(annotation);
			}

			if (IGNORE_ANNOTATION_CLASS.contains(annotationType)) {
				return null;
			}
			// find the default value of annotation
			AnnotationAttributes annotationAttributes = // recursive
					getTargetAnnotationAttributes(annotationClass, annotationType);
			if (annotationAttributes == null) { // there is no an annotation
				return null;
			}

			// found it
			for (Method method : annotationType.getDeclaredMethods()) {
				final String name = method.getName();
				Object value = annotationAttributes.get(name);
				if (value == null || eq(method.getReturnType(), value.getClass())) {
					annotationAttributes.put(name, method.invoke(annotation));
				}
			}
			return annotationAttributes;
		}
		catch (Throwable ex) {
			ex = ExceptionUtils.unwrapThrowable(ex);
			log.error("An Exception Occurred When Getting Annotation Attributes, With Msg: [{}]", ex.getMessage(), ex);
			throw new ContextException(ex);
		}
	}

	private static boolean eq(Class<?> returnType, Class<?> clazz) {
		if (returnType == clazz) {
			return true;
		}
		if (returnType.isPrimitive()) {
			switch (returnType.getName())
			{
				case "int" :
					return clazz == Integer.class;
				case "long" :
					return clazz == Long.class;
				case "byte" :
					return clazz == Byte.class;
				case "char" :
					return clazz == Character.class;
				case "float" :
					return clazz == Float.class;
				case "double" :
					return clazz == Double.class;
				case "short" :
					return clazz == Short.class;
				case "boolean" :
					return clazz == Boolean.class;
			}
		}
		return false;
	}

	/**
	 * Use recursive to find the target annotation instance
	 * 
	 * @param targetAnnotationType
	 * @param annotationType
	 * @return
	 * @since 2.1.1
	 */
	public static <T extends Annotation> AnnotationAttributes getTargetAnnotationAttributes(Class<T> targetAnnotationType,
			Class<? extends Annotation> annotationType) //
	{

		for (Annotation currentAnnotation : annotationType.getAnnotations()) {

			if (IGNORE_ANNOTATION_CLASS.contains(currentAnnotation.annotationType())) {
				continue;
			}
			if (currentAnnotation.annotationType() == targetAnnotationType) {
				return getAnnotationAttributes(currentAnnotation); // found it
			}
			AnnotationAttributes attributes = // recursive
					getTargetAnnotationAttributes(targetAnnotationType, currentAnnotation.annotationType());
			if (attributes != null) {
				return attributes;
			}
		}
		return null;
	}

	/**
	 * Get instance with bean class
	 * 
	 * @param beanClass
	 *            bean class
	 * @return
	 * @since 2.1.2
	 * @throws Throwable
	 */
	public static <T> T newInstance(Class<T> beanClass) throws Throwable {
		return beanClass.getConstructor().newInstance();
	}

	/**
	 * Get instance with bean class
	 * 
	 * @param beanClassName
	 *            bean class name string
	 * @return
	 * @throws Throwable
	 * @since 2.1.2
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String beanClassName) throws Throwable {
		return (T) forName(beanClassName).getConstructor().newInstance();
	}

	/**
	 * Get all {@link Field} list
	 * 
	 * @param targetClass
	 *            target class
	 * @return
	 * @since 2.1.2
	 */
	public static Collection<Field> getFields(Class<?> targetClass) {

		final List<Field> list = new ArrayList<>(64);
		do {

			for (Field field : targetClass.getDeclaredFields()) {
				list.add(field);
			}
		} while ((targetClass = targetClass.getSuperclass()) != Object.class && targetClass != null);

		return list;
	}

	/**
	 * Get all {@link Field} array
	 * 
	 * @param targetClass
	 *            target class
	 * @return
	 * @since 2.1.2
	 */
	public static Field[] getFieldArray(Class<?> targetClass) {
		return getFields(targetClass).toArray(new Field[0]);
	}

	/**
	 * clear cache
	 */
	public static void clearCache() {
		setClassCache(null);
	}

	public static void setClassLoader(ClassLoader classLoader) {
		ClassUtils.classLoader = classLoader;
	}

	public static ClassLoader getClassLoader() {
		return ClassUtils.classLoader;
	}

	/**
	 * get all classes loaded in class path
	 */
	public static Collection<Class<?>> getClassCache() {

		if (classesCache == null || classesCache.isEmpty()) {
			setClassCache(scan(""));
		}
		return classesCache;
	}

	public static void setClassCache(Collection<Class<?>> classes) {
		ClassUtils.classesCache = classes;
	}

}
