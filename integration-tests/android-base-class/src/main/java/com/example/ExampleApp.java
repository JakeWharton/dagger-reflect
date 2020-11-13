package com.example;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;

public final class ExampleApp extends DaggerApplication {
  @Override
  protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
    return DaggerAppComponent.factory().create(this);
  }
}
