package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

@Component(modules = SubcomponentProvision.Module1.class)
public interface SubcomponentProvision {
  Nested nested();

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
    abstract class Module2 {
      @Provides
      static Long two() {
        return 2L;
      }
    }
  }
}
