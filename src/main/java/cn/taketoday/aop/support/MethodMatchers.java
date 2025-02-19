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

package cn.taketoday.aop.support;

import org.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;
import java.lang.reflect.Method;

import cn.taketoday.aop.ClassFilter;
import cn.taketoday.aop.IntroductionAwareMethodMatcher;
import cn.taketoday.aop.MethodMatcher;
import cn.taketoday.lang.Assert;

/**
 * Static utility methods for composing {@link MethodMatcher MethodMatchers}.
 *
 * <p>A MethodMatcher may be evaluated statically (based on method and target
 * class) or need further evaluation dynamically (based on arguments at the
 * time of method invocation).
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author TODAY 2021/2/1 18:19
 * @see ClassFilters
 * @see Pointcuts
 * @since 3.0
 */
public abstract class MethodMatchers {

  /**
   * Match all methods that <i>either</i> (or both) of the given MethodMatchers matches.
   *
   * @param mm1 the first MethodMatcher
   * @param mm2 the second MethodMatcher
   * @return a distinct MethodMatcher that matches all methods that either
   * of the given MethodMatchers matches
   */
  public static MethodMatcher union(MethodMatcher mm1, MethodMatcher mm2) {
    return (mm1 instanceof IntroductionAwareMethodMatcher || mm2 instanceof IntroductionAwareMethodMatcher
            ? new UnionIntroductionAwareMethodMatcher(mm1, mm2)
            : new UnionMethodMatcher(mm1, mm2));
  }

  /**
   * Match all methods that <i>either</i> (or both) of the given MethodMatchers matches.
   *
   * @param mm1 the first MethodMatcher
   * @param cf1 the corresponding ClassFilter for the first MethodMatcher
   * @param mm2 the second MethodMatcher
   * @param cf2 the corresponding ClassFilter for the second MethodMatcher
   * @return a distinct MethodMatcher that matches all methods that either
   * of the given MethodMatchers matches
   */
  public static MethodMatcher union(MethodMatcher mm1, ClassFilter cf1, MethodMatcher mm2, ClassFilter cf2) {
    return (mm1 instanceof IntroductionAwareMethodMatcher || mm2 instanceof IntroductionAwareMethodMatcher ?
            new ClassFilterAwareUnionIntroductionAwareMethodMatcher(mm1, cf1, mm2, cf2) :
            new ClassFilterAwareUnionMethodMatcher(mm1, cf1, mm2, cf2));
  }

  /**
   * Match all methods that <i>both</i> of the given MethodMatchers match.
   *
   * @param mm1 the first MethodMatcher
   * @param mm2 the second MethodMatcher
   * @return a distinct MethodMatcher that matches all methods that both
   * of the given MethodMatchers match
   */
  public static MethodMatcher intersection(MethodMatcher mm1, MethodMatcher mm2) {
    return (mm1 instanceof IntroductionAwareMethodMatcher || mm2 instanceof IntroductionAwareMethodMatcher ?
            new IntersectionIntroductionAwareMethodMatcher(mm1, mm2) : new IntersectionMethodMatcher(mm1, mm2));
  }

  /**
   * Apply the given MethodMatcher to the given Method, supporting an
   * {@link IntroductionAwareMethodMatcher}
   * (if applicable).
   *
   * @param mm the MethodMatcher to apply (may be an IntroductionAwareMethodMatcher)
   * @param method the candidate method
   * @param targetClass the target class
   * @param hasIntroductions {@code true} if the object on whose behalf we are
   * asking is the subject on one or more introductions; {@code false} otherwise
   * @return whether or not this method matches statically
   */
  public static boolean matches(MethodMatcher mm, Method method, Class<?> targetClass, boolean hasIntroductions) {
    Assert.notNull(mm, "MethodMatcher must not be null");
    return (mm instanceof IntroductionAwareMethodMatcher ?
            ((IntroductionAwareMethodMatcher) mm).matches(method, targetClass, hasIntroductions)
                                                         : mm.matches(method, targetClass));
  }

  /**
   * MethodMatcher implementation for a union of two given MethodMatchers.
   */
  static class UnionMethodMatcher implements MethodMatcher, Serializable {
    private static final long serialVersionUID = 1L;

    protected final MethodMatcher mm1;
    protected final MethodMatcher mm2;

    public UnionMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
      Assert.notNull(mm1, "First MethodMatcher must not be null");
      Assert.notNull(mm2, "Second MethodMatcher must not be null");
      this.mm1 = mm1;
      this.mm2 = mm2;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
      return (matchesClass1(targetClass) && this.mm1.matches(method, targetClass)) ||
              (matchesClass2(targetClass) && this.mm2.matches(method, targetClass));
    }

    protected boolean matchesClass1(Class<?> targetClass) {
      return true;
    }

    protected boolean matchesClass2(Class<?> targetClass) {
      return true;
    }

    @Override
    public boolean isRuntime() {
      return this.mm1.isRuntime() || this.mm2.isRuntime();
    }

    @Override
    public boolean matches(MethodInvocation invocation) {
      return this.mm1.matches(invocation) || this.mm2.matches(invocation);
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof UnionMethodMatcher)) {
        return false;
      }
      UnionMethodMatcher that = (UnionMethodMatcher) other;
      return (this.mm1.equals(that.mm1) && this.mm2.equals(that.mm2));
    }

    @Override
    public int hashCode() {
      return 37 * this.mm1.hashCode() + this.mm2.hashCode();
    }

    @Override
    public String toString() {
      return getClass().getName() + ": " + this.mm1 + ", " + this.mm2;
    }
  }

  /**
   * MethodMatcher implementation for a union of two given MethodMatchers
   * of which at least one is an IntroductionAwareMethodMatcher.
   */
  static class UnionIntroductionAwareMethodMatcher
          extends UnionMethodMatcher implements IntroductionAwareMethodMatcher {
    private static final long serialVersionUID = 1L;

    public UnionIntroductionAwareMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
      super(mm1, mm2);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
      return (matchesClass1(targetClass) && MethodMatchers.matches(this.mm1, method, targetClass, hasIntroductions)) ||
              (matchesClass2(targetClass) && MethodMatchers.matches(this.mm2, method, targetClass, hasIntroductions));
    }
  }

  /**
   * MethodMatcher implementation for a union of two given MethodMatchers,
   * supporting an associated ClassFilter per MethodMatcher.
   */
  static class ClassFilterAwareUnionMethodMatcher extends UnionMethodMatcher {
    private static final long serialVersionUID = 1L;

    private final ClassFilter cf1;
    private final ClassFilter cf2;

    public ClassFilterAwareUnionMethodMatcher(MethodMatcher mm1, ClassFilter cf1, MethodMatcher mm2, ClassFilter cf2) {
      super(mm1, mm2);
      this.cf1 = cf1;
      this.cf2 = cf2;
    }

    @Override
    protected boolean matchesClass1(Class<?> targetClass) {
      return this.cf1.matches(targetClass);
    }

    @Override
    protected boolean matchesClass2(Class<?> targetClass) {
      return this.cf2.matches(targetClass);
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!super.equals(other)) {
        return false;
      }
      ClassFilter otherCf1 = ClassFilter.TRUE;
      ClassFilter otherCf2 = ClassFilter.TRUE;
      if (other instanceof ClassFilterAwareUnionMethodMatcher) {
        ClassFilterAwareUnionMethodMatcher cfa = (ClassFilterAwareUnionMethodMatcher) other;
        otherCf1 = cfa.cf1;
        otherCf2 = cfa.cf2;
      }
      return (this.cf1.equals(otherCf1) && this.cf2.equals(otherCf2));
    }

    @Override
    public int hashCode() {
      // Allow for matching with regular UnionMethodMatcher by providing same hash...
      return super.hashCode();
    }

    @Override
    public String toString() {
      return getClass().getName() + ": " + this.cf1 + ", " + this.mm1 + ", " + this.cf2 + ", " + this.mm2;
    }
  }

  /**
   * MethodMatcher implementation for a union of two given MethodMatchers
   * of which at least one is an IntroductionAwareMethodMatcher,
   * supporting an associated ClassFilter per MethodMatcher.
   */
  static class ClassFilterAwareUnionIntroductionAwareMethodMatcher
          extends ClassFilterAwareUnionMethodMatcher implements IntroductionAwareMethodMatcher {
    private static final long serialVersionUID = 1L;

    public ClassFilterAwareUnionIntroductionAwareMethodMatcher(
            MethodMatcher mm1, ClassFilter cf1, MethodMatcher mm2, ClassFilter cf2) {

      super(mm1, cf1, mm2, cf2);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
      return (matchesClass1(targetClass) && MethodMatchers.matches(this.mm1, method, targetClass, hasIntroductions)) ||
              (matchesClass2(targetClass) && MethodMatchers.matches(this.mm2, method, targetClass, hasIntroductions));
    }
  }

  /**
   * MethodMatcher implementation for an intersection of two given MethodMatchers.
   */
  static class IntersectionMethodMatcher implements MethodMatcher, Serializable {
    private static final long serialVersionUID = 1L;

    protected final MethodMatcher mm1;
    protected final MethodMatcher mm2;

    public IntersectionMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
      Assert.notNull(mm1, "First MethodMatcher must not be null");
      Assert.notNull(mm2, "Second MethodMatcher must not be null");
      this.mm1 = mm1;
      this.mm2 = mm2;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
      return (this.mm1.matches(method, targetClass) && this.mm2.matches(method, targetClass));
    }

    @Override
    public boolean isRuntime() {
      return (this.mm1.isRuntime() || this.mm2.isRuntime());
    }

    @Override
    public boolean matches(MethodInvocation invocation) {
      final Method method = invocation.getMethod();
      final Class<?> targetClass = invocation.getThis().getClass();
      boolean aMatches = (this.mm1.isRuntime() ? this.mm1.matches(invocation) : this.mm1.matches(method, targetClass));
      boolean bMatches = (this.mm2.isRuntime() ? this.mm2.matches(invocation) : this.mm2.matches(method, targetClass));
      return aMatches && bMatches;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof IntersectionMethodMatcher)) {
        return false;
      }
      IntersectionMethodMatcher that = (IntersectionMethodMatcher) other;
      return (this.mm1.equals(that.mm1) && this.mm2.equals(that.mm2));
    }

    @Override
    public int hashCode() {
      return 37 * this.mm1.hashCode() + this.mm2.hashCode();
    }

    @Override
    public String toString() {
      return getClass().getName() + ": " + this.mm1 + ", " + this.mm2;
    }
  }

  /**
   * MethodMatcher implementation for an intersection of two given MethodMatchers
   * of which at least one is an IntroductionAwareMethodMatcher.
   */
  static class IntersectionIntroductionAwareMethodMatcher
          extends IntersectionMethodMatcher implements IntroductionAwareMethodMatcher {
    private static final long serialVersionUID = 1L;

    public IntersectionIntroductionAwareMethodMatcher(MethodMatcher mm1, MethodMatcher mm2) {
      super(mm1, mm2);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
      return (MethodMatchers.matches(this.mm1, method, targetClass, hasIntroductions) &&
              MethodMatchers.matches(this.mm2, method, targetClass, hasIntroductions));
    }

  }

}

