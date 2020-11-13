package com.example;

import static androidx.lifecycle.Lifecycle.State.CREATED;
import static androidx.test.core.app.ActivityScenario.launch;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeFalse;

import androidx.test.core.app.ActivityScenario;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class) //
public class ExampleFragmentInjectionActivityTest {
  @BeforeClass
  public static void beforeClass() {
    // TODO https://github.com/JakeWharton/dagger-reflect/issues/202
    assumeFalse(BuildConfig.FLAVOR.equals("reflect"));
  }
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
