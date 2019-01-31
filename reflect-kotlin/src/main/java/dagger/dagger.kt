@file:Suppress("NOTHING_TO_INLINE")

package dagger

import kotlin.reflect.KClass

inline fun <C : Any> KClass<C>.create(): C = Dagger.create(java)
inline fun <B : Any> KClass<B>.builder(): B = Dagger.builder(java)
