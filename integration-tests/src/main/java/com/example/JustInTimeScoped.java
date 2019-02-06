package com.example;

import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component
interface JustInTimeScoped {
  Thing thing();

  @Singleton
  final class Thing {
    @Inject
    Thing() {}
  }
}
