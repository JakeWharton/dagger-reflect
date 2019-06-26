package com.example;

import dagger.BindsInstance;
import dagger.Component;
import org.jetbrains.annotations.Nullable;

@Component
public interface FactoryBindsInstanceNull {
  @Nullable
  String string();

  @Component.Factory
  interface Factory {
    FactoryBindsInstanceNull create(@BindsInstance @Nullable String one);
  }
}
