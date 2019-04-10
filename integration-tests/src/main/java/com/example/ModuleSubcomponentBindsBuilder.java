package com.example;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

@Component(modules = ModuleSubcomponentBindsBuilder.StringModule.class)
public interface ModuleSubcomponentBindsBuilder {
  String string();

  @Module(subcomponents = StringSubcomponent.class)
  abstract class StringModule {
    @Provides static String string(StringSubcomponent.Builder builder) {
      return builder.longValue(5L).build().value().toString();
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
  }
}
