package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

@Component(modules = SubcomponentFactoryMethod.Module1.class)
public interface SubcomponentFactoryMethod {
  Nested createNested(Nested.Module2 module);

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
