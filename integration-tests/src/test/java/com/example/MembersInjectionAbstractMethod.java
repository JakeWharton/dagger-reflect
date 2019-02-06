package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MembersInjectionAbstractMethod.Module1.class)
public interface MembersInjectionAbstractMethod {
  void inject(Target instance);

  abstract class Target {
    @SuppressWarnings("JavaxInjectOnAbstractMethod") // Known incorrect behavior under test.
    @Inject
    abstract void abstractMethod(String one);
  }

  @Module
  abstract class Module1 {
    @Provides
    static String one() {
      return "one";
    }
  }
}
