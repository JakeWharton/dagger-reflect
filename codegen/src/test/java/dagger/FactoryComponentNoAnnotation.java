package dagger;

interface FactoryComponentNoAnnotation {
  interface Factory {
    FactoryComponentNoAnnotation create();
  }
}
