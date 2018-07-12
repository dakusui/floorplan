package com.github.dakusui.floorplan.examples.bookstore;

import com.github.dakusui.floorplan.examples.bookstore.floorplan.BookstoreProfile;
import com.github.dakusui.floorplan.examples.bookstore.tdescs.SmokeTestDesc;
import com.github.dakusui.floorplan.tdesc.TestSuiteDescriptor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class FloorPlanRunner extends Parameterized {

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @param klass A target test class
   */
  public FloorPlanRunner(Class<?> klass) throws Throwable {
    super(klass);
  }

  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return new TestClass(testClass) {
      private final TestSuiteDescriptor descriptor = createTestSuiteDescriptor();

      @Override
      public List<FrameworkMethod> getAnnotatedMethods(Class<? extends Annotation> annotationClass) {
        if (annotationsForMethodsShouldHaveOnlyParameterForDescriptor().contains(annotationClass))
          return super.getAnnotatedMethods(annotationClass).stream().map(new Function<FrameworkMethod, FrameworkMethod>() {
            @Override
            public FrameworkMethod apply(FrameworkMethod frameworkMethod) {
              return new FrameworkMethod(frameworkMethod.getMethod()) {
                @Override
                public Object invokeExplosively(Object target, Object... args) throws Throwable {
                  return super.invokeExplosively(target, descriptor);
                }
              };
            }
          }).collect(toList());
        return super.getAnnotatedMethods(annotationClass);
      }
    };
  }

  private TestSuiteDescriptor createTestSuiteDescriptor() {
    return new SmokeTestDesc().create(new BookstoreProfile());
  }

  @Override
  protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation,
      boolean isStatic, List<Throwable> errors) {
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);

    for (FrameworkMethod eachTestMethod : methods) {
      if (annotationsForMethodsShouldHaveOnlyParameterForDescriptor().contains(annotation))
        eachTestMethod.validatePublicVoid(isStatic, errors);
      else
        eachTestMethod.validatePublicVoidNoArg(isStatic, errors);
    }
  }

  private Collection<Class<? extends Annotation>> annotationsForMethodsShouldHaveOnlyParameterForDescriptor() {
    return asList(Parameterized.Parameters.class, BeforeClass.class, AfterClass.class);
  }
}

