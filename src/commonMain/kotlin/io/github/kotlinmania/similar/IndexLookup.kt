// port-lint: ignore - Kotlin shim for generic index access.
package io.github.kotlinmania.similar

/**
 * Read-only indexable lookup keyed by `Int`.
 *
 * Implementations decide how to fetch the element at a given index; the
 * [unique], [commonPrefixLen], and similar helpers in
 * [io.github.kotlinmania.similar.algorithms] route everything through this
 * interface.
 */
fun interface IndexLookup<out T> {
    /** Returns the element at the given [index]. */
    operator fun get(index: Int): T
}

/** Adapts a [List] into an [IndexLookup]. */
fun <T> List<T>.asLookup(): IndexLookup<T> = IndexLookup { this[it] }

/** Adapts an [Array] into an [IndexLookup]. */
fun <T> Array<T>.asLookup(): IndexLookup<T> = IndexLookup { this[it] }

/** Adapts a [CharSequence] into an [IndexLookup] of `Char`. */
fun CharSequence.asLookup(): IndexLookup<Char> = IndexLookup { this[it] }
