package com.example;

import dagger.Component;

@Component(dependencies = BuilderDependency.Other.class)
public interface BuilderDependency {
  String string();

  class Other {
    private final String string;

    public Other(String string) {
      this.string = string;
    }

    String string() {
      return string;
    }
  }

  @Component.Builder
  interface Builder {
    Builder other(Other other);

    BuilderDependency build();
  }
}
