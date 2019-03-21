package com.example;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

@Component(modules = ModuleSubcomponentBindsFactoryAndBuilder.StringModule.class)
public interface ModuleSubcomponentBindsFactoryAndBuilder {
  String string();

  @Module(subcomponents = StringSubcomponent.class)
  abstract class StringModule {
    @Provides
    static String string(StringSubcomponent.Builder builder, StringSubcomponent.Factory factory) {
      return builder.longValue(5L).build().value().toString()
          + " and "
          + factory.create(5L).value().toString();
    }
  }

  @Subcomponent
  interface StringSubcomponent {
    Long value();

    @Subcomponent.Builder
    interface Builder {
      @BindsInstance Builder longValue(Long value);
      StringSubcomponent build();
    }

    @Subcomponent.Factory
    interface Factory {
      StringSubcomponent create(@BindsInstance Long value);
    }
  }
}
