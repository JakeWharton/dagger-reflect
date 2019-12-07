package com.example;

import dagger.Component;
import dagger.Module;
import dagger.multibindings.Multibinds;
import java.util.Map;

@Component(modules = MultibindingMapEmpty.Module1.class)
interface MultibindingMapEmpty {
  Map<String, String> values();

  @Module
  abstract class Module1 {
    @Multibinds
    abstract Map<String, String> empty();
  }
}
