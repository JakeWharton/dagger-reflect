package com.example;

import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;

@Component
interface JustInTimeScopedIntoUnscoped {
  Thing thing();

  @Singleton
  final class Thing {
    @Inject
    Thing() {}
  }
}
