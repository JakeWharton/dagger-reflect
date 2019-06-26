package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = FactoryExplicitModules.Module1.class)
public interface FactoryExplicitModules {
  String string();

  @Module
  final class Module1 {
    private final String value;

    public Module1(String value) {
      this.value = value;
    }

    @Provides
    String string() {
      return value;
    }
  }

  @Component.Factory
  interface Factory {
    FactoryExplicitModules create(Module1 module1);
  }
}
