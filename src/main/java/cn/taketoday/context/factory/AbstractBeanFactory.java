/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2020 All Rights Reserved.
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
package cn.taketoday.context.factory;

import static cn.taketoday.context.utils.ClassUtils.makeAccessible;
import static cn.taketoday.context.utils.ContextUtils.resolveParameter;
import static cn.taketoday.context.utils.ExceptionUtils.unwrapThrowable;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import cn.taketoday.context.BeanNameCreator;
import cn.taketoday.context.Scope;
import cn.taketoday.context.annotation.Component;
import cn.taketoday.context.annotation.Primary;
import cn.taketoday.context.asm.Type;
import cn.taketoday.context.aware.Aware;
import cn.taketoday.context.aware.BeanClassLoaderAware;
import cn.taketoday.context.aware.BeanFactoryAware;
import cn.taketoday.context.aware.BeanNameAware;
import cn.taketoday.context.env.DefaultBeanNameCreator;
import cn.taketoday.context.exception.BeanDefinitionStoreException;
import cn.taketoday.context.exception.BeanInitializingException;
import cn.taketoday.context.exception.BeanInstantiationException;
import cn.taketoday.context.exception.ConfigurationException;
import cn.taketoday.context.exception.ContextException;
import cn.taketoday.context.exception.NoSuchBeanDefinitionException;
import cn.taketoday.context.exception.PropertyValueException;
import cn.taketoday.context.loader.BeanDefinitionLoader;
import cn.taketoday.context.logger.Logger;
import cn.taketoday.context.logger.LoggerFactory;
import cn.taketoday.context.utils.Assert;
import cn.taketoday.context.utils.ClassUtils;
import cn.taketoday.context.utils.ContextUtils;
import cn.taketoday.context.utils.ExceptionUtils;
import cn.taketoday.context.utils.ObjectUtils;
import cn.taketoday.context.utils.OrderUtils;
import cn.taketoday.context.utils.StringUtils;

/**
 * @author TODAY <br>
 *         2018-06-23 11:20:58
 */
public abstract class AbstractBeanFactory implements ConfigurableBeanFactory {

    private static final Logger log = LoggerFactory.getLogger(AbstractBeanFactory.class);

    /** bean name creator */
    private BeanNameCreator beanNameCreator;
    /** object factories */
    private Map<Class<?>, Object> objectFactories;
    /** dependencies */
    private final HashSet<PropertyValue> dependencies = new HashSet<>(128);
    /** Bean Post Processors */
    private final ArrayList<BeanPostProcessor> postProcessors = new ArrayList<>();
    /** Map of bean instance, keyed by bean name */
    private final HashMap<String, Object> singletons = new HashMap<>(128);
    private final HashMap<String, Scope> scopes = new HashMap<>();
    /** Map of bean definition objects, keyed by bean name */
    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);

    // @since 2.1.6
    private boolean fullPrototype = false;
    // @since 2.1.6
    private boolean fullLifecycle = false;

    @Override
    public Object getBean(final String name) throws ContextException {
        final BeanDefinition def = getBeanDefinition(name);
        // if not exits a bean definition return a bean may exits in singletons cache
        return def != null ? getBean(def) : getSingleton(name);
    }

    @Override
    public Object getBean(final BeanDefinition def) {
        if (def.isFactoryBean()) {
            return getFactoryBean(def).getBean();
        }
        if (def.isInitialized()) { // fix #7
            return getSingleton(def.getName());
        }
        try {
            final BeanDefinition child = def.getChild();
            if (child == null) {
                return initializeBean(def);
            }
            return getImplementation(child, def);
        }
        catch (Throwable ex) {
            ex = ExceptionUtils.unwrapThrowable(ex);
            throw new ContextException("An Exception Occurred When Getting A Bean: ["
                    + def + "], With Msg: [" + ex + "]", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> requiredType) {

        final Object bean = getBean(getBeanNameCreator().create(requiredType));
        if (bean != null && requiredType.isInstance(bean)) {
            return (T) bean;
        }
        return (T) doGetBeanForType(requiredType);
    }

    /**
     * Get bean for required type
     * 
     * @param requiredType
     *            Bean type
     * @since 2.1.2
     */
    protected <T> Object doGetBeanForType(final Class<T> requiredType) {
        Object bean = null;
        for (final Entry<String, BeanDefinition> entry : getBeanDefinitions().entrySet()) {
            if (requiredType.isAssignableFrom(entry.getValue().getBeanClass())) {
                bean = getBean(entry.getValue());
                if (bean != null) {
                    return bean;
                }
            }
        }
        // fix
        for (final Object entry : getSingletons().values()) {
            if (requiredType.isAssignableFrom(entry.getClass())) {
                return entry;
            }
        }
        return bean;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> requiredType) {
        final Object bean = getBean(name);
        return requiredType.isInstance(bean) ? (T) bean : null;
    }

    @Override
    public <T> List<T> getBeans(final Class<T> requiredType) {
        final HashSet<T> beans = new HashSet<>();

        for (final Entry<String, BeanDefinition> entry : getBeanDefinitions().entrySet()) {
            if (requiredType.isAssignableFrom(entry.getValue().getBeanClass())) {
                @SuppressWarnings("unchecked") //
                final T bean = (T) getBean(entry.getValue());
                if (bean != null) {
                    beans.add(bean);
                }
            }
        }
        return new ArrayList<>(beans);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation, T> List<T> getAnnotatedBeans(final Class<A> annotationType) {
        final HashSet<T> beans = new HashSet<>();

        for (final Entry<String, BeanDefinition> entry : getBeanDefinitions().entrySet()) {
            if (entry.getValue().isAnnotationPresent(annotationType)) {
                final T bean = (T) getBean(entry.getValue());
                if (bean != null) {
                    beans.add(bean);
                }
            }
        }
        return new ArrayList<>(beans);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(final Class<T> requiredType) {
        final HashMap<String, T> beans = new HashMap<>();

        for (final Entry<String, BeanDefinition> entry : getBeanDefinitions().entrySet()) {
            if (requiredType.isAssignableFrom(entry.getValue().getBeanClass())) {
                @SuppressWarnings("unchecked") //
                final T bean = (T) getBean(entry.getValue());
                if (bean != null) {
                    beans.put(entry.getKey(), bean);
                }
            }
        }
        return beans;
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions() {
        return beanDefinitionMap;
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitionsMap() {
        return beanDefinitionMap;
    }

    /**
     * Create bean instance
     * <p>
     * <b> Note </b> If target bean is {@link Scope#SINGLETON} will be register is
     * to the singletons pool
     * </p>
     * 
     * @param def
     *            Bean definition
     * @return Target bean instance
     * @throws BeanInstantiationException
     *             When instantiation of a bean failed
     */
    protected Object createBeanIfNecessary(final BeanDefinition def) throws BeanInstantiationException {
        if (def.isSingleton()) {
            final String name = def.getName();
            Object bean = getSingleton(name);
            if (bean == null) {
                registerSingleton(name, bean = createBeanInstance(def));
            }
            return bean;
        }
        else {
            return createBeanInstance(def);
        }
    }

    /**
     * Create new bean instance
     * 
     * @param def
     *            Target {@link BeanDefinition} descriptor
     * @return A new bean object
     * @throws BeanInstantiationException
     *             When instantiation of a bean failed
     */
    protected Object createBeanInstance(final BeanDefinition def) throws BeanInstantiationException {
        return ClassUtils.newInstance(def, this);
    }

    /**
     * Apply property values.
     *
     * @param bean
     *            Bean instance
     * @param propertyValues
     *            Property list
     * @throws PropertyValueException
     *             If any {@link Exception} occurred when apply
     *             {@link PropertyValue}
     * @throws NoSuchBeanDefinitionException
     *             If {@link BeanReference} is required and there isn't a bean in
     *             this {@link BeanFactory}
     */
    protected void applyPropertyValues(final Object bean, final PropertyValue[] propertyValues)
            throws PropertyValueException, NoSuchBeanDefinitionException//
    {
        for (final PropertyValue propertyValue : propertyValues) {
            Object value = propertyValue.getValue();
            // reference bean
            if (value instanceof BeanReference) {
                final BeanReference reference = (BeanReference) value;
                // fix: same name of bean
                value = resolvePropertyValue(reference);
                if (value == null) {
                    if (reference.isRequired()) {
                        log.error("[{}] is required.", propertyValue);
                        throw new NoSuchBeanDefinitionException(reference.getName(), reference.getReferenceClass());
                    }
                    continue; // if reference bean is null and it is not required ,do nothing,default value
                }
            }
            // set property
            propertyValue.set(bean, value);
        }
    }

    /**
     * Resolve reference {@link PropertyValue}
     * 
     * @param ref
     *            {@link BeanReference} record a reference of bean
     * @return A {@link PropertyValue} bean or a proxy
     */
    protected Object resolvePropertyValue(final BeanReference ref) {

        final Class<?> type = ref.getReferenceClass();
        final String name = ref.getName();

        if (fullPrototype && ref.isPrototype() && containsBeanDefinition(name)) {
            return Prototypes.newProxyInstance(type, getBeanDefinition(name), this);
        }
        final Object bean = getBean(name, type);
        return bean != null ? bean : doGetBeanForType(type);
    }

    /**
     * Invoke initialize methods
     * 
     * @param bean
     *            Bean instance
     * @param methods
     *            Initialize methods
     * @throws Exception
     *             If any {@link Exception} occurred when invoke init methods
     */
    protected void invokeInitMethods(final Object bean, final Method... methods) {

        for (final Method method : methods) {
            try {
                //method.setAccessible(true); // fix: can not access a member
                method.invoke(bean, resolveParameter(makeAccessible(method), this));
            }
            catch (Exception e) {
                throw new BeanInitializingException("An Exception Occurred When [" +
                        bean + "] invoke init method: [" + method + "], With Msg: [" + e + "]", e);
            }
        }

        if (bean instanceof InitializingBean) {
            try {
                ((InitializingBean) bean).afterPropertiesSet();
            }
            catch (Exception e) {
                throw new BeanInitializingException("An Exception Occurred When [" +
                        bean + "] apply after properties, With Msg: [" + e + "]", e);
            }
        }
    }

    /**
     * Create prototype bean instance.
     *
     * @param def
     *            Bean definition
     * @param name
     *            Bean name
     * @return A initialized Prototype bean instance
     * @throws BeanInstantiationException
     *             If any {@link Exception} occurred when create prototype
     */
    protected Object createPrototype(final BeanDefinition def) throws BeanInstantiationException {
        return initializeBean(createBeanInstance(def), def); // initialize
    }

    /**
     * Get initialized {@link FactoryBean}
     * 
     * @param def
     *            Target {@link BeanDefinition}
     * @return Initialized {@link FactoryBean} never be null
     */
    @SuppressWarnings("unchecked")
    protected <T> FactoryBean<T> getFactoryBean(final BeanDefinition def) throws BeanInitializingException {

        final FactoryBean<T> factoryBean = getFactoryBeanInstance(def);

        if (def.isInitialized()) {
            return factoryBean;
        }

        if (factoryBean instanceof AbstractFactoryBean) {
            ((AbstractFactoryBean<?>) factoryBean).setSingleton(def.isSingleton());
        }

        // Initialize Factory
        // Factory is always a SINGLETON bean
        // ----------------------------------------

        if (log.isDebugEnabled()) {
            log.debug("Initialize FactoryBean: [{}]", def.getName());
        }
        final Object initBean = initializeBean(factoryBean, def);

        def.setInitialized(true);
        registerSingleton(getFactoryBeanName(def), initBean); // Refresh bean to the mapping
        return (FactoryBean<T>) initBean;
    }

    /**
     * Get {@link FactoryBean} object
     * 
     * @param <T>
     *            Target bean {@link Type}
     * @param def
     *            Target bean definition
     * @return {@link FactoryBean} object
     */
    @SuppressWarnings("unchecked")
    protected <T> FactoryBean<T> getFactoryBeanInstance(final BeanDefinition def) {
        if (def instanceof FactoryBeanDefinition) {
            return ((FactoryBeanDefinition<T>) def).getFactory();
        }
        Object factory = getSingleton(getFactoryBeanName(def));
        if (factory instanceof FactoryBean) {
            // has already exits factory
            return (FactoryBean<T>) factory;
        }
        factory = createBeanInstance(def);
        if (factory instanceof FactoryBean) {
            return (FactoryBean<T>) factory;
        }
        throw new ConfigurationException("object must be FactoryBean");
    }

    /**
     * Get {@link FactoryBean} bean name
     * 
     * @param def
     *            Target {@link FactoryBean} {@link BeanDefinition}
     * @return The name of target factory in this {@link BeanFactory}
     */
    protected String getFactoryBeanName(final BeanDefinition def) {
        return FACTORY_BEAN_PREFIX.concat(def.getName());
    }

    /**
     * Get current {@link BeanDefinition} implementation invoke this method requires
     * that input {@link BeanDefinition} is not initialized, Otherwise the bean will
     * be initialized multiple times
     * 
     * @param childName
     *            Child bean name
     * @param currentDef
     *            Bean definition
     * @return Current {@link BeanDefinition} implementation
     * @throws BeanInstantiationException
     *             When instantiation of a bean failed
     */
    protected Object getImplementation(final String childName, final BeanDefinition currentDef) throws BeanInstantiationException {
        return getImplementation(getBeanDefinition(childName), currentDef);
    }

    /**
     * Get current {@link BeanDefinition} implementation invoke this method requires
     * that input {@link BeanDefinition} is not initialized, Otherwise the bean will
     * be initialized multiple times
     * 
     * @param childDef
     *            Child bean definition
     * @param currentDef
     *            Target bean definition
     * @return Current {@link BeanDefinition} implementation
     * @throws BeanInstantiationException
     *             When instantiation of a bean failed
     */
    protected Object getImplementation(final BeanDefinition childDef, final BeanDefinition currentDef)
            throws BeanInstantiationException // 
    {
        if (currentDef.isPrototype()) {
            return createPrototype(childDef);
        }
        // initialize child bean
        final Object bean = initializeSingleton(getSingleton(currentDef.getName()), childDef);
        if (!currentDef.isInitialized()) {
            // register as parent bean and set initialize flag
            registerSingleton(currentDef.getName(), bean);
            currentDef.setInitialized(true);
        }
        return bean;
    }

    /**
     * Initialize a bean with given name and it's definition.
     * 
     * @param def
     *            Target {@link BeanDefinition}
     * @return A initialized bean, should not be null
     */
    protected Object initializeBean(final BeanDefinition def) {
        if (def.isSingleton()) {
            return initializeSingleton(def);
        }
        else if (def.isPrototype()) {
            return createPrototype(def);
        }
        else {
            final Scope scope = scopes.get(def.getScope());
            if (scope == null) {
                throw new ConfigurationException("No such scope: [" + def.getScope() + "] in this " + this);
            }
            return getScopeBean(def, scope);
        }
    }

    @Override
    public Object getScopeBean(final BeanDefinition def, Scope scope) {
        return scope.get(def, this::createPrototype);
    }

    /**
     * Initializing bean, with given bean instance and bean definition
     * 
     * @param bean
     *            Bean instance
     * @param def
     *            Bean definition
     * @return A initialized object, never be null
     * @throws BeanInitializingException
     *             If any {@link Exception} occurred when initialize bean
     */
    protected Object initializeBean(final Object bean, final BeanDefinition def) throws BeanInitializingException {

        if (log.isDebugEnabled()) {
            log.debug("Initializing bean named: [{}].", def.getName());
        }
        aware(bean, def);

        final List<BeanPostProcessor> postProcessors = getPostProcessors();
        if (postProcessors.isEmpty()) {
            // apply properties
            applyPropertyValues(bean, def.getPropertyValues());
            // invoke initialize methods
            invokeInitMethods(bean, def.getInitMethods());
            return bean;
        }
        return initWithPostProcessors(bean, def, postProcessors);
    }

    /**
     * Initialize with {@link BeanPostProcessor}s
     * 
     * @param bean
     *            Bean instance
     * @param def
     *            Current {@link BeanDefinition}
     * @param processors
     *            {@link BeanPostProcessor}s
     * @return Initialized bean
     * @throws BeanInitializingException
     *             If any {@link Exception} occurred when initialize with processors
     */
    protected Object initWithPostProcessors(final Object bean,
                                            final BeanDefinition def,
                                            final List<BeanPostProcessor> processors) throws BeanInitializingException //
    {
        Object ret = bean;
        // before properties
        for (final BeanPostProcessor processor : processors) {
            try {
                ret = processor.postProcessBeforeInitialization(ret, def);
            }
            catch (Exception e) {
                throw new BeanInitializingException("An Exception Occurred When [" +
                        bean + "] before properties set, With Msg: [" + e + "]", e);
            }
        }
        // apply properties
        applyPropertyValues(ret, def.getPropertyValues());
        // invoke initialize methods
        invokeInitMethods(ret, def.getInitMethods());
        // after properties
        for (final BeanPostProcessor processor : processors) {
            try {
                ret = processor.postProcessAfterInitialization(ret, def);
            }
            catch (Exception e) {
                throw new BeanInitializingException("An Exception Occurred When [" +
                        bean + "] after properties set, With Msg: [" + e + "]", e);
            }
        }
        return ret;
    }

    /**
     * Inject FrameWork {@link Component}s to target bean
     *
     * @param bean
     *            Bean instance
     * @param def
     *            Bean definition
     */
    public final void aware(final Object bean, final BeanDefinition def) {
        if (bean instanceof Aware) {
            awareInternal(bean, def);
        }
    }

    /**
     * Do Inject FrameWork {@link Component}s to target bean
     * 
     * @param bean
     *            Target bean
     * @param def
     *            Target {@link BeanDefinition}
     */
    protected void awareInternal(final Object bean, final BeanDefinition def) {

        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(def.getName());
        }
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }
        if (bean instanceof BeanClassLoaderAware) {
            ((BeanClassLoaderAware) bean).setBeanClassLoader(bean.getClass().getClassLoader());
        }
    }

    /**
     * Initialize a singleton bean with given name and it's definition.
     * <p>
     * Bean definition must be a singleton
     * </p>
     * this method will apply {@link BeanDefinition}'s 'initialized' property and
     * register is bean instance to the singleton pool
     * <p>
     * If the input bean is {@code null} then use
     * {@link #initializeSingleton(BeanDefinition)} To initialize singleton
     * 
     * @param bean
     *            Input old bean
     * @param def
     *            Bean definition
     * @return A initialized singleton bean
     * @throws BeanInstantiationException
     *             When instantiation of a bean failed
     * @see #initializeSingleton(BeanDefinition)
     */
    protected Object initializeSingleton(final Object bean, final BeanDefinition def) {
        if (bean == null) {
            return initializeSingleton(def);
        }
        Assert.isTrue(def.isSingleton(), "Bean definition must be a singleton");
        if (def.isInitialized()) { // fix #7
            return bean;
        }
        final Object initBean = initializeBean(bean, def);
        if (initBean != bean) {
            registerSingleton(def.getName(), initBean);
        }
        // apply this bean definition's 'initialized' property
        def.setInitialized(true);
        return initBean;
    }

    /**
     * Initialize a singleton bean with given name and it's definition.
     * <p>
     * Bean definition must be a singleton
     * </p>
     * this method will apply {@link BeanDefinition}'s 'initialized' property and
     * register is bean instance to the singleton pool
     * 
     * @param def
     *            Bean definition
     * @return A initialized singleton bean
     * @throws BeanInstantiationException
     *             When instantiation of a bean failed
     */
    protected Object initializeSingleton(final BeanDefinition def) throws BeanInstantiationException {
        Assert.isTrue(def.isSingleton(), "Bean definition must be a singleton");

        if (def.isFactoryBean()) {
            final Object bean = getFactoryBean(def).getBean();
            if (!containsSingleton(def.getName())) {
                registerSingleton(def.getName(), bean);
                def.setInitialized(true);
            }
            return bean;
        }

        if (def.isInitialized()) { // fix #7
            return getSingleton(def.getName());
        }

        final Object bean = createBeanIfNecessary(def);
        final Object initBean = initializeBean(bean, def);

        if (initBean != bean) {
            registerSingleton(def.getName(), initBean);
        }
        def.setInitialized(true);
        return initBean;
    }

    /**
     * Register {@link BeanPostProcessor}s
     */
    public void registerBeanPostProcessors() {
        log.debug("Start loading BeanPostProcessor.");
        postProcessors.addAll(getBeans(BeanPostProcessor.class));
        OrderUtils.reversedSort(postProcessors);
    }

    // handleDependency
    // ---------------------------------------

    /**
     * Handle abstract dependencies
     */
    public void handleDependency() {

        for (final PropertyValue propertyValue : getDependencies()) {

            final Class<?> propertyType = propertyValue.getField().getType();

            // Abstract
            if (!Modifier.isAbstract(propertyType.getModifiers())) {
                continue;
            }

            final BeanReference ref = (BeanReference) propertyValue.getValue();
            final String beanName = ref.getName();

            // fix: #2 when handle dependency some bean definition has already exist
            if (containsBeanDefinition(beanName)) {
                continue;
            }

            // handle dependency which is interface and parent object
            // --------------------------------------------------------

            // find child beans
            final List<BeanDefinition> childDefs = doGetChildDefinition(beanName, propertyType);

            if (childDefs.isEmpty()) {
                if (ref.isRequired()) {
                    throw new ConfigurationException("Context does not exist for this type:[" + propertyType + "] of bean");
                }
            }
            else {
                final BeanDefinition childDef = getPrimaryBeanDefinition(childDefs);
                if (log.isDebugEnabled()) {
                    log.debug("Found The Implementation Of [{}] Bean: [{}].", beanName, childDef.getName());
                }
                registerBeanDefinition(beanName, new DefaultBeanDefinition(beanName, childDef));
            }
        }
    }

    /**
     * Get {@link Primary} {@link BeanDefinition}
     * 
     * @param childDefs
     *            All suitable {@link BeanDefinition}s
     * @return A {@link Primary} {@link BeanDefinition}
     */
    protected BeanDefinition getPrimaryBeanDefinition(final List<BeanDefinition> childDefs) {
        BeanDefinition childDef = null;
        if (childDefs.size() > 1) {
            OrderUtils.reversedSort(childDefs); // size > 1 sort
            for (final BeanDefinition def : childDefs) {
                if (def.isAnnotationPresent(Primary.class)) {
                    childDef = def;
                    break;
                }
            }
        }
        if (childDef == null) {
            childDef = childDefs.get(0); // first one
        }
        return childDef;
    }

    /**
     * Get child {@link BeanDefinition}s
     * 
     * @param beanName
     *            Bean name
     * @param beanClass
     *            Bean class
     * @return A list of {@link BeanDefinition}s, Never be null
     */
    protected List<BeanDefinition> doGetChildDefinition(final String beanName, final Class<?> beanClass) {

        final Set<BeanDefinition> ret = new HashSet<>();

        for (final Entry<String, BeanDefinition> entry : getBeanDefinitions().entrySet()) {
            final BeanDefinition childDef = entry.getValue();
            final Class<?> clazz = childDef.getBeanClass();

            if (beanClass != clazz
                && beanClass.isAssignableFrom(clazz)
                && !beanName.equals(childDef.getName())) {

                ret.add(childDef); // is beanClass's Child Bean
            }
        }

        if (ret.isEmpty()) { // If user registered BeanDefinition
            final BeanDefinition handleBeanDef = handleDependency(beanName, beanClass);
            if (handleBeanDef != null) {
                ret.add(handleBeanDef);
            }
        }
        return ret.isEmpty() ? Collections.emptyList() : new ArrayList<>(ret);
    }

    /**
     * Handle dependency {@link BeanDefinition}
     * 
     * @param beanName
     *            bean name
     * @param beanClass
     *            bean class
     * @return Dependency {@link BeanDefinition}
     */
    protected BeanDefinition handleDependency(final String beanName, final Class<?> beanClass) {

        final Object obj = createDependencyInstance(beanClass);
        if (obj != null) {
            registerSingleton(beanName, obj);
            return new DefaultBeanDefinition(beanName, beanClass);
        }
        return null;
    }

    /**
     * Create dependency object
     * 
     * @param type
     *            dependency type
     * @return Dependency object
     */
    protected Object createDependencyInstance(final Class<?> type) {

        final Map<Class<?>, Object> objectFactories = getObjectFactories();
        if (objectFactories != null) {
            return createDependencyInstance(type, objectFactories.get(type));
        }
        return null;
    }

    /**
     * Create dependency object
     * 
     * @param type
     *            dependency type
     * @param objectFactory
     *            Object factory
     * @return Dependency object
     */
    protected Object createDependencyInstance(final Class<?> type, final Object objectFactory) {
        if (type.isInstance(objectFactory)) {
            return objectFactory;
        }
        if (objectFactory instanceof ObjectFactory) {
            return createObjectFactoryDependencyProxy(type, (ObjectFactory<?>) objectFactory);
        }
        return null;
    }

    protected Object createObjectFactoryDependencyProxy(final Class<?> type, final ObjectFactory<?> objectFactory) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type },
                                      new ObjectFactoryDelegatingHandler(objectFactory));
    }

    /**
     * Get {@link ObjectFactory}s
     * 
     * @since 2.3.7
     * @return {@link ObjectFactory}s
     */
    public final Map<Class<?>, Object> getObjectFactories() {
        final Map<Class<?>, Object> objectFactories = this.objectFactories;
        return objectFactories == null
                ? this.objectFactories = createObjectFactories()
                : objectFactories;
    }

    protected Map<Class<?>, Object> createObjectFactories() {
        return null;
    }

    public void setObjectFactories(Map<Class<?>, Object> objectFactories) {
        this.objectFactories = objectFactories;
    }

    /**
     * Reflective InvocationHandler for lazy access to the current target object.
     */
    @SuppressWarnings("serial")
    public static class ObjectFactoryDelegatingHandler implements InvocationHandler, Serializable {

        private final ObjectFactory<?> objectFactory;

        public ObjectFactoryDelegatingHandler(ObjectFactory<?> objectFactory) {
            this.objectFactory = requireNonNull(objectFactory);
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                return method.invoke(objectFactory.getObject(), args);
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }

    // ---------------------------------------

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        final BeanDefinition def = getBeanDefinition(name);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(name);
        }
        return def.isSingleton();
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        return !isSingleton(name);
    }

    @Override
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        final BeanDefinition def = getBeanDefinition(name);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(name);
        }
        return def.getBeanClass();
    }

    @Override
    public Set<String> getAliases(Class<?> type) {
        return getBeanDefinitions()
                .entrySet()
                .stream()
                .filter(entry -> type.isAssignableFrom(entry.getValue().getBeanClass()))
                .map(entry -> entry.getKey())
                .collect(Collectors.toSet());
    }

    @Override
    public void registerBean(Class<?> clazz) throws BeanDefinitionStoreException {
        registerBean(getBeanNameCreator().create(clazz), clazz);
    }

    @Override
    public void registerBean(Set<Class<?>> candidates) throws BeanDefinitionStoreException {
        final BeanNameCreator nameCreator = getBeanNameCreator();
        for (final Class<?> candidate : candidates) {
            registerBean(nameCreator.create(candidate), candidate);
        }
    }

    @Override
    public void registerBean(String name, Class<?> clazz) throws BeanDefinitionStoreException {
        getBeanDefinitionLoader().loadBeanDefinition(name, clazz);
    }

    @Override
    public void registerBean(String name, BeanDefinition beanDefinition) //
            throws BeanDefinitionStoreException, ConfigurationException //
    {
        getBeanDefinitionLoader().register(name, beanDefinition);
    }

    @Override
    public void registerBean(Object obj) throws BeanDefinitionStoreException {
        registerBean(getBeanNameCreator().create(obj.getClass()), obj);
    }

    @Override
    public void registerBean(final String name, final Object obj) throws BeanDefinitionStoreException {
        Assert.notNull(obj, "bean instance must not be null");

        String nameToUse = name;
        final Class<? extends Object> beanClass = obj.getClass();
        if (StringUtils.isEmpty(nameToUse)) {
            nameToUse = getBeanNameCreator().create(beanClass);
        }
        getBeanDefinitionLoader().loadBeanDefinition(nameToUse, beanClass);
        final BeanDefinition def = getBeanDefinition(name);
        if (def.isSingleton()) {
            registerSingleton(name, obj);
        }
    }

    @Override
    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");
        getPostProcessors().remove(beanPostProcessor);
        getPostProcessors().add(beanPostProcessor);
    }

    @Override
    public void removeBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        getPostProcessors().remove(beanPostProcessor);
    }

    @Override
    public void registerSingleton(final String name, final Object singleton) {
        Assert.notNull(name, "Bean name must not be null");
        Assert.notNull(singleton, "Singleton object must not be null");

        synchronized (singletons) {
            final Object oldBean = singletons.put(name, singleton);
            if (log.isDebugEnabled()) {
                if (oldBean == null) {
                    log.debug("Register Singleton: [{}] = [{}]", name, singleton);
                }
                else if (oldBean != singleton) {
                    log.debug("Refresh Singleton: [{}] = [{}] old bean: [{}] ", name, singleton, oldBean);
                }
            }
        }
    }

    @Override
    public void registerSingleton(Object bean) {
        registerSingleton(getBeanNameCreator().create(bean.getClass()), bean);
    }

    @Override
    public Map<String, Object> getSingletons() {
        return singletons;
    }

    @Override
    public Map<String, Object> getSingletonsMap() {
        return singletons;
    }

    @Override
    public Object getSingleton(String name) {
        return singletons.get(name);
    }

    /**
     * Get target singleton
     * 
     * @param name
     *            Bean name
     * @param targetClass
     *            Target class
     * @return Target singleton
     */
    public <T> T getSingleton(String name, Class<T> targetClass) {
        return targetClass.cast(getSingleton(name));
    }

    @Override
    public void removeSingleton(String name) {
        singletons.remove(name);
    }

    @Override
    public void removeBean(String name) {
        removeBeanDefinition(name);
        removeSingleton(name);
    }

    @Override
    public boolean containsSingleton(String name) {
        return singletons.containsKey(name);
    }

    @Override
    public void registerBeanDefinition(final String beanName, final BeanDefinition beanDefinition) {

        this.beanDefinitionMap.put(beanName, beanDefinition);

        final PropertyValue[] propertyValues = beanDefinition.getPropertyValues();
        if (ObjectUtils.isNotEmpty(propertyValues)) {
            for (final PropertyValue propertyValue : propertyValues) {
                if (propertyValue.getValue() instanceof BeanReference) {
                    this.dependencies.add(propertyValue);
                }
            }
        }
    }

    @Override
    public void registerScope(String name, Scope scope) {
        Assert.notNull(name, "scope name must not be null");
        Assert.notNull(scope, "scope object must not be null");
        scopes.put(name, scope);
    }

    /**
     * Destroy a bean with bean instance and bean definition
     * 
     * @param beanInstance
     *            Bean instance
     * @param def
     *            Bean definition
     */
    public void destroyBean(final Object beanInstance, final BeanDefinition def) {
        if (beanInstance == null || def == null) {
            return;
        }
        try {
            // use real class
            final Class<?> beanClass = ClassUtils.getUserClass(beanInstance);
            for (final String destroyMethod : def.getDestroyMethods()) {
                beanClass.getMethod(destroyMethod).invoke(beanInstance);
            }
            for (final BeanPostProcessor postProcessor : getPostProcessors()) {
                if (postProcessor instanceof DestructionBeanPostProcessor) {
                    final DestructionBeanPostProcessor destruction = (DestructionBeanPostProcessor) postProcessor;
                    if (destruction.requiresDestruction(beanInstance)) {
                        destruction.postProcessBeforeDestruction(beanInstance, def.getName());
                    }
                }
            }
            ContextUtils.destroyBean(beanInstance);
        }
        catch (Throwable e) {
            e = unwrapThrowable(e);
            removeBean(def.getName());
            throw new ContextException("An Exception Occurred When Destroy a bean: [" + def.getName() + "], With Msg: [" + e + "]", e);
        }
    }

    @Override
    public void destroyBean(String name) {

        BeanDefinition beanDefinition = getBeanDefinition(name);

        if (beanDefinition == null && name.charAt(0) == FACTORY_BEAN_PREFIX_CHAR) {
            // if it is a factory bean
            final String factoryBeanName = name.substring(1);
            beanDefinition = getBeanDefinition(factoryBeanName);
            destroyBean(getSingleton(factoryBeanName), beanDefinition);
            removeBean(factoryBeanName);
        }
        destroyBean(getSingleton(name), beanDefinition);
        removeBean(name);
    }

    @Override
    public void destroyScopedBean(String beanName) {
        final BeanDefinition def = getBeanDefinition(beanName);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(beanName);
        }
        if (def.isSingleton() || def.isPrototype()) {
            throw new IllegalArgumentException("Bean name '"
                    + beanName + "' does not correspond to an object in a mutable scope");
        }
        final Scope scope = scopes.get(def.getScope());
        if (scope == null) {
            throw new IllegalStateException("No Scope SPI registered for scope name '" + def.getScope() + "'");
        }
        final Object bean = scope.remove(beanName);
        if (bean != null) {
            destroyBean(bean, def);
        }
    }

    @Override
    public String getBeanName(Class<?> targetClass) throws NoSuchBeanDefinitionException {

        for (final Entry<String, BeanDefinition> entry : getBeanDefinitions().entrySet()) {
            if (entry.getValue().getBeanClass() == targetClass) {
                return entry.getKey();
            }
        }
        throw new NoSuchBeanDefinitionException(targetClass);
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        beanDefinitionMap.remove(beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionMap.get(beanName);
    }

    @Override
    public BeanDefinition getBeanDefinition(Class<?> beanClass) {

        final BeanDefinition beanDefinition = getBeanDefinition(getBeanNameCreator().create(beanClass));
        if (beanDefinition != null && beanClass.isAssignableFrom(beanDefinition.getBeanClass())) {
            return beanDefinition;
        }
        for (final BeanDefinition definition : getBeanDefinitions().values()) {
            if (beanClass.isAssignableFrom(definition.getBeanClass())) {
                return definition;
            }
        }
        return null;
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return getBeanDefinitions().containsKey(beanName);
    }

    @Override
    public boolean containsBeanDefinition(Class<?> type) {
        return containsBeanDefinition(type, false);
    }

    @Override
    public boolean containsBeanDefinition(final Class<?> type, final boolean equals) {

        final Predicate<BeanDefinition> predicate = getPredicate(type, equals);
        final BeanDefinition def = getBeanDefinition(getBeanNameCreator().create(type));
        if (def != null && predicate.test(def)) {
            return true;
        }

        for (final BeanDefinition beanDef : getBeanDefinitions().values()) {
            if (predicate.test(beanDef)) {
                return true;
            }
        }
        return false;
    }

    private final Predicate<BeanDefinition> getPredicate(final Class<?> type, final boolean equals) {
        return equals
                ? (beanDef) -> type == beanDef.getBeanClass()
                : (beanDef) -> type.isAssignableFrom(beanDef.getBeanClass());
    }

    @Override
    public Set<String> getBeanDefinitionNames() {
        return getBeanDefinitions().keySet();
    }

    @Override
    public int getBeanDefinitionCount() {
        return getBeanDefinitions().size();
    }

    public Set<PropertyValue> getDependencies() {
        return dependencies;
    }

    @Override
    public void initializeSingletons() throws Throwable {

        log.debug("Initialization of singleton objects.");

        for (final BeanDefinition def : getBeanDefinitions().values()) {
            if (def.isSingleton() && !def.isInitialized()) {
                initializeSingleton(def);
            }
        }

        log.debug("The singleton objects are initialized.");
    }

    /**
     * Initialization singletons that has already in context
     */
    public void preInitialization() {

        for (final Entry<String, Object> entry : new HashMap<>(getSingletons()).entrySet()) {

            final String name = entry.getKey();
            final BeanDefinition def = getBeanDefinition(name);
            if (def == null || def.isInitialized()) {
                continue;
            }
            initializeSingleton(entry.getValue(), def);
            if (log.isDebugEnabled()) {
                log.debug("Pre initialize singleton bean is being stored in the name of [{}].", name);
            }
        }
    }

    // -----------------------------------------------------

    @Override
    public void refresh(String name) {

        final BeanDefinition def = getBeanDefinition(name);
        if (def == null) {
            throw new NoSuchBeanDefinitionException(name);
        }
        if (!def.isInitialized()) {
            initializeSingleton(def);
        }
        else if (log.isWarnEnabled()) {
            log.warn("A bean named: [{}] has already initialized", name);
        }
    }

    @Override
    public Object refresh(BeanDefinition def) {
        return getBean(def);
    }

    // -----------------------------

    public abstract BeanDefinitionLoader getBeanDefinitionLoader();

    /**
     * Get a bean name creator
     * 
     * @return {@link BeanNameCreator}
     */
    public BeanNameCreator getBeanNameCreator() {
        final BeanNameCreator ret = this.beanNameCreator;
        return ret == null ? this.beanNameCreator = createBeanNameCreator() : ret;
    }

    /**
     * create {@link BeanNameCreator}
     * 
     * @return a default {@link BeanNameCreator}
     */
    protected BeanNameCreator createBeanNameCreator() {
        return new DefaultBeanNameCreator(true);
    }

    public final List<BeanPostProcessor> getPostProcessors() {
        return postProcessors;
    }

    @Override
    public void enableFullPrototype() {
        setFullPrototype(true);
    }

    @Override
    public void enableFullLifecycle() {
        setFullLifecycle(true);
    }

    public boolean isFullPrototype() {
        return fullPrototype;
    }

    public boolean isFullLifecycle() {
        return fullLifecycle;
    }

    public void setFullPrototype(boolean fullPrototype) {
        this.fullPrototype = fullPrototype;
    }

    public void setFullLifecycle(boolean fullLifecycle) {
        this.fullLifecycle = fullLifecycle;
    }

    public void setBeanNameCreator(BeanNameCreator beanNameCreator) {
        this.beanNameCreator = beanNameCreator;
    }
}
