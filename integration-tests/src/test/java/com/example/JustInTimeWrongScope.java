package com.example;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import dagger.Component;
import java.lang.annotation.Retention;
import javax.inject.Inject;
import javax.inject.Scope;
import javax.inject.Singleton;

@Singleton
@Component
interface JustInTimeWrongScope {
  Thing thing();

  @Unrelated
  final class Thing {
    @Inject
    Thing() {}
  }

  @Scope
  @Retention(RUNTIME)
  @interface Unrelated {}
}
