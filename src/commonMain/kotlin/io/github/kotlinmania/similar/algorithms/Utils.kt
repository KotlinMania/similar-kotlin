// port-lint: source src/algorithms/utils.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.IndexLookup

/**
 * Utility function to check if a range is empty that works on older rust versions
 */
fun isEmptyRange(range: IntRange): Boolean {
    return !(range.first <= range.last)
}

/**
 * Represents an item in the vector returned by [unique].
 *
 * It compares like the underlying item does it was created from but
 * carries the index it was originally created from.
 */
class UniqueItem<out T>(
    private val lookup: IndexLookup<T>,
    private val index: Int,
) {
    /** Returns the value. */
    fun value(): T = lookup[index]

    /** Returns the original index. */
    fun originalIndex(): Int = index

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UniqueItem<*>) return false
        return value() == other.value()
    }

    override fun hashCode(): Int = value()?.hashCode() ?: 0

    override fun toString(): String =
        "UniqueItem(value=${value()}, original_index=${originalIndex()})"
}

/**
 * Returns only unique items in the sequence as vector.
 *
 * Each item is wrapped in a [UniqueItem] so that both the value and the
 * index can be extracted.
 */
fun <T> unique(lookup: IndexLookup<T>, range: IntRange): List<UniqueItem<T>> {
    val byItem: MutableMap<T, Int?> = HashMap()
    for (index in range) {
        val value = lookup[index]
        if (!byItem.containsKey(value)) {
            byItem[value] = index
        } else {
            val existing = byItem[value]
            if (existing != null) {
                byItem[value] = null
            }
        }
    }
    val rv = byItem.values
        .filterNotNull()
        .map { UniqueItem(lookup, it) }
        .toMutableList()
    rv.sortBy { it.originalIndex() }
    return rv
}

/** Given two lookups and ranges calculates the length of the common prefix. */
fun <Old, New> commonPrefixLen(
    old: IndexLookup<Old>,
    oldRange: IntRange,
    new: IndexLookup<New>,
    newRange: IntRange,
): Int {
    if (isEmptyRange(oldRange) || isEmptyRange(newRange)) {
        return 0
    }
    var count = 0
    val newIter = newRange.iterator()
    val oldIter = oldRange.iterator()
    while (newIter.hasNext() && oldIter.hasNext()) {
        val n = newIter.nextInt()
        val o = oldIter.nextInt()
        if (new[n] != old[o]) {
            break
        }
        count++
    }
    return count
}

/** Given two lookups and ranges calculates the length of common suffix. */
fun <Old, New> commonSuffixLen(
    old: IndexLookup<Old>,
    oldRange: IntRange,
    new: IndexLookup<New>,
    newRange: IntRange,
): Int {
    if (isEmptyRange(oldRange) || isEmptyRange(newRange)) {
        return 0
    }
    var count = 0
    val newIter = newRange.reversed().iterator()
    val oldIter = oldRange.reversed().iterator()
    while (newIter.hasNext() && oldIter.hasNext()) {
        val n = newIter.nextInt()
        val o = oldIter.nextInt()
        if (new[n] != old[o]) {
            break
        }
        count++
    }
    return count
}

private class OffsetLookup<T>(
    val offset: Int,
    val vec: List<T>,
) : IndexLookup<T> {
    override fun get(index: Int): T = vec[index - offset]
}

/**
 * A utility struct to convert distinct items to unique integers.
 *
 * This can be helpful on larger inputs to speed up the comparisons
 * performed by doing a first pass where the data set gets reduced
 * to (small) integers.
 *
 * The idea is that instead of passing two sequences to a diffling algorithm
 * you first pass it via [IdentifyDistinct]:
 *
 * ```kotlin
 * import io.github.kotlinmania.similar.captureDiff
 * import io.github.kotlinmania.similar.algorithms.Algorithm
 * import io.github.kotlinmania.similar.algorithms.IdentifyDistinct
 *
 * val old = listOf("foo", "bar", "baz")
 * val new = listOf("foo", "blah", "baz")
 * val h = IdentifyDistinct(old.asLookup(), 0 until old.size, new.asLookup(), 0 until new.size)
 * val ops = captureDiff(
 *     Algorithm.Myers,
 *     h.oldLookup(),
 *     h.oldRange(),
 *     h.newLookup(),
 *     h.newRange(),
 * )
 * ```
 *
 * The indexes are the same as with the passed source ranges.
 */
class IdentifyDistinct private constructor(
    private val old: OffsetLookup<Int>,
    private val new: OffsetLookup<Int>,
) {
    /** Returns a lookup for the old side. */
    fun oldLookup(): IndexLookup<Int> = old

    /** Returns a lookup for the new side. */
    fun newLookup(): IndexLookup<Int> = new

    /** Convenience method to get back the old range. */
    fun oldRange(): IntRange = old.offset until old.offset + old.vec.size

    /** Convenience method to get back the new range. */
    fun newRange(): IntRange = new.offset until new.offset + new.vec.size

    companion object {
        /** Creates an int hasher for two sequences. */
        operator fun <T> invoke(
            old: IndexLookup<T>,
            oldRange: IntRange,
            new: IndexLookup<T>,
            newRange: IntRange,
        ): IdentifyDistinct {
            val map: MutableMap<T, Int> = HashMap()
            val oldSeq = ArrayList<Int>()
            val newSeq = ArrayList<Int>()
            var nextId = 0
            val step = 1
            val oldStart = oldRange.first
            val newStart = newRange.first

            for (idx in oldRange) {
                val item = old[idx]
                val id = map.getOrPut(item) {
                    val current = nextId
                    nextId += step
                    current
                }
                oldSeq.add(id)
            }

            for (idx in newRange) {
                val item = new[idx]
                val id = map.getOrPut(item) {
                    val current = nextId
                    nextId += step
                    current
                }
                newSeq.add(id)
            }

            return IdentifyDistinct(
                OffsetLookup(offset = oldStart, vec = oldSeq),
                OffsetLookup(offset = newStart, vec = newSeq),
            )
        }
    }
}
