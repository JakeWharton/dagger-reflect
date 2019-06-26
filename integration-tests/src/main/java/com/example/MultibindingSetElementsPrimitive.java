package com.example;

import static java.util.Collections.singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;
import java.util.Set;

@Component(modules = MultibindingSetElementsPrimitive.Module1.class)
interface MultibindingSetElementsPrimitive {
  Set<Long> values();

  @Module
  abstract class Module1 {
    @Provides
    @ElementsIntoSet
    static Set<Long> one() {
      return singleton(1L);
    }

    @Provides
    @IntoSet
    static long two() {
      return 2L;
    }
  }
}
