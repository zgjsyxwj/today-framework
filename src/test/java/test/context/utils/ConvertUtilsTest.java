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
package test.context.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.taketoday.context.Scope;
import cn.taketoday.context.conversion.TypeConverter;
import cn.taketoday.context.exception.ConfigurationException;
import cn.taketoday.context.exception.ConversionException;
import cn.taketoday.context.io.Resource;
import cn.taketoday.context.utils.ConvertUtils;
import cn.taketoday.context.utils.StringUtils;

/**
 * 
 * @author Today <br>
 *         2018-07-12 20:43:53
 */
public class ConvertUtilsTest {

    private long start;

    @Before
    public void start() {
        start = System.currentTimeMillis();
    }

    @After
    public void end() {
        System.out.println("process takes " + (System.currentTimeMillis() - start) + "ms.");
    }

    @Test
    public void addConverter() {
        ConvertUtils.addConverter(
                new TypeConverter() {

                    @Override
                    public boolean supports(Class<?> targetClass, Object source) {
                        return false;
                    }

                    @Override
                    public Object convert(Class<?> targetClass, Object source) throws ConversionException {
                        return null;
                    }
                });
    }

    @Test
    public void testConvert() throws IOException {

        //
        assert "123".equals(ConvertUtils.convert("123", String.class));
        // ----------number
        Object convert = ConvertUtils.convert("123", Integer.class);
        assert convert.getClass() == Integer.class;
        assert convert.equals(123);

        Integer[] convertArray = (Integer[]) ConvertUtils.convert("12;456,121", Integer[].class);

        System.err.println(Arrays.toString(convertArray));

        assert convertArray.getClass().isArray();
        assert convertArray.length == 3;
        assert convertArray[0] == 12;

        assert ConvertUtils.convert("123", Long.class).equals(123l);
        assert ConvertUtils.convert("123", Double.class).equals(123d);
        assert ConvertUtils.convert("123", Float.class).equals(123f);
        assert ConvertUtils.convert("123", long.class).equals(123l);
        assert ConvertUtils.convert("123", double.class).equals(123d);
        assert ConvertUtils.convert("123", float.class).equals(123f);

        try {
            ConvertUtils.convert(".Float", ConvertUtilsTest.class);
        }
        catch (ConversionException e) {
        }
        // -- Class
        assert ConvertUtils.convert("java.lang.Float", Class.class).equals(Float.class);
        try {
            ConvertUtils.convert("Float", Class.class);
        }
        catch (ConversionException e) {
            assert e.getCause().getClass().equals(ClassNotFoundException.class);
        }
        try {
            ConvertUtils.convert("/info", Resource.class);
        }
        catch (ConversionException e) {
            assert e.getCause().getClass().equals(ConfigurationException.class);
        }

        // --Resource
        final Object resource = ConvertUtils.convert("/info.properties", Resource.class);
        assert resource instanceof Resource;
        assert ((Resource) resource).getName().equals("info.properties");//

        final Object url = ConvertUtils.convert("/info.properties", URL.class);
        assert url instanceof URL;

        final InputStream openStream = ((URL) url).openStream();

        final String readAsText = StringUtils.readAsText(openStream);
        assert readAsText != null;
        System.err.println(readAsText);
        // uri
        final Object uri = ConvertUtils.convert("/info.properties", URI.class);
        assert uri instanceof URI;
        assert StringUtils.readAsText(((URI) uri).toURL().openStream()) != null;
        // file
        final Object file = ConvertUtils.convert("/info.properties", File.class);
        assert file instanceof File;
        assert ((File) file).getName().equals("info.properties");
        assert StringUtils.readAsText(Files.newInputStream(((File) file).toPath())) != null;

        // enum
        final Object scope = ConvertUtils.convert("SINGLETON", Scope.class);
        assert scope instanceof Scope;
        assert scope == Scope.SINGLETON;
        // array
        final Object scopes = ConvertUtils.convert("SINGLETON,PROTOTYPE", Scope[].class);
        assert scopes instanceof Scope[];

        Scope[] sc = (Scope[]) scopes;
        assert sc[0] == Scope.SINGLETON;
        assert sc[1] == Scope.PROTOTYPE;
        // Constructor

        final Object test = ConvertUtils.convert("123", TEST.class);
        assert test instanceof TEST;
        assert ((TEST) test).test.equals("123");
        try {
            ConvertUtils.convert("123", TEST_NONE.class);
        }
        catch (ConversionException e) {
        }

        try {
            ConvertUtils.convert("123", TEST_THROW.class);
        }
        catch (ConversionException e) {
            assert e.getCause().getClass().equals(InvocationTargetException.class);
        }

    }

    public static class TEST_THROW {
        public TEST_THROW(String test) {
            throw new ConfigurationException();
        }
    }

    public static class TEST_NONE {

    }

    public static class TEST {
        private String test;

        public TEST(String test) {
            this.test = test;
        }
    }

    @Test
    public void testParseDuration() {

        Duration s = ConvertUtils.parseDuration("123s");
        Duration h = ConvertUtils.parseDuration("123h");
        Duration ns = ConvertUtils.parseDuration("123ns");
        Duration ms = ConvertUtils.parseDuration("123ms");
        Duration min = ConvertUtils.parseDuration("123min");
        Duration d = ConvertUtils.parseDuration("123d");

        Duration convert = ConvertUtils.parseDuration("PT20S");

        assert s.equals(Duration.of(123, ChronoUnit.SECONDS));
        assert h.equals(Duration.of(123, ChronoUnit.HOURS));
        assert ns.equals(Duration.of(123, ChronoUnit.NANOS));
        assert ms.equals(Duration.of(123, ChronoUnit.MILLIS));
        assert min.equals(Duration.of(123, ChronoUnit.MINUTES));
        assert d.equals(Duration.of(123, ChronoUnit.DAYS));

        assert convert.equals(Duration.of(20l, ChronoUnit.SECONDS));
    }
}
