package com.example;

import dagger.Component;

@Component
public abstract class AbstractComponent {
  @Component.Builder
  interface Builder {
    AbstractComponent build();
  }
}
