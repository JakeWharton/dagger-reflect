package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

class NestedComponent {
  static class MoreNesting {
    interface AndMore {

      @Component(modules = TheComponent.Module1.class)
      interface TheComponent {
        String string();

        @Component.Builder
        interface Builder {
          TheComponent build();
        }

        @Module
        abstract class Module1 {
          @Provides
          static String string() {
            return "foo";
          }
        }
      }
    }
  }
}
