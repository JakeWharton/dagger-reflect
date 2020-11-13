package com.example;

import com.example.ExampleService.ExampleServiceModule;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@Component(modules = {
    ActivityModule.class,
    ExampleServiceModule.class,
    AndroidInjectionModule.class,
    StringModule.class
})
public interface AppComponent extends AndroidInjector<ExampleApp> {
  @Component.Factory
  interface Factory extends AndroidInjector.Factory<ExampleApp> {
  }
}
