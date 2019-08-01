package com.example;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static androidx.test.core.app.ActivityScenario.launch;

@RunWith(RobolectricTestRunner.class)
public class TestActivityTest {
  @Test public void test() {
    try (ActivityScenario<TestActivity> scenario = launch(TestActivity.class)) {
      scenario.moveToState(Lifecycle.State.CREATED);
    }
  }
}
