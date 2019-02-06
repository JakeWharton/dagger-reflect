package com.example;

import dagger.BindsInstance;
import dagger.Component;

@Component
public interface BuilderBindsInstanceOnParameter {
  String string();

  @Component.Builder
  interface Builder {
    Builder string(@BindsInstance String string);

    BuilderBindsInstanceOnParameter build();
  }
}
