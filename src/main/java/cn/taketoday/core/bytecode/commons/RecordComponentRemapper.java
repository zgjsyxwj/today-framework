// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.

package cn.taketoday.core.bytecode.commons;

import cn.taketoday.core.bytecode.AnnotationVisitor;
import cn.taketoday.core.bytecode.RecordComponentVisitor;
import cn.taketoday.core.bytecode.TypePath;
import cn.taketoday.lang.Nullable;

/**
 * A {@link RecordComponentVisitor} that remaps types with a {@link Remapper}.
 *
 * @author Remi Forax
 */
public class RecordComponentRemapper extends RecordComponentVisitor {

  /** The remapper used to remap the types in the visited field. */
  protected final Remapper remapper;

  /**
   * Constructs a new {@link RecordComponentRemapper}.
   *
   * @param recordComponentVisitor
   *         the record component visitor this remapper must delegate to.
   * @param remapper
   *         the remapper to use to remap the types in the visited record component.
   */
  public RecordComponentRemapper(
          final RecordComponentVisitor recordComponentVisitor, final Remapper remapper) {
    super(recordComponentVisitor);
    this.remapper = remapper;
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
    AnnotationVisitor annotationVisitor =
            super.visitAnnotation(remapper.mapDesc(descriptor), visible);
    return createAnnotationRemapper(descriptor, annotationVisitor);
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(
          final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
    AnnotationVisitor annotationVisitor =
            super.visitTypeAnnotation(typeRef, typePath, remapper.mapDesc(descriptor), visible);
    return createAnnotationRemapper(descriptor, annotationVisitor);
  }

  /**
   * Constructs a new remapper for annotations. The default implementation of this method returns a
   * new {@link AnnotationRemapper}.
   *
   * @param descriptor
   *         the descriptor sof the visited annotation.
   * @param annotationVisitor
   *         the AnnotationVisitor the remapper must delegate to.
   *
   * @return the newly created remapper.
   */
  @Nullable
  protected AnnotationVisitor createAnnotationRemapper(
          final String descriptor, @Nullable final AnnotationVisitor annotationVisitor) {
    if (annotationVisitor == null) {
      return null;
    }
    return new AnnotationRemapper(descriptor, annotationVisitor, remapper);
  }
}
