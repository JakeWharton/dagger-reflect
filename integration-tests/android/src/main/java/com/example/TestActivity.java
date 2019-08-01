package com.example;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import com.example.TestFragment.TestFragmentModule;
import dagger.Module;
import dagger.Provides;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

import javax.inject.Inject;
import javax.inject.Named;

public final class TestActivity extends Activity implements HasAndroidInjector {

  @Inject
  DispatchingAndroidInjector<Object> androidInjector;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.test_activity);
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return androidInjector;
  }

  @Module
  static abstract class TestActivityModule {

    @ContributesAndroidInjector(modules = {TestFragmentModule.class, StringModule.class})
    abstract TestActivity activity();

  }

  @Module
  static abstract class StringModule {

    @Named("fragment_string")
    @Provides
    static String string() {
      return "some string";
    }

  }

}
