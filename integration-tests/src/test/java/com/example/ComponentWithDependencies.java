package com.example;

import dagger.Component;

@Component(dependencies = {String.class, Runnable.class})
public interface ComponentWithDependencies {}
