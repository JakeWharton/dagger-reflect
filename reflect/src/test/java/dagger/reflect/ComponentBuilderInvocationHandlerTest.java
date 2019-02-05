/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dagger.reflect;

import dagger.Component;
import dagger.Module;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.emptySet;
import static org.junit.Assert.fail;

public final class ComponentBuilderInvocationHandlerTest {
  @Component public interface UndeclaredModules {
    // [dagger-compiler] error: @Component.Builder has setters for modules or components that aren't required:
    // [UndeclaredModules.Builder UndeclaredModules.Builder.module(UndeclaredModules.Module1)]
    @Component.Builder interface Builder {
      Builder module(Module1 module);
      UndeclaredModules build();
    }

    @Module
    class Module1 {}
  }

  @Test public void undeclaredModule() throws NoSuchMethodException {
    UndeclaredModules.Builder builder =
        ComponentBuilderInvocationHandler.create(UndeclaredModules.class,
            UndeclaredModules.Builder.class, emptySet(), emptySet());
    try {
      builder.module(new UndeclaredModules.Module1());
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo(
          "@Component.Builder has setters for modules or components that aren't required: ["
              + UndeclaredModules.Builder.class.getDeclaredMethod("module", UndeclaredModules.Module1.class)
              + "]");
    }
  }

  @Component public interface UndeclaredDependencies {
    // [dagger-compiler] error: @Component.Builder has setters for modules or components that aren't required:
    // [UndeclaredDependencies.Builder UndeclaredDependencies.Builder.dep(String)]
    @Component.Builder interface Builder {
      Builder dep(String module);
      UndeclaredDependencies build();
    }
  }

  @Test public void undeclaredDependencies() throws NoSuchMethodException {
    UndeclaredDependencies.Builder builder =
        ComponentBuilderInvocationHandler.create(UndeclaredDependencies.class,
            UndeclaredDependencies.Builder.class, emptySet(), emptySet());
    try {
      builder.dep("hey");
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo(
          "@Component.Builder has setters for modules or components that aren't required: ["
              + UndeclaredDependencies.Builder.class.getDeclaredMethod("dep", String.class)
              + "]");
    }
  }
}
