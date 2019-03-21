package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = JustInTimeMembersInjection.Module1.class)
interface JustInTimeMembersInjection {
  Thing thing();

  final class Thing {
    final String stringConstructor;
    @Inject String stringField;
    String stringMethod;

    @Inject Thing(String string) {
      stringConstructor = string;
    }

    @Inject void setString(String string) {
      stringMethod = string;
    }
  }

  @Module
  abstract class Module1 {
    @Provides static String string() {
      return "hey";
    }
  }
}
