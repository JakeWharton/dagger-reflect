package com.example;

import dagger.BindsInstance;
import dagger.Component;
import org.jetbrains.annotations.Nullable;

@Component
public interface BuilderBindsInstanceOnParameterNull {
  @Nullable
  String string();

  @Component.Builder
  interface Builder {
    Builder string(@BindsInstance @Nullable String string);

    BuilderBindsInstanceOnParameterNull build();
  }
}
