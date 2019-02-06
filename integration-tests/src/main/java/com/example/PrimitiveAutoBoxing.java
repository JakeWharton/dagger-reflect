package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = PrimitiveAutoBoxing.Module1.class)
interface PrimitiveAutoBoxing {

  Byte getByte();

  Short getShort();

  Integer getInteger();

  Long getLong();

  Float getFloat();

  Double getDouble();

  Boolean getBoolean();

  Character getCharacter();

  @Module
  abstract class Module1 {
    @Provides
    static byte provideByte() {
      return 8;
    }

    @Provides
    static short provideShort() {
      return 16;
    }

    @Provides
    static int provideInt() {
      return 32;
    }

    @Provides
    static long provideLong() {
      return 64L;
    }

    @Provides
    static float provideFloat() {
      return -32.0f;
    }

    @Provides
    static double provideDouble() {
      return -64.0;
    }

    @Provides
    static boolean provideBoolean() {
      return true;
    }

    @Provides
    static char provideChar() {
      return '\u221E';
    }
  }
}
