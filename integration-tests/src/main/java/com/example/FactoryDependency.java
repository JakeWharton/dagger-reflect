package com.example;

import dagger.Component;

@Component(dependencies = FactoryDependency.Other.class)
public interface FactoryDependency {
  String string();

  class Other {
    private final String string;

    Other(String string) {
      this.string = string;
    }

    String string() {
      return string;
    }
  }

  @Component.Factory
  interface Factory {
    FactoryDependency create(Other other);
  }
}
