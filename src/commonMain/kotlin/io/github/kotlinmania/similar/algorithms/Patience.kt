// port-lint: source algorithms/patience.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.IndexLookup
import io.github.kotlinmania.similar.asLookup
import kotlin.time.TimeMark

/**
 * Patience diff algorithm.
 *
 * Diff `old`, between indices `oldRange` and `new` between indices `newRange`.
 */
fun <E, T> patienceDiff(
    d: DiffHook<E>,
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
): DiffHookResult<E> =
    patienceDiffDeadline(d, old, oldRange, new, newRange, null)

/**
 * Patience diff algorithm with deadline.
 *
 * Diff `old`, between indices `oldRange` and `new` between indices `newRange`.
 *
 * This diff is done with an optional deadline that defines the maximal
 * execution time permitted before it bails and falls back to an approximation.
 */
fun <E, T> patienceDiffDeadline(
    d: DiffHook<E>,
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
    deadline: TimeMark?,
): DiffHookResult<E> {
    val oldIndexes = unique(old, oldRange)
    val newIndexes = unique(new, newRange)

    val patience = PatienceHook(
        d = d,
        old = old,
        oldCurrent = oldRange.first,
        oldEnd = oldRange.exclusiveEnd(),
        oldIndexes = oldIndexes,
        new = new,
        newCurrent = newRange.first,
        newEnd = newRange.exclusiveEnd(),
        newIndexes = newIndexes,
        deadline = deadline,
    )
    val replace = Replace(patience)
    return myersDiffDeadline(
        replace,
        oldIndexes.asLookup(),
        oldIndexes.indices,
        newIndexes.asLookup(),
        newIndexes.indices,
        deadline,
    )
}

private class PatienceHook<E, T>(
    private val d: DiffHook<E>,
    private val old: IndexLookup<T>,
    private var oldCurrent: Int,
    private val oldEnd: Int,
    private val oldIndexes: List<UniqueItem<T>>,
    private val new: IndexLookup<T>,
    private var newCurrent: Int,
    private val newEnd: Int,
    private val newIndexes: List<UniqueItem<T>>,
    private val deadline: TimeMark?,
) : DiffHook<E> {
    override fun equal(oldIndex: Int, newIndex: Int, len: Int): DiffHookResult<E> {
        for ((oldUniqueIndex, newUniqueIndex) in (oldIndex until oldIndex + len).zip(newIndex until newIndex + len)) {
            val a0 = oldCurrent
            val b0 = newCurrent
            while (
                oldCurrent < oldIndexes[oldUniqueIndex].originalIndex() &&
                newCurrent < newIndexes[newUniqueIndex].originalIndex() &&
                new[newCurrent] == old[oldCurrent]
            ) {
                oldCurrent += 1
                newCurrent += 1
            }
            if (oldCurrent > a0) {
                when (val equal = d.equal(a0, b0, oldCurrent - a0)) {
                    is DiffHookResult.Err -> return equal
                    is DiffHookResult.Ok -> {}
                }
            }
            val noFinishD = NoFinishHook(d)
            when (
                val diffed = myersDiffDeadline(
                    noFinishD,
                    old,
                    oldCurrent until oldIndexes[oldUniqueIndex].originalIndex(),
                    new,
                    newCurrent until newIndexes[newUniqueIndex].originalIndex(),
                    deadline,
                )
            ) {
                is DiffHookResult.Err -> return diffed
                is DiffHookResult.Ok -> {}
            }
            oldCurrent = oldIndexes[oldUniqueIndex].originalIndex()
            newCurrent = newIndexes[newUniqueIndex].originalIndex()
        }
        return DiffHookResult.Ok
    }

    override fun finish(): DiffHookResult<E> =
        myersDiffDeadline(
            d,
            old,
            oldCurrent until oldEnd,
            new,
            newCurrent until newEnd,
            deadline,
        )
}
