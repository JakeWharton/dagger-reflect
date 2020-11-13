package com.example;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class ExampleFragmentInjectionActivity extends Activity implements HasAndroidInjector {
    @Inject DispatchingAndroidInjector<Object> androidInjector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new ExampleFragment(), "ExampleFragment")
                    .commit();
        }
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }
}
