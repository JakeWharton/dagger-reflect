package com.example;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

@Component(modules = ModuleSubcomponentBindsFactory.StringModule.class)
public interface ModuleSubcomponentBindsFactory {
  String string();

  @Module(subcomponents = StringSubcomponent.class)
  abstract class StringModule {
    @Provides static String string(StringSubcomponent.Factory factory) {
      return factory.create(5L).value().toString();
    }
  }

  @Subcomponent
  interface StringSubcomponent {
    Long value();

    @Subcomponent.Factory
    interface Factory {
      StringSubcomponent create(@BindsInstance Long value);
    }
  }
}
