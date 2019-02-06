package com.example;

import dagger.BindsInstance;
import dagger.Component;

@Component
public interface BuilderBindsInstanceOnParameterAndMethod {
  String string();

  @Component.Builder
  interface Builder {
    @BindsInstance
    Builder string(@BindsInstance String string);

    BuilderBindsInstanceOnParameterAndMethod build();
  }
}
