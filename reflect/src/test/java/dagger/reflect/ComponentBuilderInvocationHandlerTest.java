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

import dagger.Module;
import org.junit.Test;

import static java.util.Collections.emptySet;
import static org.junit.Assert.fail;

public final class ComponentBuilderInvocationHandlerTest {
  public interface UndeclaredModules {
    interface Builder {
      UndeclaredModules module(Module1 module);
    }

    @Module
    class Module1 {}
  }

  @Test public void undeclaredModule() {
    UndeclaredModules.Builder builder =
        ComponentBuilderInvocationHandler.create(UndeclaredModules.class,
            UndeclaredModules.Builder.class, emptySet(), emptySet());
    try {
      builder.module(new UndeclaredModules.Module1());
      fail();
    } catch (IllegalStateException e) {
      // TODO assert message
    }
  }

  public interface UndeclaredDependencies {
    interface Builder {
      UndeclaredDependencies dep(String module);
    }
  }

  @Test public void undeclaredDependencies() {
    UndeclaredDependencies.Builder builder =
        ComponentBuilderInvocationHandler.create(UndeclaredDependencies.class,
            UndeclaredDependencies.Builder.class, emptySet(), emptySet());
    try {
      builder.dep("hey");
      fail();
    } catch (IllegalStateException e) {
      // TODO assert message
    }
  }
}
