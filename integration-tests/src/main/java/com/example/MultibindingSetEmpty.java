package com.example;

import dagger.Component;
import dagger.Module;
import dagger.multibindings.Multibinds;
import java.util.Set;

@Component(modules = MultibindingSetEmpty.Module1.class)
interface MultibindingSetEmpty {
  Set<String> values();

  @Module
  abstract class Module1 {
    @Multibinds
    abstract Set<String> empty();
  }
}
