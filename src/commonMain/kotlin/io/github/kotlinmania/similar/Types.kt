// port-lint: source types.rs
package io.github.kotlinmania.similar

import io.github.kotlinmania.similar.algorithms.DiffHook
import io.github.kotlinmania.similar.algorithms.DiffHookResult
import io.github.kotlinmania.similar.algorithms.isEmptyRange

/** An enum representing a diffing algorithm. */
enum class Algorithm {
    /** Picks the Myers algorithm. */
    Myers,

    /** Picks the patience algorithm. */
    Patience,

    /** Picks the LCS algorithm. */
    Lcs,
}

/** The tag of a change. */
enum class ChangeTag(private val sign: Char) {
    /** The change indicates equality, not a change. */
    Equal(' '),

    /** The change indicates deleted text. */
    Delete('-'),

    /** The change indicates inserted text. */
    Insert('+');

    override fun toString(): String = sign.toString()
}

/**
 * Represents the expanded [DiffOp] change.
 *
 * This type is returned from [DiffOp.iterChanges] and `TextDiff.iterChanges`.
 *
 * It exists so that it is more convenient to work with textual differences as
 * the underlying [DiffOp] encodes a group of changes.
 *
 * This type has additional methods that are only available for types
 * implementing the text diffable interface.
 */
data class Change<T>(
    private val tagValue: ChangeTag,
    private val oldIndexValue: Int?,
    private val newIndexValue: Int?,
    private var valueData: T,
) {
    /** Returns the change tag. */
    fun tag(): ChangeTag = tagValue

    /** Returns the old index if available. */
    fun oldIndex(): Int? = oldIndexValue

    /** Returns the new index if available. */
    fun newIndex(): Int? = newIndexValue

    /**
     * Returns the underlying changed value.
     *
     * Depending on the type of the underlying text-diffable value this value is
     * more or less useful. If you always want to have a UTF-8 string it is best
     * to use the text-specific string helpers.
     */
    fun value(): T = valueData

    /** Returns the underlying changed value as reference. */
    fun valueRef(): T = valueData

    /** Replaces the underlying changed value and returns it. */
    fun valueMut(value: T): T {
        valueData = value
        return valueData
    }

    /** Returns the value as string if it is UTF-8 text. */
    fun asStr(): String? = valueData as? String

    /** Returns the value decoded lossily as UTF-8 string. */
    fun toStringLossy(): String = valueData.toString()

    /**
     * Returns `true` if this change does not end in a newline and must be
     * followed up by one if line based diffs are used.
     */
    fun missingNewline(): Boolean =
        when (val value = valueData) {
            is String -> !value.endsWith('\r') && !value.endsWith('\n')
            else -> false
        }

    override fun toString(): String =
        toStringLossy() + if (missingNewline()) "\n" else ""
}

/** Utility enum to capture a diff operation. */
sealed class DiffOp {
    /** The diff op encodes an equal segment. */
    data class Equal(
        /** The starting index in the old sequence. */
        var oldIndex: Int,
        /** The starting index in the new sequence. */
        var newIndex: Int,
        /** The length of the segment. */
        var len: Int,
    ) : DiffOp()

    /** A segment was deleted. */
    data class Delete(
        /** The starting index in the old sequence. */
        var oldIndex: Int,
        /** The length of the old segment. */
        var oldLen: Int,
        /** The starting index in the new sequence. */
        var newIndex: Int,
    ) : DiffOp()

    /** A segment was inserted. */
    data class Insert(
        /** The starting index in the old sequence. */
        var oldIndex: Int,
        /** The starting index in the new sequence. */
        var newIndex: Int,
        /** The length of the new segment. */
        var newLen: Int,
    ) : DiffOp()

    /** A segment was replaced. */
    data class Replace(
        /** The starting index in the old sequence. */
        var oldIndex: Int,
        /** The length of the old segment. */
        var oldLen: Int,
        /** The starting index in the new sequence. */
        var newIndex: Int,
        /** The length of the new segment. */
        var newLen: Int,
    ) : DiffOp()

    /** Returns the tag of the operation. */
    fun tag(): DiffTag = asTagTuple().tag

    /** Returns the old range. */
    fun oldRange(): IntRange = asTagTuple().oldRange

    /** Returns the new range. */
    fun newRange(): IntRange = asTagTuple().newRange

    /**
     * Transform the op into a tuple of diff tag and ranges.
     *
     * This is useful when operating on slices. The returned format is
     * `(tag, i1 until i2, j1 until j2)`:
     *
     * * `Replace`: `a[i1 until i2]` should be replaced by `b[j1 until j2]`.
     * * `Delete`: `a[i1 until i2]` should be deleted (`j1 == j2` in this case).
     * * `Insert`: `b[j1 until j2]` should be inserted at `a[i1 until i2]` (`i1 == i2` in this case).
     * * `Equal`: `a[i1 until i2]` is equal to `b[j1 until j2]`.
     */
    fun asTagTuple(): DiffOpTuple =
        when (this) {
            is Equal -> DiffOpTuple(
                DiffTag.Equal,
                oldIndex until oldIndex + len,
                newIndex until newIndex + len,
            )

            is Delete -> DiffOpTuple(
                DiffTag.Delete,
                oldIndex until oldIndex + oldLen,
                newIndex until newIndex,
            )

            is Insert -> DiffOpTuple(
                DiffTag.Insert,
                oldIndex until oldIndex,
                newIndex until newIndex + newLen,
            )

            is Replace -> DiffOpTuple(
                DiffTag.Replace,
                oldIndex until oldIndex + oldLen,
                newIndex until newIndex + newLen,
            )
        }

    /** Apply this operation to a diff hook. */
    fun <E> applyToHook(d: DiffHook<E>): DiffHookResult<E> =
        when (this) {
            is Equal -> d.equal(oldIndex, newIndex, len)
            is Delete -> d.delete(oldIndex, oldLen, newIndex)
            is Insert -> d.insert(oldIndex, newIndex, newLen)
            is Replace -> d.replace(oldIndex, oldLen, newIndex, newLen)
        }

    /**
     * Iterates over all changes encoded in the diff op against old and new
     * sequences.
     *
     * `old` and `new` are two indexable objects like the types you pass to the
     * diffing algorithm functions.
     */
    fun <T> iterChanges(old: IndexLookup<T>, new: IndexLookup<T>): ChangesIter<T> =
        ChangesIter(old, new, this)

    /**
     * Given a diff op yields the changes it encodes against the given slices.
     *
     * This is similar to [iterChanges] but instead of yielding the individual
     * changes it yields consecutive changed slices.
     *
     * This will only ever yield a single tuple or two tuples in case a
     * [DiffOp.Replace] operation is passed.
     */
    fun <T> iterSlices(old: List<T>, new: List<T>): Sequence<Pair<ChangeTag, List<T>>> =
        when (this) {
            is Equal -> sequenceOf(ChangeTag.Equal to old.subList(oldIndex, oldIndex + len))
            is Insert -> sequenceOf(ChangeTag.Insert to new.subList(newIndex, newIndex + newLen))
            is Delete -> sequenceOf(ChangeTag.Delete to old.subList(oldIndex, oldIndex + oldLen))
            is Replace -> sequenceOf(
                ChangeTag.Delete to old.subList(oldIndex, oldIndex + oldLen),
                ChangeTag.Insert to new.subList(newIndex, newIndex + newLen),
            )
        }

    internal fun isEmpty(): Boolean {
        val (_, old, new) = asTagTuple()
        return isEmptyRange(old) && isEmptyRange(new)
    }

    internal fun shiftLeft(adjust: Int) {
        adjust(adjust to true, 0 to false)
    }

    internal fun shiftRight(adjust: Int) {
        adjust(adjust to false, 0 to false)
    }

    internal fun growLeft(adjust: Int) {
        adjust(adjust to true, adjust to false)
    }

    internal fun growRight(adjust: Int) {
        adjust(0 to false, adjust to false)
    }

    internal fun shrinkLeft(adjust: Int) {
        adjust(0 to false, adjust to true)
    }

    internal fun shrinkRight(adjust: Int) {
        adjust(adjust to false, adjust to true)
    }

    private fun modify(value: Int, adjustment: Pair<Int, Boolean>): Int =
        if (adjustment.second) {
            value - adjustment.first
        } else {
            value + adjustment.first
        }

    private fun adjust(adjustOffset: Pair<Int, Boolean>, adjustLen: Pair<Int, Boolean>) {
        when (this) {
            is Equal -> {
                oldIndex = modify(oldIndex, adjustOffset)
                newIndex = modify(newIndex, adjustOffset)
                len = modify(len, adjustLen)
            }

            is Delete -> {
                oldIndex = modify(oldIndex, adjustOffset)
                oldLen = modify(oldLen, adjustLen)
                newIndex = modify(newIndex, adjustOffset)
            }

            is Insert -> {
                oldIndex = modify(oldIndex, adjustOffset)
                newIndex = modify(newIndex, adjustOffset)
                newLen = modify(newLen, adjustLen)
            }

            is Replace -> {
                oldIndex = modify(oldIndex, adjustOffset)
                oldLen = modify(oldLen, adjustLen)
                newIndex = modify(newIndex, adjustOffset)
                newLen = modify(newLen, adjustLen)
            }
        }
    }
}

/** The tag of a diff operation. */
enum class DiffTag {
    /** The diff op encodes an equal segment. */
    Equal,

    /** The diff op encodes a deleted segment. */
    Delete,

    /** The diff op encodes an inserted segment. */
    Insert,

    /** The diff op encodes a replaced segment. */
    Replace,
}

/** Tuple returned by [DiffOp.asTagTuple]. */
data class DiffOpTuple(
    val tag: DiffTag,
    val oldRange: IntRange,
    val newRange: IntRange,
)
