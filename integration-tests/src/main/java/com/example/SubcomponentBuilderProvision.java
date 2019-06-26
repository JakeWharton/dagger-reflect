package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

@Component(modules = SubcomponentBuilderProvision.Module1.class)
public interface SubcomponentBuilderProvision {
  Nested.Builder nestedBuilder();

  @Module
  abstract class Module1 {
    @Provides
    static String one() {
      return "one";
    }
  }

  @Subcomponent(modules = Nested.Module2.class)
  interface Nested {
    String one();

    Long two();

    @Subcomponent.Builder
    interface Builder {
      Builder module2(Module2 module);

      Nested build();
    }

    @Module
    final class Module2 {
      private final long two;

      Module2(long two) {
        this.two = two;
      }

      @Provides
      Long two() {
        return two;
      }
    }
  }
}
