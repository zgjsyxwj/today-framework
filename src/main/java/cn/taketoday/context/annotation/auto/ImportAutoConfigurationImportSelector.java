/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2022 All Rights Reserved.
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

package cn.taketoday.context.annotation.auto;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.taketoday.core.MultiValueMap;
import cn.taketoday.core.annotation.AnnotatedElementUtils;
import cn.taketoday.core.annotation.AnnotationAttributes;
import cn.taketoday.core.annotation.AnnotationUtils;
import cn.taketoday.core.type.AnnotationMetadata;
import cn.taketoday.lang.TodayStrategies;
import cn.taketoday.util.ClassUtils;
import cn.taketoday.util.CollectionUtils;
import cn.taketoday.util.ObjectUtils;

/**
 * Variant of {@link AutoConfigurationImportSelector} for
 * {@link ImportAutoConfiguration @ImportAutoConfiguration}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/2/1 23:56
 */
class ImportAutoConfigurationImportSelector extends AutoConfigurationImportSelector implements DeterminableImports {

  @Override
  public Set<Object> determineImports(AnnotationMetadata metadata) {
    List<String> candidateConfigurations = getCandidateConfigurations(metadata, null);
    Set<String> result = new LinkedHashSet<>(candidateConfigurations);
    result.removeAll(getExclusions(metadata, null));
    return Collections.unmodifiableSet(result);
  }

  @Override
  protected AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
    return null;
  }

  @Override
  protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
    ArrayList<String> candidates = new ArrayList<>();
    Map<Class<?>, List<Annotation>> annotations = getAnnotations(metadata);

    for (Map.Entry<Class<?>, List<Annotation>> entry : annotations.entrySet()) {
      Class<?> source = entry.getKey();
      List<Annotation> sourceAnnotations = entry.getValue();
      collectCandidateConfigurations(source, sourceAnnotations, candidates);
    }
    return candidates;
  }

  private void collectCandidateConfigurations(
          Class<?> source, List<Annotation> annotations, List<String> candidates) {
    for (Annotation annotation : annotations) {
      candidates.addAll(getConfigurationsForAnnotation(source, annotation));
    }
  }

  private Collection<String> getConfigurationsForAnnotation(Class<?> source, Annotation annotation) {
    String[] classes = (String[]) AnnotationUtils.getAnnotationAttributes(annotation, true).get("classes");
    if (classes.length > 0) {
      return Arrays.asList(classes);
    }
    return getStrategiesNames(source);
  }

  protected Collection<String> getStrategiesNames(Class<?> source) {
    return TodayStrategies.getStrategies(source.getName(), getBeanClassLoader());
  }

  @Override
  protected Set<String> getExclusions(AnnotationMetadata metadata, AnnotationAttributes attributes) {
    LinkedHashSet<String> exclusions = new LinkedHashSet<>();
    Class<?> source = ClassUtils.resolveClassName(metadata.getClassName(), null);

    // TODO 优化内存
    AnnotationAttributes merged = AnnotatedElementUtils.getMergedAnnotationAttributes(
            source, ImportAutoConfiguration.class);
    {
      Class<?>[] exclude = merged != null ? merged.getClassArray("exclude") : null;
      if (exclude != null) {
        for (Class<?> excludeClass : exclude) {
          exclusions.add(excludeClass.getName());
        }
      }
    }

    for (List<Annotation> annotations : getAnnotations(metadata).values()) {
      for (Annotation annotation : annotations) {
        String[] exclude = (String[]) AnnotationUtils.getAnnotationAttributes(annotation, true).get("exclude");
        if (ObjectUtils.isNotEmpty(exclude)) {
          CollectionUtils.addAll(exclusions, exclude);
        }
      }
    }
    exclusions.addAll(getExcludeAutoConfigurationsProperty());
    return exclusions;
  }

  protected final Map<Class<?>, List<Annotation>> getAnnotations(AnnotationMetadata metadata) {
    MultiValueMap<Class<?>, Annotation> annotations = MultiValueMap.fromLinkedHashMap();
    Class<?> source = ClassUtils.resolveClassName(metadata.getClassName(), null);
    collectAnnotations(source, annotations, new HashSet<>());
    return Collections.unmodifiableMap(annotations);
  }

  private void collectAnnotations(
          Class<?> source, MultiValueMap<Class<?>, Annotation> annotations, HashSet<Class<?>> seen) {
    if (source != null && seen.add(source)) {
      for (Annotation annotation : source.getDeclaredAnnotations()) {
        if (!AnnotationUtils.isInJavaLangAnnotationPackage(annotation)) {
          if (ImportAutoConfiguration.class == annotation.annotationType()) {
            annotations.add(source, annotation);
          }
          collectAnnotations(annotation.annotationType(), annotations, seen);
        }
      }
      collectAnnotations(source.getSuperclass(), annotations, seen);
    }
  }

  @Override
  public int getOrder() {
    return super.getOrder() - 1;
  }

  @Override
  protected void handleInvalidExcludes(List<String> invalidExcludes) {
    // Ignore for test
  }

}

