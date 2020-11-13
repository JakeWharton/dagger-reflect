package com.example;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.view.Gravity.CENTER;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ExampleFragment extends Fragment implements HasAndroidInjector {
    @Inject DispatchingAndroidInjector<Object> androidInjector;
    @Inject String string;
    @Inject long aLong;
    @Inject int anInt;


    @Override
    public void onAttach(Activity activity) {
        AndroidInjection.inject(this);
        super.onAttach(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        textView.setGravity(CENTER);
        textView.setTextSize(COMPLEX_UNIT_DIP, 40);
        textView.setText(string);

        return textView;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }
}
