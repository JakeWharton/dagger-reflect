package com.example;

import android.app.Application;
import dagger.Dagger;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

import javax.inject.Inject;

public final class ExampleApp extends Application implements HasAndroidInjector {
//  private AppComponent component;

  @Inject
  DispatchingAndroidInjector<Object> injector;

  @Override public void onCreate() {
    super.onCreate();

    Dagger.create(AppComponent.class).inject(this);
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return injector;
  }
}
