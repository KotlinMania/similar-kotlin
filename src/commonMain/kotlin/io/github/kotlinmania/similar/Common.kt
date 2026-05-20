// port-lint: source common.rs
package io.github.kotlinmania.similar

import io.github.kotlinmania.similar.algorithms.Capture
import io.github.kotlinmania.similar.algorithms.Compact
import io.github.kotlinmania.similar.algorithms.Replace
import io.github.kotlinmania.similar.algorithms.diffDeadline

/**
 * Creates a diff between old and new with the given algorithm capturing the ops.
 *
 * This is like `diff` in the algorithms package but instead of using an
 * arbitrary hook this will always use [Compact] plus [Replace] plus [Capture]
 * and return the captured [DiffOp]s.
 */
fun <T> captureDiff(
    alg: Algorithm,
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
): List<DiffOp> where T : Comparable<T> =
    captureDiffDeadline(alg, old, oldRange, new, newRange, null)

/** Creates a diff between old and new with the given algorithm capturing the ops. */
fun <T> captureDiffDeadline(
    alg: Algorithm,
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
    deadline: kotlin.time.TimeMark?,
): List<DiffOp> where T : Comparable<T> {
    val d = Compact(Replace(Capture()), old, new)
    val result = diffDeadline(alg, d, old, oldRange, new, newRange, deadline)
    if (result is io.github.kotlinmania.similar.algorithms.DiffHookResult.Err) {
        error("Capture diff hook cannot fail")
    }
    return d.intoInner().intoInner().intoOps()
}

/** Creates a diff between old and new with the given algorithm capturing the ops. */
fun <T> captureDiffSlices(alg: Algorithm, old: List<T>, new: List<T>): List<DiffOp> where T : Comparable<T> =
    captureDiffSlicesDeadline(alg, old, new, null)

/** Creates a diff between old and new with the given algorithm capturing the ops. */
fun <T> captureDiffSlicesDeadline(
    alg: Algorithm,
    old: List<T>,
    new: List<T>,
    deadline: kotlin.time.TimeMark?,
): List<DiffOp> where T : Comparable<T> =
    captureDiffDeadline(alg, old.asLookup(), old.indices, new.asLookup(), new.indices, deadline)

/**
 * Return a measure of similarity in the range `0..=1`.
 *
 * A ratio of `1.0` means the two sequences are a complete match, a
 * ratio of `0.0` would indicate completely distinct sequences. The input
 * is the sequence of diff operations and the length of the old and new
 * sequence.
 */
fun getDiffRatio(ops: List<DiffOp>, oldLen: Int, newLen: Int): Float {
    val matches = ops.sumOf { op ->
        when (op) {
            is DiffOp.Equal -> op.len
            is DiffOp.Delete,
            is DiffOp.Insert,
            is DiffOp.Replace,
            -> 0
        }
    }
    val len = oldLen + newLen
    return if (len == 0) {
        1.0f
    } else {
        2.0f * matches.toFloat() / len.toFloat()
    }
}

/**
 * Isolate change clusters by eliminating ranges with no changes.
 *
 * This will leave holes behind in long periods of equal ranges so that
 * you can build things like unified diffs.
 */
fun groupDiffOps(inputOps: List<DiffOp>, n: Int): List<List<DiffOp>> {
    val ops = inputOps.map { it.copyOp() }.toMutableList()
    if (ops.isEmpty()) {
        return emptyList()
    }

    var pendingGroup = mutableListOf<DiffOp>()
    val rv = mutableListOf<List<DiffOp>>()

    val first = ops.first()
    if (first is DiffOp.Equal) {
        val offset = (first.len - n).coerceAtLeast(0)
        first.oldIndex += offset
        first.newIndex += offset
        first.len -= offset
    }

    val last = ops.last()
    if (last is DiffOp.Equal) {
        last.len -= (last.len - n).coerceAtLeast(0)
    }

    for (op in ops) {
        if (op is DiffOp.Equal) {
            if (op.len > n * 2) {
                pendingGroup += DiffOp.Equal(
                    oldIndex = op.oldIndex,
                    newIndex = op.newIndex,
                    len = n,
                )
                rv += pendingGroup
                val offset = (op.len - n).coerceAtLeast(0)
                pendingGroup = mutableListOf(
                    DiffOp.Equal(
                        oldIndex = op.oldIndex + offset,
                        newIndex = op.newIndex + offset,
                        len = op.len - offset,
                    ),
                )
                continue
            }
        }
        pendingGroup += op
    }

    if (pendingGroup.isNotEmpty() && pendingGroup.any { it !is DiffOp.Equal }) {
        rv += pendingGroup
    }

    return rv
}

private fun DiffOp.copyOp(): DiffOp =
    when (this) {
        is DiffOp.Equal -> copy()
        is DiffOp.Delete -> copy()
        is DiffOp.Insert -> copy()
        is DiffOp.Replace -> copy()
    }
