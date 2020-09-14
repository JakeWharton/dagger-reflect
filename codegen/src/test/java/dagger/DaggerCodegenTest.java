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
package dagger;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public final class DaggerCodegenTest {
  @Test
  public void create() {
    JustComponent component = Dagger.create(JustComponent.class);
    assertThat(component).isInstanceOf(JustComponent.class);
  }

  @Test
  public void createNoAnnotation() {
    try {
      Dagger.create(JustComponentNoAnnotation.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Unable to find generated component implementation "
                  + "dagger.Dagger"
                  + JustComponentNoAnnotation.class.getSimpleName()
                  + " for component "
                  + "dagger."
                  + JustComponentNoAnnotation.class.getSimpleName());
    }
  }

  @Test
  public void builder() {
    BuilderComponent.Builder builder = Dagger.builder(BuilderComponent.Builder.class);
    assertThat(builder).isInstanceOf(DaggerBuilderComponent.builder().getClass());
    BuilderComponent component = builder.build();
    assertThat(component).isInstanceOf(DaggerBuilderComponent.class);
  }

  @Test
  public void builderNoAnnotation() {
    try {
      Dagger.builder(BuilderComponentNoAnnotation.Builder.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Unable to find generated component implementation "
                  + "dagger.Dagger"
                  + BuilderComponentNoAnnotation.class.getSimpleName()
                  + " for component "
                  + "dagger."
                  + BuilderComponentNoAnnotation.class.getSimpleName());
    }
  }

  @Test
  public void builderNotNested() {
    try {
      Dagger.builder(BuilderNotNested.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              BuilderNotNested.class.getName()
                  + " is not a nested type inside of a component interface");
    }
  }

  @Test
  public void factory() {
    FactoryComponent.Factory factory = Dagger.factory(FactoryComponent.Factory.class);
    assertThat(factory).isInstanceOf(DaggerFactoryComponent.factory().getClass());
    FactoryComponent component = factory.create();
    assertThat(component).isInstanceOf(DaggerFactoryComponent.class);
  }

  @Test
  public void factoryNoAnnotation() {
    try {
      Dagger.factory(FactoryComponentNoAnnotation.Factory.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Unable to find generated component implementation "
                  + "dagger.Dagger"
                  + FactoryComponentNoAnnotation.class.getSimpleName()
                  + " for component "
                  + "dagger."
                  + FactoryComponentNoAnnotation.class.getSimpleName());
    }
  }

  @Test
  public void factoryNotNested() {
    try {
      Dagger.factory(FactoryNotNested.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              FactoryNotNested.class.getName()
                  + " is not a nested type inside of a component interface");
    }
  }
}
