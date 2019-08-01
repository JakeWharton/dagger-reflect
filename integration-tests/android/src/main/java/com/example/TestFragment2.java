package com.example;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import dagger.Module;
import dagger.android.AndroidInjection;
import dagger.android.ContributesAndroidInjector;

import javax.inject.Inject;
import javax.inject.Named;

public class TestFragment2 extends Fragment {

  @Named("fragment_string")
  @Inject
  String fragmentString;

  @Override
  public void onAttach(Context context) {
    AndroidInjection.inject(this);
    super.onAttach(context);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return new TextView(getActivity());
  }

  @Module
  static abstract class TestFragment2Module {

    @ContributesAndroidInjector
    abstract TestFragment2 fragment();

  }

}
