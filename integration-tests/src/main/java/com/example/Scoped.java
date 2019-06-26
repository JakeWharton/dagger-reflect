package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Singleton
@Component(modules = Scoped.Module1.class)
interface Scoped {
  Object value();

  @Module
  abstract class Module1 {
    @Provides
    @Singleton
    static Object value() {
      return new Object();
    }
  }
}
