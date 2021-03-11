package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import dagger.multibindings.IntoSet;
import java.util.Set;
import javax.inject.Inject;

@Component(modules = SubcomponentMultibinding.ProvidesOne.class)
public interface SubcomponentMultibinding {
  InjectsMultibindings injectsMultibindings();

  Nested nested();

  class InjectsMultibindings {
    public final Set<String> multibindings;

    @Inject
    protected InjectsMultibindings(Set<String> strings) {
      this.multibindings = strings;
    }
  }

  @Module
  abstract class ProvidesOne {
    @Provides
    @IntoSet
    static String one() {
      return "one";
    }
  }

  @Module
  abstract class ProvidesTwo {
    @Provides
    @IntoSet
    static String two() {
      return "two";
    }
  }

  @Subcomponent(modules = ProvidesTwo.class)
  interface Nested {
    InjectsMultibindings injectsMultibindings();
  }
}
