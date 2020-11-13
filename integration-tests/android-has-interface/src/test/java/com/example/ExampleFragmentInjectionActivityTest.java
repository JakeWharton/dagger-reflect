package com.example;

import static androidx.lifecycle.Lifecycle.State.CREATED;
import static androidx.test.core.app.ActivityScenario.launch;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class) //
public class ExampleFragmentInjectionActivityTest {
  @Test
  public void activityFragmentInjection() {
    try (ActivityScenario<ExampleFragmentInjectionActivity> scenario =
        launch(ExampleFragmentInjectionActivity.class)) {
      scenario.moveToState(CREATED);
      scenario.onActivity(
          activity -> {
            ExampleFragment fragment =
                (ExampleFragment)
                    activity.getFragmentManager().findFragmentByTag("ExampleFragment");
            assertThat(fragment.string).isEqualTo("Hello!");
            assertThat(fragment.aLong).isEqualTo(10L);
            assertThat(fragment.anInt).isEqualTo(20);
          });
    }
  }
}
