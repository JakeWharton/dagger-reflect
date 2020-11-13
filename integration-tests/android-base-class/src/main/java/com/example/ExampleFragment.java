package com.example;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.view.Gravity.CENTER;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import dagger.android.DaggerFragment;
import javax.inject.Inject;

public class ExampleFragment extends DaggerFragment {
  @Inject String string;
  @Inject long aLong;
  @Inject int anInt;

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    TextView textView = new TextView(getActivity());
    textView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    textView.setGravity(CENTER);
    textView.setTextSize(COMPLEX_UNIT_DIP, 40);
    textView.setText(string);

    return textView;
  }
}
