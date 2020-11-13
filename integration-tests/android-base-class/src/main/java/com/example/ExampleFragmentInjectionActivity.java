package com.example;

import android.os.Bundle;

import androidx.annotation.Nullable;

import dagger.android.DaggerActivity;

public class ExampleFragmentInjectionActivity extends DaggerActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new ExampleFragment(), "ExampleFragment")
                    .commit();
        }
    }
}
