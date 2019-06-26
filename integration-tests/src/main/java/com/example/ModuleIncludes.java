package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleIncludes.StringModule.class)
public interface ModuleIncludes {
  String string();

  @Module(includes = {AddModule.class, IntegerModule.class})
  abstract class StringModule {
    @Provides
    static String string(Number value) {
      return value.toString();
    }
  }

  @Module(includes = LongModule.class)
  abstract class AddModule {
    @Provides
    static Number add(Long longValue, Integer integerValue) {
      return longValue + integerValue;
    }
  }

  @Module
  abstract class LongModule {
    @Provides
    static Long value() {
      return 3L;
    }
  }

  @Module
  abstract class IntegerModule {
    @Provides
    static Integer value() {
      return 2;
    }
  }
}
