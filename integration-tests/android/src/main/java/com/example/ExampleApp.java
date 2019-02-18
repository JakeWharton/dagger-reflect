package com.example;

import android.app.Activity;
import android.app.Application;
import dagger.Dagger;
import dagger.android.AndroidInjector;
import dagger.android.HasActivityInjector;

public final class ExampleApp extends Application implements HasActivityInjector {
  private AppComponent component;

  @Override public void onCreate() {
    super.onCreate();

    component = Dagger.create(AppComponent.class);
  }

  @Override public AndroidInjector<Activity> activityInjector() {
    return component.activityInjector();
  }
}
