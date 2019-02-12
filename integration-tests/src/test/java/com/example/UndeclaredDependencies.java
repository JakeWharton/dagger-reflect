package com.example;

import dagger.Component;

@Component
public interface UndeclaredDependencies {
  @Component.Builder
  interface Builder {
    Builder dep(String module);
    UndeclaredDependencies build();
  }
}
