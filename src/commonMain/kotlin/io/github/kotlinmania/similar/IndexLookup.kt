// port-lint: ignore — Kotlin shim standing in for std::ops::Index<usize>; the
// similar crate is generic over any indexable source, but Kotlin lacks a built-in
// counterpart to Rust's std::ops::Index trait, so the port defines its own.
package io.github.kotlinmania.similar

/**
 * Read-only indexable lookup keyed by `Int`.
 *
 * Stands in for the upstream `std::ops::Index<usize, Output = T>` bound used
 * throughout the diff algorithms. Implementations decide how to fetch the
 * element at a given index; the [unique], [commonPrefixLen], and similar
 * helpers in [io.github.kotlinmania.similar.algorithms] route everything
 * through this interface.
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
