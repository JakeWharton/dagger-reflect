package com.example;

import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static androidx.lifecycle.Lifecycle.State.CREATED;
import static androidx.test.core.app.ActivityScenario.launch;
import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class) //
public final class ExampleActivityTest {
  @Test public void string() {
    // TODO Use assumptions once https://github.com/robolectric/robolectric/pull/4645 is released.
    if (BuildConfig.FLAVOR.equals("reflect")) return;
    // assumeFalse(BuildConfig.FLAVOR.equals("reflect"));

    try (ActivityScenario<ExampleActivity> scenario = launch(ExampleActivity.class)) {
      scenario.moveToState(CREATED);
      scenario.onActivity(activity -> {
        assertThat(activity.string).isEqualTo("Hello!");
      });
    }
  }
}
