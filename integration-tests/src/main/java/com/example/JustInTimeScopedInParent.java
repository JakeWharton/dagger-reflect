package com.example;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import dagger.Component;
import dagger.Subcomponent;
import java.lang.annotation.Retention;
import javax.inject.Inject;
import javax.inject.Scope;
import javax.inject.Singleton;

@Singleton
@Component
interface JustInTimeScopedInParent {
  ChildComponent child();

  @Child
  @Subcomponent
  interface ChildComponent {
    Thing thing();
  }

  @Singleton
  final class Thing {
    @Inject
    Thing() {}
  }

  @Scope
  @Retention(RUNTIME)
  @interface Child {}
}
