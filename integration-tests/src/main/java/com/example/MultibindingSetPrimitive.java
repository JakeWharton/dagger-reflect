package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.util.Set;

@Component(modules = MultibindingSetPrimitive.Module1.class)
interface MultibindingSetPrimitive {
  Set<Long> values();

  @Module
  abstract class Module1 {
    @Provides
    @IntoSet
    static long one() {
      return 1L;
    }

    @Provides
    @IntoSet
    static long two() {
      return 2L;
    }
  }
}
