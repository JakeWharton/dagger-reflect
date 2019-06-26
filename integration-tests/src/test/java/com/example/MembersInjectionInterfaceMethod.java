package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MembersInjectionInterfaceMethod.Module1.class)
public interface MembersInjectionInterfaceMethod {
  void inject(Target instance);

  interface Target {
    @SuppressWarnings("JavaxInjectOnAbstractMethod") // Known incorrect behavior under test.
    @Inject
    void interfaceMethod(String one);
  }

  @Module
  abstract class Module1 {
    @Provides
    static String one() {
      return "one";
    }
  }
}
