Change Log
==========

Version 0.2.0
-------------

*2019-12-08*

 * New: Add lint artifact `dagger-reflect-lint` which will validate that qualifiers and map keys correctly specify runtime retention.
 * New: Include `@Generated` annotation on types created by `reflect-compiler`.
 * Fix: Support binding to a component or subcomponent instance inside that component or subcomponent.
 * Fix: Properly handle `@ClassKey` and other multibinding keys whose type contains a generic or wildcard.
 * Fix: Account for additional modules specified by `@ContributesAndroidInjector`.
 * Fix: Handle `@Multibinds` annotation.
 * Fix: Recursively expand type hierarchies for component dependencies.
 * Fix: Allow multiple interfaces in a component dependency hierarchy to expose the same type.
 * Fix: Allow non-provision methods on module instances and do not use them to determine whether a module instance is required.


Version 0.1.0
-------------

*2019-07-01*

Initial release.
