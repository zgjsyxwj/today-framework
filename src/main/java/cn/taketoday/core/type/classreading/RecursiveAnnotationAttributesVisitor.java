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

import cn.taketoday.core.annotation.AnnotationAttributes;
import cn.taketoday.core.annotation.AnnotationUtils;
import cn.taketoday.core.bytecode.AnnotationVisitor;
import cn.taketoday.lang.Nullable;

/**
 * {@link AnnotationVisitor} to recursively visit annotation attributes.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 4.0
 * @deprecated this class and related classes in this
 * package have been replaced by {@link SimpleAnnotationMetadataReadingVisitor}
 * and related classes for internal use within the framework.
 */
@Deprecated
class RecursiveAnnotationAttributesVisitor extends AbstractRecursiveAnnotationVisitor {

  protected final String annotationType;

  public RecursiveAnnotationAttributesVisitor(
          String annotationType, AnnotationAttributes attributes, @Nullable ClassLoader classLoader) {

    super(classLoader, attributes);
    this.annotationType = annotationType;
  }

  @Override
  public void visitEnd() {
    AnnotationUtils.registerDefaultValues(this.attributes);
  }

}
