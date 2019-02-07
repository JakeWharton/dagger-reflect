package com.example;

import dagger.Component;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MembersInjectionAbstractMethod.Module1.class)
public interface MembersInjectionAbstractMethod {
  void inject(Target instance);

  abstract class Target {
    @Inject abstract void abstractMethod(String one);
  }

  abstract class Module1 {
    @Provides static String one() {
      return "one";
    }
  }
}
