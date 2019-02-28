package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.lang.annotation.Retention;
import javax.inject.Scope;
import javax.inject.Singleton;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Singleton
@Component(modules = ScopedWrong.Module1.class)
interface ScopedWrong {
  Object value();

  @Module
  abstract class Module1 {
    @Provides @Unrelated static Object value() {
      return new Object();
    }
  }

  @Scope
  @Retention(RUNTIME)
  @interface Unrelated {}
}
