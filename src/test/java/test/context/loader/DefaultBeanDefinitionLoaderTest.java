/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://yanghaijian.top
 * Copyright © Today & 2017 - 2018 All Rights Reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package test.context.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cn.taketoday.context.bean.BeanDefinition;
import cn.taketoday.context.exception.BeanDefinitionStoreException;
import cn.taketoday.context.exception.ConfigurationException;
import cn.taketoday.context.factory.BeanDefinitionRegistry;
import cn.taketoday.context.factory.BeanPostProcessor;
import cn.taketoday.context.factory.SimpleBeanDefinitionRegistry;
import cn.taketoday.context.loader.DefaultBeanDefinitionLoader;
import cn.taketoday.context.utils.ClassUtils;
import test.domain.User;

/**
 * Default Bean Definition Loader implements
 * 
 * @author Today <br>
 *         2018-06-23 11:18:22
 */
public final class DefaultBeanDefinitionLoaderTest {

	private long						start;

	private BeanDefinitionRegistry		registry;

	private List<BeanPostProcessor>		postProcessors;

	private DefaultBeanDefinitionLoader	beanDefinitionLoader;

	@Before
	public void start() {
		postProcessors = new ArrayList<>();
		registry = new SimpleBeanDefinitionRegistry();
		beanDefinitionLoader = new DefaultBeanDefinitionLoader(registry);
		start = System.currentTimeMillis();
	}

	@After
	public void end() {
		System.out.println("process takes " + (System.currentTimeMillis() - start) + "ms.");
	}

	@Test
	public void test_LoadBeanDefinition() throws BeanDefinitionStoreException, ConfigurationException {

		beanDefinitionLoader.loadBeanDefinition(User.class);
		beanDefinitionLoader.loadBeanDefinition("user", User.class);
		beanDefinitionLoader.loadBeanDefinitions(ClassUtils.scanPackage("cn.taketoday.test.dao"));

		Map<String, BeanDefinition> beanDefinitionsMap = registry.getBeanDefinitionsMap();

		assert beanDefinitionsMap.size() > 0;

		System.out.println(beanDefinitionsMap);
	}

	@Test
	public void test_LoadBeanPostProcessor() throws BeanDefinitionStoreException, ConfigurationException {

		beanDefinitionLoader.loadBeanDefinitions(ClassUtils.scanPackage("cn.taketoday.test.context.loader"));

		Map<String, BeanDefinition> beanDefinitionsMap = registry.getBeanDefinitionsMap();

		assert postProcessors.size() > 0;
		assert beanDefinitionsMap.size() > 0;

		System.out.println(postProcessors);
		System.out.println(beanDefinitionsMap);
	}

}
