package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = PrimitiveAutoUnboxing.Module1.class)
interface PrimitiveAutoUnboxing {

  byte getByte();

  short getShort();

  int getInt();

  long getLong();

  float getFloat();

  double getDouble();

  boolean getBoolean();

  char getChar();

  @Module
  abstract class Module1 {
    @Provides
    static Byte provideByte() {
      return 8;
    }

    @Provides
    static Short provideShort() {
      return 16;
    }

    @Provides
    static Integer provideInteger() {
      return 32;
    }

    @Provides
    static Long provideLong() {
      return 64L;
    }

    @Provides
    static Float provideFloat() {
      return -32.0f;
    }

    @Provides
    static Double provideDouble() {
      return -64.0;
    }

    @Provides
    static Boolean provideBoolean() {
      return true;
    }

    @Provides
    static Character provideCharacter() {
      return '\u221E';
    }
  }
}
