package com.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import androidx.annotation.Nullable;
import dagger.Module;
import dagger.Provides;
import dagger.android.AndroidInjection;
import dagger.android.ContributesAndroidInjector;
import javax.inject.Inject;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.view.Gravity.CENTER;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public final class ExampleActivity extends Activity {
  @Inject String string;
  @Inject long aLong;
  @Inject int anInt;

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
    @ContributesAndroidInjector(modules = LongModule.class)
    abstract ExampleActivity activity();
  }

  @Module(includes = IntegerModule.class)
  static class LongModule {

    @Provides
    static long provideLong() {
      return 10L;
    }

  }

  @Module
  static class IntegerModule {

    @Provides
    static int provideInt() {
      return 20;
    }
  }

}
