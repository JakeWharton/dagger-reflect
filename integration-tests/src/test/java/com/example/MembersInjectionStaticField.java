package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MembersInjectionStaticField.Module1.class)
public interface MembersInjectionStaticField {
  void inject(Target instance);

  class Target {
    @Inject static String staticField;
  }

  @Module
  abstract class Module1 {
    @Provides
    static String one() {
      return "one";
    }
  }
}
