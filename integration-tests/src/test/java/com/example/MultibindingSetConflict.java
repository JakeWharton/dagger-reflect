package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;
import java.util.Collections;
import java.util.Set;

@Component(modules = MultibindingSetConflict.Module1.class)
interface MultibindingSetConflict {
  Set<String> values();

  @Module
  abstract class Module1 {
    @Provides
    @IntoSet
    static String one() {
      return "one";
    }

    @Provides
    @ElementsIntoSet
    static Set<String> two() {
      return Collections.singleton("two");
    }

    @Provides
    static Set<String> explicit() {
      return Collections.emptySet();
    }
  }
}
