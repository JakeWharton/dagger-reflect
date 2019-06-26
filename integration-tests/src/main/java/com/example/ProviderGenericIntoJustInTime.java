package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;
import javax.inject.Provider;

@Component(modules = ProviderGenericIntoJustInTime.Module1.class)
interface ProviderGenericIntoJustInTime {
  Thing<String> thing();

  @Module
  abstract class Module1 {
    @Provides
    static String provideString() {
      return "foo";
    }
  }

  final class Thing<T> {
    final Provider<T> genericProvider;

    @Inject
    Thing(Provider<T> genericProvider) {
      this.genericProvider = genericProvider;
    }
  }
}
