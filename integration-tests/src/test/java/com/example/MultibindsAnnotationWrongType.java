package com.example;

import dagger.Component;
import dagger.Module;
import dagger.multibindings.Multibinds;

@Component(modules = MultibindsAnnotationWrongType.Module1.class)
interface MultibindsAnnotationWrongType {
  @Module
  abstract class Module1 {
    @Multibinds
    abstract String empty();
  }
}
