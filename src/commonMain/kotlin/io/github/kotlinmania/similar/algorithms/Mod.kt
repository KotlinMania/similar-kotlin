// port-lint: source algorithms/mod.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.Algorithm
import io.github.kotlinmania.similar.IndexLookup
import io.github.kotlinmania.similar.asLookup
import kotlin.time.TimeMark

/**
 * Creates a diff between old and new with the given algorithm.
 *
 * Diffs `old`, between indices `oldRange` and `new` between indices `newRange`.
 */
fun <E, T> diff(
    alg: Algorithm,
    d: DiffHook<E>,
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
): DiffHookResult<E> =
    diffDeadline(alg, d, old, oldRange, new, newRange, null)

/**
 * Creates a diff between old and new with the given algorithm with deadline.
 *
 * Diffs `old`, between indices `oldRange` and `new` between indices `newRange`.
 *
 * This diff is done with an optional deadline that defines the maximal
 * execution time permitted before it bails and falls back to an approximation.
 * Note that not all algorithms behave well if they reach the deadline; LCS for
 * instance produces a very simplistic diff when the deadline is reached in all
 * cases.
 */
fun <E, T> diffDeadline(
    alg: Algorithm,
    d: DiffHook<E>,
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
    deadline: TimeMark?,
): DiffHookResult<E> =
    when (alg) {
        Algorithm.Myers -> myersDiffDeadline(d, old, oldRange, new, newRange, deadline)
        Algorithm.Patience -> patienceDiffDeadline(d, old, oldRange, new, newRange, deadline)
        Algorithm.Lcs -> lcsDiffDeadline(d, old, oldRange, new, newRange, deadline)
    }

/** Shortcut for diffing slices with a specific algorithm. */
fun <E, T> diffSlices(alg: Algorithm, d: DiffHook<E>, old: List<T>, new: List<T>): DiffHookResult<E> =
    diff(alg, d, old.asLookup(), old.indices, new.asLookup(), new.indices)

/** Shortcut for diffing slices with a specific algorithm. */
fun <E, T> diffSlicesDeadline(
    alg: Algorithm,
    d: DiffHook<E>,
    old: List<T>,
    new: List<T>,
    deadline: TimeMark?,
): DiffHookResult<E> =
    diffDeadline(alg, d, old.asLookup(), old.indices, new.asLookup(), new.indices, deadline)
