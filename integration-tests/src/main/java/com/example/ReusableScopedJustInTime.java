package com.example;

import dagger.Component;
import dagger.Reusable;
import javax.inject.Inject;

@Component
interface ReusableScopedJustInTime {
  Bar bar();

  @Reusable
  class Bar {
    @Inject
    Bar() {}
  }
}
