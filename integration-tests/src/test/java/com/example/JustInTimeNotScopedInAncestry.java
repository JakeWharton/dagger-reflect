package com.example;

import dagger.Component;
import dagger.Subcomponent;
import java.lang.annotation.Retention;
import javax.inject.Inject;
import javax.inject.Scope;
import javax.inject.Singleton;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Singleton
@Component
interface JustInTimeNotScopedInAncestry {
  ChildComponent child();

  @Child
  @Subcomponent
  interface ChildComponent {
    Thing thing();
  }

  @Unrelated
  final class Thing {
    @Inject Thing() {}
  }

  @Scope
  @Retention(RUNTIME)
  @interface Child {}

  @Scope
  @Retention(RUNTIME)
  @interface Unrelated {}
}
