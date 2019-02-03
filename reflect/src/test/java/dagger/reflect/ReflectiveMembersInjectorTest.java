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

import javax.inject.Inject;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings("unused") // Unused fields/parameters and over-specified visibility for testing.
public final class ReflectiveMembersInjectorTest {
  private static class PrivateField {
    // [dagger-compiler] error: Dagger does not support injection into private fields
    @Inject private String privateField;
  }

  @Test public void privateFieldFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(PrivateField.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Dagger does not support injection into private fields: "
              + PrivateField.class.getCanonicalName()
              + ".privateField");
    }
  }

  private static class StaticField {
    // [dagger-compiler] error: Dagger does not support injection into static fields
    @Inject static String staticField;
  }

  @Test public void staticFieldFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(StaticField.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Dagger does not support injection into static fields: "
              + StaticField.class.getCanonicalName()
              + ".staticField");
    }
  }

  private static class PrivateMethod {
    // [dagger-compiler] error: Dagger does not support injection into private methods
    @Inject private void privateMethod(String one) {}
  }

  @Test public void privateMethodFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(PrivateMethod.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Dagger does not support injection into private methods: "
              + PrivateMethod.class.getCanonicalName()
              + ".privateMethod()");
    }
  }

  private static class StaticMethod {
    // [dagger-compiler] error: Dagger does not support injection into static methods
    @Inject static void staticMethod(String one) {}
  }

  @Test public void staticMethodFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(StaticMethod.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Dagger does not support injection into static methods: "
              + StaticMethod.class.getCanonicalName()
              + ".staticMethod()");
    }
  }

  private interface Interface {
    // [dagger-compile] Methods with @Inject may not be abstract
    @Inject void interfaceMethod(String one);
  }

  @Test public void interfaceInjectionFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(Interface.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Methods with @Inject may not be abstract: "
              + Interface.class.getCanonicalName()
              + ".interfaceMethod()");
    }
  }

  private static abstract class Abstract {
    // [dagger-compile] Methods with @Inject may not be abstract
    @Inject abstract void abstractMethod(String one);
  }

  @Test public void abstractInjectionFails() {
    BindingGraph graph = new BindingGraph.Builder().build();
    try {
      ReflectiveMembersInjector.create(Abstract.class, graph);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat()
          .startsWith("Methods with @Inject may not be abstract: "
              + Abstract.class.getCanonicalName()
              + ".abstractMethod()");
    }
  }
}
