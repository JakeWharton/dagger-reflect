package com.example;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.lang.annotation.Retention;
import javax.inject.Scope;
import javax.inject.Singleton;

@Singleton
@ScopedWithMultipleAnnotations.Onesie
@Component(
    modules = {
      ScopedWithMultipleAnnotations.Module1.class,
      ScopedWithMultipleAnnotations.Module2.class,
    })
interface ScopedWithMultipleAnnotations {
  Object value();

  Runnable runnable();

  @Scope
  @Retention(RUNTIME)
  @interface Onesie {}

  @Module
  abstract class Module1 {
    @Provides
    @Singleton
    static Object value() {
      return new Object();
    }
  }

  @Module
  abstract class Module2 {
    @Provides
    @Onesie
    static Runnable value() {
      return new Runnable() {
        @Override
        public void run() {}
      };
    }
  }
}
