package com.example;

import dagger.BindsInstance;
import dagger.Component;

@Component
public interface GenericComponentBuilderInterface {
  String value();

  @Component.Builder
  interface Builder extends GenericBuilder<GenericComponentBuilderInterface> {
    Builder bindString(@BindsInstance String instance);
  }

  public interface GenericBuilder<Component> {
    Component build();
  }
}
