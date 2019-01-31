package dagger

import org.junit.Assert.assertNotNull
import org.junit.Test

class DaggerTest {
  @Test fun create() {
    assertNotNull(NoBuilderComponent::class.create())
  }

  @Test fun builder() {
    assertNotNull(BuilderComponent.Builder::class.builder())
  }
}
