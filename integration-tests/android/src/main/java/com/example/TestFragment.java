package com.example;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.TestFragment2.TestFragment2Module;
import dagger.Module;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import dagger.android.HasFragmentInjector;

import javax.inject.Inject;
import javax.inject.Named;

public class TestFragment extends Fragment implements HasAndroidInjector {

  @Named("fragment_string")
  @Inject
  String fragmentString;

  @Inject
  DispatchingAndroidInjector<Object> androidInjector;

  @Override
  public AndroidInjector<Object> androidInjector() {
    return androidInjector;
  }

  @Override
  public void onAttach(Context context) {
    AndroidInjection.inject(this);
    super.onAttach(context);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.test_fragment, container, false);
  }

  @Module
  static abstract class TestFragmentModule {

    @ContributesAndroidInjector(modules = {TestFragment2Module.class})
    abstract TestFragment fragment();

  }

}
