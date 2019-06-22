package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;
import javax.inject.Provider;

@Component(modules = ProviderMultipleGenericIntoJustInTime.Module1.class)
interface ProviderMultipleGenericIntoJustInTime {
  Thing<String, Integer> thing();

  @Module
  abstract class Module1 {
    @Provides
    static String provideString() {
      return "foo";
    }

    @Provides
    static Integer provideInteger() {
      return 1;
    }
  }

  final class Thing<T, V> {
    final Provider<V> valueProvider;
    final Provider<T> thingProvider;

    // Constructor parameters are flipped from class declaration to ensure the implementation does
    // not rely on ordering.
    @Inject
    Thing(Provider<V> valueProvider, Provider<T> thingProvider) {
      this.thingProvider = thingProvider;
      this.valueProvider = valueProvider;
    }
  }
}
