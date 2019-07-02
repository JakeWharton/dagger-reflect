package com.example;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

final class IntegrationTestRule implements TestRule {
  private final Backend backend;

  IntegrationTestRule(Backend backend) {
    this.backend = backend;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    boolean ignoreCodegen = description.getAnnotation(IgnoreCodegen.class) != null;
    if (ignoreCodegen && backend == Backend.CODEGEN) {
      return new Statement() {
        @Override
        public void evaluate() {
          throw new AssumptionViolatedException("Ignored in code gen backend");
        }
      };
    }

    ReflectBug reflectBug = description.getAnnotation(ReflectBug.class);
    if (reflectBug != null && backend == Backend.REFLECT) {
      return new Statement() {
        @Override
        public void evaluate() {
          try {
            base.evaluate();
          } catch (Throwable t) {
            String message = "Known issue in reflection backend";
            if (!reflectBug.value().isEmpty()) {
              message += ": " + reflectBug.value();
            }
            throw new AssumptionViolatedException(message, t);
          }
          throw new AssertionError("Test succeeded when expected to fail. Remove @ReflectBug?");
        }
      };
    }

    return base;
  }
}
