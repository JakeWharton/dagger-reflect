package dagger;

@Component
interface FactoryComponent {
  @Component.Factory
  interface Factory {
    FactoryComponent create();
  }
}
