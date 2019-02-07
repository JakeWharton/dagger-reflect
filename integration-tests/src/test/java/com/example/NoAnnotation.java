package com.example;

import dagger.Component;

public interface NoAnnotation {
  @Component.Builder
  interface Builder {
    NoAnnotation build();
  }
}
