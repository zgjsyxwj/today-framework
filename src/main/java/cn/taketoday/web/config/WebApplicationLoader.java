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
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */
package cn.taketoday.web.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cn.taketoday.context.annotation.Autowired;
import cn.taketoday.context.annotation.Env;
import cn.taketoday.context.annotation.Props;
import cn.taketoday.context.annotation.Value;
import cn.taketoday.context.env.ConfigurableEnvironment;
import cn.taketoday.context.exception.BeanDefinitionStoreException;
import cn.taketoday.context.exception.ConfigurationException;
import cn.taketoday.context.exception.NoSuchBeanDefinitionException;
import cn.taketoday.context.io.Resource;
import cn.taketoday.context.utils.ClassUtils;
import cn.taketoday.context.utils.ContextUtils;
import cn.taketoday.context.utils.OrderUtils;
import cn.taketoday.context.utils.ResourceUtils;
import cn.taketoday.context.utils.StringUtils;
import cn.taketoday.web.Constant;
import cn.taketoday.web.WebApplicationContext;
import cn.taketoday.web.event.WebApplicationStartedEvent;
import cn.taketoday.web.mapping.HandlerMethod;
import cn.taketoday.web.mapping.MethodParameter;
import cn.taketoday.web.mapping.ResourceMappingRegistry;
import cn.taketoday.web.multipart.MultipartConfiguration;
import cn.taketoday.web.resolver.ControllerAdviceExceptionResolver;
import cn.taketoday.web.resolver.ExceptionResolver;
import cn.taketoday.web.resolver.method.ConverterParameterResolver;
import cn.taketoday.web.resolver.method.DelegatingParameterResolver;
import cn.taketoday.web.resolver.method.ParameterResolver;
import cn.taketoday.web.resolver.result.ResultResolver;
import cn.taketoday.web.servlet.WebServletApplicationContext;
import cn.taketoday.web.view.AbstractViewResolver;
import cn.taketoday.web.view.FreeMarkerViewResolver;
import cn.taketoday.web.view.ViewResolver;

/**
 * @author TODAY <br>
 *         2019-07-10 23:12
 */
public class WebApplicationLoader implements WebApplicationInitializer, Constant {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(WebApplicationLoader.class);

    private ViewConfiguration viewConfiguration;

    /** context **/
    protected static WebApplicationContext applicationContext;

    /**
     * Get {@link WebServletApplicationContext}
     * 
     * @return {@link WebServletApplicationContext}
     */
    protected final static WebApplicationContext getWebApplicationContext() {
        return applicationContext;
    }

    private DocumentBuilder builder;

    @Override
    public void onStartup(WebApplicationContext applicationContext) throws Throwable {

        final ConfigurableEnvironment environment = applicationContext.getEnvironment();

        final WebMvcConfiguration mvcConfiguration = getWebMvcConfiguration();

        configureMultipart(applicationContext.getBean(MultipartConfiguration.class), mvcConfiguration);

        configureResultResolver(applicationContext.getBeans(ResultResolver.class), mvcConfiguration);

        configureParameterResolver(applicationContext.getBeans(ParameterResolver.class), mvcConfiguration);
        configureViewResolver(applicationContext.getBean(AbstractViewResolver.class), mvcConfiguration);

        configureResourceRegistry(applicationContext.getBean(ResourceMappingRegistry.class), mvcConfiguration);

        if (environment.getProperty(ENABLE_WEB_MVC_XML, Boolean::parseBoolean, true)) {
            this.viewConfiguration = applicationContext.getBean(VIEW_CONFIG, ViewConfiguration.class);
            initFrameWorkFromWebMvcXml();
        }

        // check all resolver
        checkFrameWorkResolvers();

        initializerStartup(applicationContext);

        applicationContext.publishEvent(new WebApplicationStartedEvent(applicationContext));
        if (environment.getProperty(ENABLE_WEB_STARTED_LOG, Boolean::parseBoolean, true)) {
            log.info("Your Application Started Successfully, It takes a total of [{}] ms.", //
                    System.currentTimeMillis() - applicationContext.getStartupDate()//
            );
        }

        Runtime.getRuntime().addShutdownHook(new Thread(applicationContext::close));

        System.gc();
    }

    protected void configureResultResolver(List<ResultResolver> resultResolvers, WebMvcConfiguration mvcConfiguration) {

        mvcConfiguration.configureResultResolver(resultResolvers);
        OrderUtils.reversedSort(resultResolvers);

        HandlerMethod.addResolver(resultResolvers);
    }

    protected void configureParameterResolver(List<ParameterResolver> parameterResolvers, WebMvcConfiguration mvcConfiguration) {

        parameterResolvers.add(new ConverterParameterResolver((m) -> m.is(String.class), (s) -> s));
        parameterResolvers.add(new ConverterParameterResolver((m) -> m.is(Long.class) || m.is(long.class), Long::parseLong));
        parameterResolvers.add(new ConverterParameterResolver((m) -> m.is(Integer.class) || m.is(int.class), Integer::parseInt));
        parameterResolvers.add(new ConverterParameterResolver((m) -> m.is(Short.class) || m.is(short.class), Short::parseShort));
        parameterResolvers.add(new ConverterParameterResolver((m) -> m.is(Float.class) || m.is(float.class), Float::parseFloat));
        parameterResolvers.add(new ConverterParameterResolver((m) -> m.is(Double.class) || m.is(double.class), Double::parseDouble));
        parameterResolvers.add(new ConverterParameterResolver((m) -> m.is(Boolean.class) || m.is(boolean.class), Boolean::parseBoolean));

        parameterResolvers.add(new DelegatingParameterResolver((m) -> m.isAnnotationPresent(Value.class), //
                (context, parameter) -> ContextUtils.resolveValue(parameter.getAnnotation(Value.class), parameter.getParameterClass())//
        ));

        parameterResolvers.add(new DelegatingParameterResolver((m) -> m.isAnnotationPresent(Env.class), //
                (context, parameter) -> ContextUtils.resolveValue(parameter.getAnnotation(Env.class), parameter.getParameterClass())//
        ));
        parameterResolvers.add(new DelegatingParameterResolver((m) -> m.isAnnotationPresent(Props.class), //
                (context, parameter) -> ContextUtils.resolveProps(parameter.getAnnotation(Props.class), parameter.getParameterClass(), null)//
        ));

        parameterResolvers.add(new DelegatingParameterResolver((m) -> m.isAnnotationPresent(Autowired.class), //
                (context, parameter) -> {
                    final Autowired autowired = parameter.getAnnotation(Autowired.class);
                    final String name = autowired.value();

                    final Object bean;
                    if (StringUtils.isEmpty(name)) {
                        bean = applicationContext.getBean(parameter.getParameterClass());
                    }
                    else {
                        bean = applicationContext.getBean(name, parameter.getParameterClass());
                    }
                    if (bean == null && autowired.required()) {
                        throw new NoSuchBeanDefinitionException(parameter.getParameterClass());
                    }
                    return bean;
                }//
        ));

        mvcConfiguration.configureParameterResolver(parameterResolvers);

        OrderUtils.reversedSort(parameterResolvers);

        MethodParameter.addResolver(parameterResolvers);
    }

    private void configureMultipart(MultipartConfiguration multipartConfiguration, WebMvcConfiguration mvcConfiguration) {
        mvcConfiguration.configureMultipart(multipartConfiguration);
    }

    protected void configureResourceRegistry(ResourceMappingRegistry registry, WebMvcConfiguration mvcConfiguration) {
        mvcConfiguration.configureResourceMappings(registry);
    }

    protected void configureViewResolver(AbstractViewResolver viewResolver, WebMvcConfiguration mvcConfiguration) {
        if (viewResolver != null) {
            mvcConfiguration.configureViewResolver(viewResolver);
        }
    }

    protected void initializerStartup(WebApplicationContext applicationContext) throws Throwable {
        for (final WebApplicationInitializer initializer : getInitializers(applicationContext)) {
            initializer.onStartup(applicationContext);
        }
    }

    protected List<WebApplicationInitializer> getInitializers(WebApplicationContext applicationContext) {
        return applicationContext.getBeans(WebApplicationInitializer.class);
    }

    protected WebMvcConfiguration getWebMvcConfiguration() {
        return new CompositeWebMvcConfiguration(applicationContext.getBeans(WebMvcConfiguration.class));
    }

    /**
     * Initialize framework.
     * 
     * @throws Throwable
     *             if any Throwable occurred
     */
    protected void initFrameWorkFromWebMvcXml() throws Throwable {

        // find the configure file
        log.info("TODAY WEB Framework Is Looking For Configuration File.");

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setIgnoringComments(true);
        builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> {
            if (systemId.contains(DTD_NAME) || publicId.contains(DTD_NAME)) {
                return new InputSource(//
                        new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes())//
                );
            }
            return null;
        });

        final String webMvcConfigLocation = getWebMvcConfigLocation();

        if (StringUtils.isEmpty(webMvcConfigLocation)) {
            return;
        }

        for (final String file : StringUtils.split(webMvcConfigLocation)) {

            final Resource resource = ResourceUtils.getResource(file);
            if (resource == null || !resource.exists()) {
                final ConfigurationException configurationException = //
                        new ConfigurationException("Your Provided Configuration File: [" + file + "], Does Not Exist");
                throw configurationException;
            }
            try (final InputStream inputStream = resource.getInputStream()) {
                registerXml(builder.parse(inputStream), file);// fixed
            }
        }
        builder = null;
    }

    protected String getWebMvcConfigLocation() throws Throwable {
        return getWebApplicationContext().getEnvironment().getProperty(WEB_MVC_CONFIG_LOCATION);
    }

    /**
     * Find configuration file.
     * 
     * @param dir
     *            directory
     * @throws Exception
     */
    protected void findConfiguration(File dir) throws Throwable {

        log.debug("Enter [{}]", dir.getAbsolutePath());

        final File[] listFiles = dir.listFiles(path -> (path.isDirectory() || path.getName().endsWith(".xml")));
        if (listFiles == null) {
            log.error("File: [{}] Does not exist", dir);
            return;
        }
        for (File file : listFiles) {
            if (file.isDirectory()) { // recursive
                findConfiguration(file);
            }
            else {
                try (InputStream inputStream = new FileInputStream(file)) {
                    registerXml(builder.parse(inputStream), file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * configure with xml file
     * 
     * @param doc
     *            xml file
     * @throws Throwable
     */
    protected final void registerXml(final Document doc, final String filePath) throws Throwable {

        final Element root = doc.getDocumentElement();
        if (ROOT_ELEMENT.equals(root.getNodeName())) { // root element
            log.info("Found Configuration File: [{}].", filePath);
            configureStart(root);
        }
    }

    /**
     * Start configure.
     * 
     * @param root
     *            Root element
     */
    protected void configureStart(Element root) throws Throwable {

        final NodeList nl = root.getChildNodes();
        final int length = nl.getLength();

        for (int i = 0; i < length; i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                String nodeName = ele.getNodeName();

                log.debug("Found Element: [{}]", nodeName);

                if (ELEMENT_CONTROLLER.equals(nodeName)) {
                    viewConfiguration.configuration(ele);
                } // ELEMENT_RESOURCES // TODO
                else {
                    log.warn("This element: [{}] is not supported.", nodeName);
                }
            }
        }
    }

    /**
     * Register resolver to application context.
     * 
     * @param element
     *            xml element
     * @param defaultClass
     *            default class
     * @param name
     *            bean name
     * @param refresh
     *            refresh ?
     * @return Resolver's Class
     * @throws ClassNotFoundException
     * @throws BeanDefinitionStoreException
     */
    protected static Class<?> registerResolver(Element element, Class<?> defaultClass, String name, boolean refresh) //
            throws ClassNotFoundException, BeanDefinitionStoreException //
    {
        String attrClass = element.getAttribute(ATTR_CLASS); // class="cn.taketoday..."

        Class<?> resolverClass = null;
        if (defaultClass.getName().equals(attrClass)) { // Custom
            resolverClass = defaultClass; // default
        }
        else {
            resolverClass = ClassUtils.forName(attrClass);
        }
        // register resolver
        applicationContext.registerBean(name, resolverClass);
        log.info("Register [{}] onto [{}]", name, resolverClass.getName());

        if (refresh) {
            applicationContext.refresh(name);
        }
        return resolverClass;
    }

    /**
     * Check resolvers
     */
    protected static void checkFrameWorkResolvers() {

        WebApplicationContext applicationContext = getWebApplicationContext();

        if (!applicationContext.containsBeanDefinition(ViewResolver.class)) {
            // use freemarker view resolver
            applicationContext.registerBean(VIEW_RESOLVER, FreeMarkerViewResolver.class);
            applicationContext.refresh(VIEW_RESOLVER);
            log.info("Use default view resolver: [{}].", FreeMarkerViewResolver.class);
        }

        if (!applicationContext.containsBeanDefinition(ExceptionResolver.class)) {
            applicationContext.registerBean(EXCEPTION_RESOLVER, ControllerAdviceExceptionResolver.class);
            applicationContext.refresh(EXCEPTION_RESOLVER);
            log.info("Use default exception resolver: [{}].", ControllerAdviceExceptionResolver.class);
        }
    }

    // -------------------------------------

    /**
     * @author TODAY <br>
     *         2019-05-17 17:46
     */
    protected class CompositeWebMvcConfiguration implements WebMvcConfiguration {

        private final List<WebMvcConfiguration> webMvcConfigurations;

        public CompositeWebMvcConfiguration(List<WebMvcConfiguration> webMvcConfigurations) {
            OrderUtils.reversedSort(webMvcConfigurations);
            this.webMvcConfigurations = webMvcConfigurations;
        }

        @Override
        public void configureViewResolver(AbstractViewResolver viewResolver) {
            for (WebMvcConfiguration webMvcConfiguration : getWebMvcConfigurations()) {
                webMvcConfiguration.configureViewResolver(viewResolver);
            }
        }

        @Override
        public void configureResourceMappings(ResourceMappingRegistry registry) {
            for (WebMvcConfiguration webMvcConfiguration : getWebMvcConfigurations()) {
                webMvcConfiguration.configureResourceMappings(registry);
            }
        }

        public List<WebMvcConfiguration> getWebMvcConfigurations() {
            return webMvcConfigurations;
        }
    }

}
