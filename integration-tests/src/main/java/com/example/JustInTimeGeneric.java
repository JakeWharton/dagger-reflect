package com.example;

import dagger.Component;
import javax.inject.Inject;

@Component
interface JustInTimeGeneric {
  Thing<String> thing();

  final class Thing<T> {
    @Inject
    Thing() {}
  }
}
