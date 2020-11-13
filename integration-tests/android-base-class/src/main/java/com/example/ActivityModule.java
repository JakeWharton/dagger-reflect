package com.example;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector(modules = ExampleActivity.LongModule.class)
    abstract ExampleActivity contributeExampleActivity();

    @ContributesAndroidInjector(modules = {
            ExampleActivity.LongModule.class,
            FragmentModule.class
    })
    abstract ExampleFragmentInjectionActivity contributeExampleFragmentInjectionActivity();

    @Module
    static abstract class FragmentModule {
        @ContributesAndroidInjector
        abstract ExampleFragment exampleFragment();
    }
}
