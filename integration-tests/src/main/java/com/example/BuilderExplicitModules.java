package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = BuilderExplicitModules.Module1.class)
public interface BuilderExplicitModules {
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

  @Component.Builder
  interface Builder {
    Builder module1(Module1 module1);

    BuilderExplicitModules build();
  }
}
