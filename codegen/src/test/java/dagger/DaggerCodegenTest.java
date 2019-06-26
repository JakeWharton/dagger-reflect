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
    JustComponent actual = Dagger.create(JustComponent.class);
    assertThat(actual).isInstanceOf(JustComponent.class);
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
                  + "dagger.DaggerJustComponentNoAnnotation for component "
                  + "dagger.JustComponentNoAnnotation");
    }
  }

  @Test
  public void builder() {
    BuilderComponent.Builder actual = Dagger.builder(BuilderComponent.Builder.class);
    assertThat(actual).isInstanceOf(DaggerBuilderComponent.builder().getClass());
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
                  + "dagger.DaggerBuilderComponentNoAnnotation for component "
                  + "dagger.BuilderComponentNoAnnotation");
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
              "dagger.BuilderNotNested is not a nested type inside of a component interface");
    }
  }

  @Test
  public void factory() {
    FactoryComponent.Factory actual = Dagger.factory(FactoryComponent.Factory.class);
    assertThat(actual).isInstanceOf(DaggerFactoryComponent.factory().getClass());
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
                  + "dagger.DaggerFactoryComponentNoAnnotation for component "
                  + "dagger.FactoryComponentNoAnnotation");
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
              "dagger.FactoryNotNested is not a nested type inside of a component interface");
    }
  }
}
