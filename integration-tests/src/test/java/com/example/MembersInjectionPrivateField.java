package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MembersInjectionPrivateField.Module1.class)
public interface MembersInjectionPrivateField {
  void inject(Target instance);

  class Target {
    @SuppressWarnings("unused") // Explicitly testing private.
    @Inject
    private String privateField;
  }

  @Module
  abstract class Module1 {
    @Provides
    static String one() {
      return "one";
    }
  }
}
