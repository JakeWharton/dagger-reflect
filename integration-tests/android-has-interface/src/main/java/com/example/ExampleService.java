package com.example;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import dagger.Module;
import dagger.android.AndroidInjection;
import dagger.android.ContributesAndroidInjector;
import javax.inject.Inject;

public final class ExampleService extends Service {
  @Inject String string;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    AndroidInjection.inject(this);
    super.onCreate();
    if (string == null) {
      throw new NullPointerException("String was not injected");
    }
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
  }

  @Module
  abstract static class ExampleServiceModule {
    @ContributesAndroidInjector
    abstract ExampleService service();
  }
}
