package com.example;

import dagger.BindsInstance;
import dagger.Component;

@Component
public interface BuilderBindsInstance {
  String string();

  @Component.Builder
  interface Builder {
    @BindsInstance
    Builder string(String one);

    BuilderBindsInstance build();
  }
}
