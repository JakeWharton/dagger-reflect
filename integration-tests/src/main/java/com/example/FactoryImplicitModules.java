package com.example;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = FactoryImplicitModules.Module1.class)
public interface FactoryImplicitModules {
  String string();

  @Module
  abstract class Module1 {
    @Provides
    static String string(Long value) {
      return Long.toString(value);
    }
  }

  @Component.Factory
  interface Factory {
    FactoryImplicitModules create(@BindsInstance Long value);
  }
}
