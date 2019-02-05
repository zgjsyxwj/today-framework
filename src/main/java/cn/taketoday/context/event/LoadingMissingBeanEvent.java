/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © Today & 2017 - 2019 All Rights Reserved.
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
package cn.taketoday.context.event;

import cn.taketoday.context.ApplicationContext;

import java.util.Collection;

/**
 * @author TODAY <br>
 *         2019-02-02 11:05
 */
@SuppressWarnings("serial")
public class LoadingMissingBeanEvent extends ApplicationContextEvent {

	private final Collection<Class<?>> beanClasses;

	public LoadingMissingBeanEvent(ApplicationContext source) {
		this(source, null);
	}

	public LoadingMissingBeanEvent(ApplicationContext source, Collection<Class<?>> beanClasses) {
		super(source);
		this.beanClasses = beanClasses;
	}

	public Collection<Class<?>> getBeanClasses() {
		return beanClasses;
	}

}
