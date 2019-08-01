package com.example;

import com.example.ExampleActivity.ExampleActivityModule;
import com.example.ExampleService.ExampleServiceModule;
import com.example.TestActivity.TestActivityModule;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.DispatchingAndroidInjector;

@Component(modules = {
    ExampleActivityModule.class,
    ExampleServiceModule.class,
    TestActivityModule.class,
    AndroidInjectionModule.class,
    StringModule.class
})
interface AppComponent {
  void inject(ExampleApp app);
}
