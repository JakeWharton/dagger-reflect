package com.example;

import android.app.Application;
import dagger.Dagger;
import dagger.android.AndroidInjector;
import dagger.android.HasAndroidInjector;

public final class ExampleApp extends Application implements HasAndroidInjector {
  private AppComponent component;

  @Override
  public void onCreate() {
    super.onCreate();

    component = Dagger.create(AppComponent.class);
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return component.androidInjector();
  }
}
