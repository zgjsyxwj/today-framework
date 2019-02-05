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
package cn.taketoday.context.loader;

import cn.taketoday.context.ApplicationContext;
import cn.taketoday.context.BeanNameCreator;
import cn.taketoday.context.annotation.Autowired;
import cn.taketoday.context.bean.BeanDefinition;
import cn.taketoday.context.bean.BeanReference;
import cn.taketoday.context.bean.PropertyValue;
import cn.taketoday.context.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.Map.Entry;

import javax.annotation.Resource;

/**
 * @author Today <br>
 * 
 *         2018-08-04 15:56
 */
public class AutowiredPropertyResolver implements PropertyValueResolver {

	private BeanNameCreator beanNameCreator;

	@Override
	public PropertyValue resolveProperty(ApplicationContext applicationContext, Field field) {

		if (beanNameCreator == null) {
			this.beanNameCreator = applicationContext.getEnvironment().getBeanNameCreator();
		}

		Autowired autowired = field.getAnnotation(Autowired.class); // auto wired

		boolean required = true;
		Class<?> propertyClass = field.getType();

		String name = null;

		if (autowired != null) {
			if (StringUtils.isNotEmpty(autowired.value())) {
				name = autowired.value();
			}
			else {
				name = byType(applicationContext, propertyClass);
			}
			required = autowired.required(); // class name
		}

		if (StringUtils.isEmpty(name) && field.isAnnotationPresent(Resource.class)) {
			// Resource.class
			Resource resource = field.getAnnotation(Resource.class);
			if (StringUtils.isNotEmpty(resource.name())) {
				name = resource.name();
			}
			else if (resource.type() != Object.class) {
				name = byType(applicationContext, propertyClass);
			}
			else {
				name = beanNameCreator.create(propertyClass);
			}
		}

		return new PropertyValue(new BeanReference()//
				.setName(name)//
				.setRequired(required)//
				.setReferenceClass(propertyClass), field//
		);
	}

	/**
	 * @param applicationContext
	 * @param targetClass
	 * @return
	 */
	private String byType(ApplicationContext applicationContext, Class<?> targetClass) {
		if (applicationContext.hasStarted()) {
			String name = findName(applicationContext, targetClass);
			if (StringUtils.isNotEmpty(name)) {
				return name;
			}
		}
		return beanNameCreator.create(targetClass);
	}

	/**
	 * 
	 * @param applicationContext
	 * @param propertyClass
	 * @return
	 */
	private String findName(ApplicationContext applicationContext, Class<?> propertyClass) {
		for (Entry<String, BeanDefinition> entry : applicationContext.getBeanDefinitionsMap().entrySet()) {
			if (propertyClass.isAssignableFrom(entry.getValue().getBeanClass())) {
				return entry.getKey();
			}
		}
		return null;
	}

}
