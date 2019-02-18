package com.example;

import android.app.Activity;
import com.example.ExampleActivity.ExampleActivityModule;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.DispatchingAndroidInjector;

@Component(modules = {
    ExampleActivityModule.class,
    AndroidInjectionModule.class,
    StringModule.class
})
interface AppComponent {
  DispatchingAndroidInjector<Activity> activityInjector();
}
