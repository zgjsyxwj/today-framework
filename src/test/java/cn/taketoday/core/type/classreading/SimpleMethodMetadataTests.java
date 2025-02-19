/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
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

package cn.taketoday.core.type.classreading;

import cn.taketoday.core.type.AbstractMethodMetadataTests;
import cn.taketoday.core.type.AnnotationMetadata;

/**
 * Tests for {@link SimpleMethodMetadata} and
 * {@link SimpleMethodMetadataReadingVisitor}.
 *
 * @author Phillip Webb
 */
class SimpleMethodMetadataTests extends AbstractMethodMetadataTests {

	@Override
	protected AnnotationMetadata get(Class<?> source) {
		try {
			return new SimpleMetadataReaderFactory(
					source.getClassLoader()).getMetadataReader(
							source.getName()).getAnnotationMetadata();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

}
