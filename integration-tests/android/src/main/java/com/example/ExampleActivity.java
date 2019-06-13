package com.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import dagger.Module;
import dagger.android.AndroidInjection;
import dagger.android.ContributesAndroidInjector;
import javax.inject.Inject;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.view.Gravity.CENTER;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public final class ExampleActivity extends Activity {
  @Inject String string;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    TextView textView = new TextView(this);
    textView.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    textView.setGravity(CENTER);
    textView.setTextSize(COMPLEX_UNIT_DIP, 40);
    textView.setText(string);
    setContentView(textView);

    startService(new Intent(this, ExampleService.class));
  }

  @Module
  static abstract class ExampleActivityModule {
    @ContributesAndroidInjector
    abstract ExampleActivity activity();
  }
}
