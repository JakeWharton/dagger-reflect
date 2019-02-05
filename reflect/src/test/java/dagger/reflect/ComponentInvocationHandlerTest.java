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
import java.lang.reflect.Method;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public final class ComponentInvocationHandlerTest {
  @Component interface MembersInjectorWrongReturnType {
    // [dagger-compile] error: Members injection methods may only return the injected type or void.
    @SuppressWarnings("UnusedReturnValue") // Behavior under test.
    String inject(Target instance);

    class Target {}
  }

  @Test public void membersInjectionWrongReturnType() throws NoSuchMethodException {
    BindingGraph graph = new BindingGraph.Builder().build();
    MembersInjectorWrongReturnType component =
        ComponentInvocationHandler.create(MembersInjectorWrongReturnType.class, graph);
    MembersInjectorWrongReturnType.Target instance = new MembersInjectorWrongReturnType.Target();
    try {
      component.inject(instance);
      fail();
    } catch (IllegalStateException e) {
      Method expectedMethod = MembersInjectorWrongReturnType.class.getDeclaredMethod(
          "inject", MembersInjectorWrongReturnType.Target.class);
      assertThat(e).hasMessageThat().isEqualTo(
          "Members injection methods may only return the injected type or void: " + expectedMethod);
    }
  }
}
