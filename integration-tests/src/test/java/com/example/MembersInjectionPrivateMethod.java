package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MembersInjectionPrivateMethod.Module1.class)
public interface MembersInjectionPrivateMethod {
  void inject(Target instance);

  class Target {
    @SuppressWarnings("unused") // Explicitly testing private.
    @Inject
    private void privateMethod(String one) {}
  }

  @Module
  abstract class Module1 {
    @Provides
    static String one() {
      return "one";
    }
  }
}
