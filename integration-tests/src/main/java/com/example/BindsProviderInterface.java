package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = BindsProviderInterface.Module1.class)
interface BindsProviderInterface {
  CharSequence string();

  @Module
  interface Module1 {
    @Provides static String string() {
      return "foo";
    }
    @Binds CharSequence charSequence(String foo);
  }
}
