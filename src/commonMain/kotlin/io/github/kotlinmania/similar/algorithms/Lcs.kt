// port-lint: source algorithms/lcs.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.IndexLookup
import io.github.kotlinmania.similar.deadlineExceeded
import kotlin.time.TimeMark

/**
 * LCS diff algorithm.
 *
 * Diff `old`, between indices `oldRange` and `new` between indices `newRange`.
 */
fun <E, T> lcsDiff(
    d: DiffHook<E>,
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
): DiffHookResult<E> =
    lcsDiffDeadline(d, old, oldRange, new, newRange, null)

/**
 * LCS diff algorithm.
 *
 * Diff `old`, between indices `oldRange` and `new` between indices `newRange`.
 *
 * This diff is done with an optional deadline that defines the maximal
 * execution time permitted before it bails and falls back to an approximation.
 */
fun <E, T> lcsDiffDeadline(
    d: DiffHook<E>,
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
    deadline: TimeMark?,
): DiffHookResult<E> {
    if (isEmptyRange(newRange)) {
        when (val deleted = d.delete(oldRange.first, oldRange.rangeLen(), newRange.first)) {
            is DiffHookResult.Err -> return deleted
            is DiffHookResult.Ok -> {}
        }
        return d.finish()
    } else if (isEmptyRange(oldRange)) {
        when (val inserted = d.insert(oldRange.first, newRange.first, newRange.rangeLen())) {
            is DiffHookResult.Err -> return inserted
            is DiffHookResult.Ok -> {}
        }
        return d.finish()
    }

    val commonPrefixLen = commonPrefixLen(old, oldRange, new, newRange)
    val commonSuffixLen = commonSuffixLen(
        old,
        oldRange.first + commonPrefixLen until oldRange.exclusiveEnd(),
        new,
        newRange.first + commonPrefixLen until newRange.exclusiveEnd(),
    )

    if (commonPrefixLen == oldRange.rangeLen() && oldRange.rangeLen() == newRange.rangeLen()) {
        when (val equal = d.equal(0, 0, oldRange.rangeLen())) {
            is DiffHookResult.Err -> return equal
            is DiffHookResult.Ok -> {}
        }
        return d.finish()
    }

    val maybeTable = makeTable(
        old,
        commonPrefixLen until oldRange.rangeLen() - commonSuffixLen,
        new,
        commonPrefixLen until newRange.rangeLen() - commonSuffixLen,
        deadline,
    )
    var oldIdx = 0
    var newIdx = 0
    val newLen = newRange.rangeLen() - commonPrefixLen - commonSuffixLen
    val oldLen = oldRange.rangeLen() - commonPrefixLen - commonSuffixLen

    if (commonPrefixLen > 0) {
        when (val equal = d.equal(oldRange.first, newRange.first, commonPrefixLen)) {
            is DiffHookResult.Err -> return equal
            is DiffHookResult.Ok -> {}
        }
    }

    if (maybeTable != null) {
        while (newIdx < newLen && oldIdx < oldLen) {
            val oldOrigIdx = oldRange.first + commonPrefixLen + oldIdx
            val newOrigIdx = newRange.first + commonPrefixLen + newIdx

            if (new[newOrigIdx] == old[oldOrigIdx]) {
                when (val equal = d.equal(oldOrigIdx, newOrigIdx, 1)) {
                    is DiffHookResult.Err -> return equal
                    is DiffHookResult.Ok -> {}
                }
                oldIdx += 1
                newIdx += 1
            } else if (
                (maybeTable[newIdx to oldIdx + 1] ?: 0) >=
                (maybeTable[newIdx + 1 to oldIdx] ?: 0)
            ) {
                when (val deleted = d.delete(oldOrigIdx, 1, newOrigIdx)) {
                    is DiffHookResult.Err -> return deleted
                    is DiffHookResult.Ok -> {}
                }
                oldIdx += 1
            } else {
                when (val inserted = d.insert(oldOrigIdx, newOrigIdx, 1)) {
                    is DiffHookResult.Err -> return inserted
                    is DiffHookResult.Ok -> {}
                }
                newIdx += 1
            }
        }
    } else {
        val oldOrigIdx = oldRange.first + commonPrefixLen + oldIdx
        val newOrigIdx = newRange.first + commonPrefixLen + newIdx
        when (val deleted = d.delete(oldOrigIdx, oldLen, newOrigIdx)) {
            is DiffHookResult.Err -> return deleted
            is DiffHookResult.Ok -> {}
        }
        when (val inserted = d.insert(oldOrigIdx, newOrigIdx, newLen)) {
            is DiffHookResult.Err -> return inserted
            is DiffHookResult.Ok -> {}
        }
    }

    if (oldIdx < oldLen) {
        when (
            val deleted = d.delete(
                oldRange.first + commonPrefixLen + oldIdx,
                oldLen - oldIdx,
                newRange.first + commonPrefixLen + newIdx,
            )
        ) {
            is DiffHookResult.Err -> return deleted
            is DiffHookResult.Ok -> {}
        }
        oldIdx += oldLen - oldIdx
    }

    if (newIdx < newLen) {
        when (
            val inserted = d.insert(
                oldRange.first + commonPrefixLen + oldIdx,
                newRange.first + commonPrefixLen + newIdx,
                newLen - newIdx,
            )
        ) {
            is DiffHookResult.Err -> return inserted
            is DiffHookResult.Ok -> {}
        }
    }

    if (commonSuffixLen > 0) {
        when (
            val equal = d.equal(
                oldRange.first + oldLen + commonPrefixLen,
                newRange.first + newLen + commonPrefixLen,
                commonSuffixLen,
            )
        ) {
            is DiffHookResult.Err -> return equal
            is DiffHookResult.Ok -> {}
        }
    }

    return d.finish()
}

internal fun <T> makeTable(
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
    deadline: TimeMark?,
): MutableMap<Pair<Int, Int>, Int>? {
    val oldLen = oldRange.rangeLen()
    val newLen = newRange.rangeLen()
    val table = mutableMapOf<Pair<Int, Int>, Int>()

    for (i in (0 until newLen).reversed()) {
        if (deadlineExceeded(deadline)) {
            return null
        }

        for (j in (0 until oldLen).reversed()) {
            val value = if (new[i] == old[j]) {
                (table[i + 1 to j + 1] ?: 0) + 1
            } else {
                maxOf(table[i + 1 to j] ?: 0, table[i to j + 1] ?: 0)
            }
            if (value > 0) {
                table[i to j] = value
            }
        }
    }

    return table
}

internal fun IntRange.exclusiveEnd(): Int =
    if (last < first) {
        first
    } else {
        last + 1
    }

internal fun IntRange.rangeLen(): Int = exclusiveEnd() - first
