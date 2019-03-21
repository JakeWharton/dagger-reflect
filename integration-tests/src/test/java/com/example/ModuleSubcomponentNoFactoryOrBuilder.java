package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

@Component(modules = ModuleSubcomponentNoFactoryOrBuilder.StringModule.class)
public interface ModuleSubcomponentNoFactoryOrBuilder {
  String string();

  @Module(subcomponents = StringSubcomponent.class)
  abstract class StringModule {
    @Provides
    static String string(StringSubcomponent subcomponent) {
      return subcomponent.value().toString();
    }
  }

  @Subcomponent(modules = LongValue.class)
  interface StringSubcomponent {
    Long value();
  }

  @Module
  abstract class LongValue {
    @Provides static Long string() {
      return 5L;
    }
  }
}
