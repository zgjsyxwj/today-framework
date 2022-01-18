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
package cn.taketoday.core.annotation;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import cn.taketoday.lang.Nullable;
import cn.taketoday.util.ConcurrentReferenceHashMap;

/**
 * Provides {@link AnnotationTypeMapping} information for a single source
 * annotation type. Performs a recursive breadth first crawl of all
 * meta-annotations to ultimately provide a quick way to map the attributes of
 * a root {@link Annotation}.
 *
 * <p>Supports convention based merging of meta-annotations as well as implicit
 * and explicit {@link AliasFor @AliasFor} aliases. Also provides information
 * about mirrored attributes.
 *
 * <p>This class is designed to be cached so that meta-annotations only need to
 * be searched once, regardless of how many times they are actually used.
 *
 * @author Phillip Webb
 * @see AnnotationTypeMapping
 * @since 4.0
 */
final class AnnotationTypeMappings {
  private static final IntrospectionFailureLogger failureLogger = IntrospectionFailureLogger.DEBUG;

  private static final ConcurrentReferenceHashMap<AnnotationFilter, Cache>
          standardRepeatablesCache = new ConcurrentReferenceHashMap<>();

  private static final ConcurrentReferenceHashMap<AnnotationFilter, Cache>
          noRepeatablesCache = new ConcurrentReferenceHashMap<>();

  private final AnnotationFilter filter;
  private final ArrayList<AnnotationTypeMapping> mappings;
  private final RepeatableContainers repeatableContainers;

  private AnnotationTypeMappings(
          RepeatableContainers repeatableContainers, AnnotationFilter filter, Class<? extends Annotation> annotationType) {

    this.filter = filter;
    this.mappings = new ArrayList<>();
    this.repeatableContainers = repeatableContainers;
    addAllMappings(annotationType);
    this.mappings.forEach(AnnotationTypeMapping::afterAllMappingsSet);
  }

  private void addAllMappings(Class<? extends Annotation> annotationType) {
    ArrayDeque<AnnotationTypeMapping> queue = new ArrayDeque<>();
    addIfPossible(queue, null, annotationType, null);
    while (!queue.isEmpty()) {
      AnnotationTypeMapping mapping = queue.removeFirst();
      this.mappings.add(mapping);
      addMetaAnnotationsToQueue(queue, mapping);
    }
  }

  private void addMetaAnnotationsToQueue(Deque<AnnotationTypeMapping> queue, AnnotationTypeMapping source) {
    Annotation[] metaAnnotations = AnnotationsScanner.getDeclaredAnnotations(source.getAnnotationType(), false);
    for (Annotation metaAnnotation : metaAnnotations) {
      if (isNotMappable(source, metaAnnotation)) {
        continue;
      }
      Annotation[] repeatedAnnotations = this.repeatableContainers.findRepeatedAnnotations(metaAnnotation);
      if (repeatedAnnotations != null) {
        for (Annotation repeatedAnnotation : repeatedAnnotations) {
          if (isNotMappable(source, repeatedAnnotation)) {
            continue;
          }
          addIfPossible(queue, source, repeatedAnnotation);
        }
      }
      else {
        addIfPossible(queue, source, metaAnnotation);
      }
    }
  }

  private void addIfPossible(Deque<AnnotationTypeMapping> queue, AnnotationTypeMapping source, Annotation ann) {
    addIfPossible(queue, source, ann.annotationType(), ann);
  }

  private void addIfPossible(Deque<AnnotationTypeMapping> queue, @Nullable AnnotationTypeMapping source,
                             Class<? extends Annotation> annotationType, @Nullable Annotation ann) {
    try {
      queue.addLast(new AnnotationTypeMapping(source, annotationType, ann));
    }
    catch (Exception ex) {
      AnnotationUtils.rethrowAnnotationConfigurationException(ex);
      if (failureLogger.isEnabled()) {
        failureLogger.log("Failed to introspect meta-annotation " + annotationType.getName(),
                (source != null ? source.getAnnotationType() : null), ex);
      }
    }
  }

  private boolean isNotMappable(AnnotationTypeMapping source, @Nullable Annotation metaAnnotation) {
    return (metaAnnotation == null || this.filter.matches(metaAnnotation)
            || AnnotationFilter.PLAIN.matches(source.getAnnotationType())
            || isAlreadyMapped(source, metaAnnotation)
    );
  }

  private boolean isAlreadyMapped(AnnotationTypeMapping source, Annotation metaAnnotation) {
    Class<? extends Annotation> annotationType = metaAnnotation.annotationType();
    AnnotationTypeMapping mapping = source;
    while (mapping != null) {
      if (mapping.getAnnotationType() == annotationType) {
        return true;
      }
      mapping = mapping.getSource();
    }
    return false;
  }

  /**
   * Get the total number of contained mappings.
   *
   * @return the total number of mappings
   */
  int size() {
    return this.mappings.size();
  }

  /**
   * Get an individual mapping from this instance.
   * <p>Index {@code 0} will always return the root mapping; higher indexes
   * will return meta-annotation mappings.
   *
   * @param index the index to return
   * @return the {@link AnnotationTypeMapping}
   * @throws IndexOutOfBoundsException if the index is out of range
   * (<tt>index &lt; 0 || index &gt;= size()</tt>)
   */
  AnnotationTypeMapping get(int index) {
    return this.mappings.get(index);
  }

  /**
   * Create {@link AnnotationTypeMappings} for the specified annotation type.
   *
   * @param annotationType the source annotation type
   * @return type mappings for the annotation type
   */
  static AnnotationTypeMappings forAnnotationType(Class<? extends Annotation> annotationType) {
    return forAnnotationType(annotationType, AnnotationFilter.PLAIN);
  }

  /**
   * Create {@link AnnotationTypeMappings} for the specified annotation type.
   *
   * @param annotationType the source annotation type
   * @param annotationFilter the annotation filter used to limit which
   * annotations are considered
   * @return type mappings for the annotation type
   */
  static AnnotationTypeMappings forAnnotationType(
          Class<? extends Annotation> annotationType, AnnotationFilter annotationFilter) {

    return forAnnotationType(annotationType, RepeatableContainers.standard(), annotationFilter);
  }

  /**
   * Create {@link AnnotationTypeMappings} for the specified annotation type.
   *
   * @param annotationType the source annotation type
   * @param repeatableContainers the repeatable containers that may be used by
   * the meta-annotations
   * @param annotationFilter the annotation filter used to limit which
   * annotations are considered
   * @return type mappings for the annotation type
   */
  static AnnotationTypeMappings forAnnotationType(
          Class<? extends Annotation> annotationType, RepeatableContainers repeatableContainers, AnnotationFilter annotationFilter) {

    if (repeatableContainers == RepeatableContainers.standard()) {
      return standardRepeatablesCache.computeIfAbsent(
              annotationFilter, key -> new Cache(repeatableContainers, key)).get(annotationType);
    }
    if (repeatableContainers == RepeatableContainers.none()) {
      return noRepeatablesCache.computeIfAbsent(
              annotationFilter, key -> new Cache(repeatableContainers, key)).get(annotationType);
    }
    return new AnnotationTypeMappings(repeatableContainers, annotationFilter, annotationType);
  }

  static void clearCache() {
    standardRepeatablesCache.clear();
    noRepeatablesCache.clear();
  }

  /**
   * Cache created per {@link AnnotationFilter}.
   */
  private static class Cache {
    private final AnnotationFilter filter;
    private final RepeatableContainers repeatableContainers;
    private final ConcurrentReferenceHashMap<Class<? extends Annotation>, AnnotationTypeMappings> mappings;

    /**
     * Create a cache instance with the specified filter.
     *
     * @param filter the annotation filter
     */
    Cache(RepeatableContainers repeatableContainers, AnnotationFilter filter) {
      this.repeatableContainers = repeatableContainers;
      this.filter = filter;
      this.mappings = new ConcurrentReferenceHashMap<>();
    }

    /**
     * Get or create {@link AnnotationTypeMappings} for the specified annotation type.
     *
     * @param annotationType the annotation type
     * @return a new or existing {@link AnnotationTypeMappings} instance
     */
    AnnotationTypeMappings get(Class<? extends Annotation> annotationType) {
      return this.mappings.computeIfAbsent(annotationType, this::createMappings);
    }

    AnnotationTypeMappings createMappings(Class<? extends Annotation> annotationType) {
      return new AnnotationTypeMappings(this.repeatableContainers, this.filter, annotationType);
    }
  }

}
