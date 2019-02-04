package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = BindsProviderAbstract.Module1.class)
interface BindsProviderAbstract {
  CharSequence string();

  @Module
  abstract class Module1 {
    @Provides static String string() {
      return "foo";
    }
    @Binds abstract CharSequence charSequence(String foo);
  }
}
