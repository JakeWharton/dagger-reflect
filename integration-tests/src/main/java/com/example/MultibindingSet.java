package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.util.Set;

@Component(modules = MultibindingSet.Module1.class)
interface MultibindingSet {
  Set<String> values();

  @Module
  abstract class Module1 {
    @Provides
    @IntoSet
    static String one() {
      return "one";
    }

    @Provides
    @IntoSet
    static String two() {
      return "two";
    }
  }
}
