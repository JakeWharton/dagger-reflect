package com.example;

import com.example.ExampleActivity.ExampleActivityModule;
import com.example.ExampleService.ExampleServiceModule;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.DispatchingAndroidInjector;

@Component(modules = {
    ExampleActivityModule.class,
    ExampleServiceModule.class,
    AndroidInjectionModule.class,
    StringModule.class
})
interface AppComponent {
  DispatchingAndroidInjector<Object> androidInjector();
}
