package com.example;

import dagger.Component;

@Component
abstract class AbstractComponent {
  @Component.Builder
  interface Builder {
    AbstractComponent build();
  }
}
