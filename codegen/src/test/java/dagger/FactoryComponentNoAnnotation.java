package dagger;

interface FactoryComponentNoAnnotation {
  @Keep
  interface Factory {
    FactoryComponentNoAnnotation create();
  }
}
