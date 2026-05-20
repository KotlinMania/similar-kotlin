// port-lint: source algorithms/myers.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.IndexLookup
import io.github.kotlinmania.similar.deadlineExceeded
import kotlin.math.abs
import kotlin.time.TimeMark

/**
 * Myers' diff algorithm.
 *
 * Diff `old`, between indices `oldRange` and `new` between indices `newRange`.
 */
fun <E, T> myersDiff(
    d: DiffHook<E>,
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
): DiffHookResult<E> =
    myersDiffDeadline(d, old, oldRange, new, newRange, null)

/**
 * Myers' diff algorithm with deadline.
 *
 * Diff `old`, between indices `oldRange` and `new` between indices `newRange`.
 *
 * This diff is done with an optional deadline that defines the maximal
 * execution time permitted before it bails and falls back to an approximation.
 */
fun <E, T> myersDiffDeadline(
    d: DiffHook<E>,
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
    deadline: TimeMark?,
): DiffHookResult<E> {
    val maxD = maxD(oldRange.rangeLen(), newRange.rangeLen())
    val vb = V(maxD)
    val vf = V(maxD)
    when (val conquered = conquer(d, old, oldRange, new, newRange, vf, vb, deadline)) {
        is DiffHookResult.Err -> return conquered
        is DiffHookResult.Ok -> {}
    }
    return d.finish()
}

internal class V(maxD: Int) {
    private val offset = maxD
    private val values = MutableList(2 * maxD) { 0 }

    fun len(): Int = values.size

    operator fun get(index: Int): Int = values[index + offset]

    operator fun set(index: Int, value: Int) {
        values[index + offset] = value
    }
}

internal fun maxD(len1: Int, len2: Int): Int = (len1 + len2 + 1) / 2 + 1

private fun splitAt(range: IntRange, at: Int): Pair<IntRange, IntRange> =
    range.first until at to (at until range.exclusiveEnd())

internal fun <T> findMiddleSnake(
    old: IndexLookup<T>,
    oldRange: IntRange,
    new: IndexLookup<T>,
    newRange: IntRange,
    vf: V,
    vb: V,
    deadline: TimeMark?,
): Pair<Int, Int>? {
    val n = oldRange.rangeLen()
    val m = newRange.rangeLen()

    val delta = n - m
    val odd = (delta and 1) == 1

    vf[1] = 0
    vb[1] = 0

    val dMax = maxD(n, m)
    check(vf.len() >= dMax)
    check(vb.len() >= dMax)

    for (d in 0 until dMax) {
        if (deadlineExceeded(deadline)) {
            break
        }

        for (k in d downTo -d step 2) {
            var x = if (k == -d || (k != d && vf[k - 1] < vf[k + 1])) {
                vf[k + 1]
            } else {
                vf[k - 1] + 1
            }
            val y = x - k

            val x0 = x
            val y0 = y
            if (x < oldRange.rangeLen() && y < newRange.rangeLen()) {
                val advance = commonPrefixLen(
                    old,
                    oldRange.first + x until oldRange.exclusiveEnd(),
                    new,
                    newRange.first + y until newRange.exclusiveEnd(),
                )
                x += advance
            }

            vf[k] = x

            if (odd && abs(k - delta) <= d - 1) {
                if (vf[k] + vb[-(k - delta)] >= n) {
                    return x0 + oldRange.first to y0 + newRange.first
                }
            }
        }

        for (k in d downTo -d step 2) {
            var x = if (k == -d || (k != d && vb[k - 1] < vb[k + 1])) {
                vb[k + 1]
            } else {
                vb[k - 1] + 1
            }
            var y = x - k

            if (x < n && y < m) {
                val advance = commonSuffixLen(
                    old,
                    oldRange.first until oldRange.first + n - x,
                    new,
                    newRange.first until newRange.first + m - y,
                )
                x += advance
                y += advance
            }

            vb[k] = x

            if (!odd && abs(k - delta) <= d) {
                if (vb[k] + vf[-(k - delta)] >= n) {
                    return n - x + oldRange.first to m - y + newRange.first
                }
            }
        }
    }

    return null
}

private fun <E, T> conquer(
    d: DiffHook<E>,
    old: IndexLookup<T>,
    oldInputRange: IntRange,
    new: IndexLookup<T>,
    newInputRange: IntRange,
    vf: V,
    vb: V,
    deadline: TimeMark?,
): DiffHookResult<E> {
    var oldStart = oldInputRange.first
    var oldEnd = oldInputRange.exclusiveEnd()
    var newStart = newInputRange.first
    var newEnd = newInputRange.exclusiveEnd()

    val commonPrefixLen = commonPrefixLen(old, oldStart until oldEnd, new, newStart until newEnd)
    if (commonPrefixLen > 0) {
        when (val equal = d.equal(oldStart, newStart, commonPrefixLen)) {
            is DiffHookResult.Err -> return equal
            is DiffHookResult.Ok -> {}
        }
    }
    oldStart += commonPrefixLen
    newStart += commonPrefixLen

    val commonSuffixLen = commonSuffixLen(old, oldStart until oldEnd, new, newStart until newEnd)
    val commonSuffix = oldEnd - commonSuffixLen to newEnd - commonSuffixLen
    oldEnd -= commonSuffixLen
    newEnd -= commonSuffixLen

    val oldRange = oldStart until oldEnd
    val newRange = newStart until newEnd

    if (!isEmptyRange(oldRange) || !isEmptyRange(newRange)) {
        if (isEmptyRange(newRange)) {
            when (val deleted = d.delete(oldRange.first, oldRange.rangeLen(), newRange.first)) {
                is DiffHookResult.Err -> return deleted
                is DiffHookResult.Ok -> {}
            }
        } else if (isEmptyRange(oldRange)) {
            when (val inserted = d.insert(oldRange.first, newRange.first, newRange.rangeLen())) {
                is DiffHookResult.Err -> return inserted
                is DiffHookResult.Ok -> {}
            }
        } else {
            val middle = findMiddleSnake(old, oldRange, new, newRange, vf, vb, deadline)
            if (middle != null) {
                val (xStart, yStart) = middle
                val (oldA, oldB) = splitAt(oldRange, xStart)
                val (newA, newB) = splitAt(newRange, yStart)
                when (val left = conquer(d, old, oldA, new, newA, vf, vb, deadline)) {
                    is DiffHookResult.Err -> return left
                    is DiffHookResult.Ok -> {}
                }
                when (val right = conquer(d, old, oldB, new, newB, vf, vb, deadline)) {
                    is DiffHookResult.Err -> return right
                    is DiffHookResult.Ok -> {}
                }
            } else {
                when (val deleted = d.delete(oldRange.first, oldRange.rangeLen(), newRange.first)) {
                    is DiffHookResult.Err -> return deleted
                    is DiffHookResult.Ok -> {}
                }
                when (val inserted = d.insert(oldRange.first, newRange.first, newRange.rangeLen())) {
                    is DiffHookResult.Err -> return inserted
                    is DiffHookResult.Ok -> {}
                }
            }
        }
    }

    if (commonSuffixLen > 0) {
        when (val equal = d.equal(commonSuffix.first, commonSuffix.second, commonSuffixLen)) {
            is DiffHookResult.Err -> return equal
            is DiffHookResult.Ok -> {}
        }
    }

    return DiffHookResult.Ok
}
