package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import dagger.Subcomponent;

import javax.inject.Inject;

@Component
interface ReusableScopedJustInTime {
  Bar bar();

  @Reusable
  class Bar {
    @Inject Bar() {
    }
  }
}
