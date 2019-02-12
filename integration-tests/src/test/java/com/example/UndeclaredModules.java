package com.example;

import dagger.Component;
import dagger.Module;

@Component
public interface UndeclaredModules {
  @Component.Builder
  interface Builder {
    Builder module(Module1 module);
    UndeclaredModules build();
  }

  @Module
  class Module1 {}
}
