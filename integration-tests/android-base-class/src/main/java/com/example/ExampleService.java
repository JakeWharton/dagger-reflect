package com.example;

import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import dagger.Module;
import dagger.android.AndroidInjection;
import dagger.android.ContributesAndroidInjector;
import dagger.android.DaggerService;
import javax.inject.Inject;

public final class ExampleService extends DaggerService {
  @Inject String string;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    if (string == null) {
      throw new NullPointerException("String was not injected");
    }
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  @Module
  static abstract class ExampleServiceModule {
    @ContributesAndroidInjector
    abstract ExampleService service();
  }
}
