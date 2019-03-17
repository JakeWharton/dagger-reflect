package com.example;

import dagger.BindsInstance;
import dagger.Component;

@Component
public interface FactoryBindsInstance {
  String string();

  @Component.Factory
  interface Factory {
    FactoryBindsInstance create(@BindsInstance String one);
  }
}
