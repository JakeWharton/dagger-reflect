package com.example;

import dagger.Component;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MembersInjectionPrivateField.Module1.class)
public interface MembersInjectionPrivateField {
  void inject(Target instance);

  class Target {
    @Inject private String privateField;
  }

  abstract class Module1 {
    @Provides static String one() {
      return "one";
    }
  }
}
