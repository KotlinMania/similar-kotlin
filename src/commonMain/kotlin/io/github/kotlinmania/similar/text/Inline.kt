// port-lint: source text/inline.rs
package io.github.kotlinmania.similar.text

import io.github.kotlinmania.similar.Algorithm
import io.github.kotlinmania.similar.Change
import io.github.kotlinmania.similar.ChangeTag
import io.github.kotlinmania.similar.DiffOp
import io.github.kotlinmania.similar.DiffTag
import io.github.kotlinmania.similar.captureDiffSlicesDeadline
import io.github.kotlinmania.similar.getDiffRatio
import kotlin.time.TimeMark

private const val MIN_RATIO: Float = 0.5f

private data class MultiLookup(
    private val strings: List<String>,
    private val seqs: List<Triple<String, Int, Int>>,
) {
    companion object {
        fun new(strings: List<String>): MultiLookup {
            val seqs = mutableListOf<Triple<String, Int, Int>>()
            for ((stringIdx, string) in strings.withIndex()) {
                var offset = 0
                for (word in string.asDiffableStr().tokenizeWords()) {
                    seqs += Triple(word, stringIdx, offset)
                    offset += word.length
                }
            }
            return MultiLookup(strings, seqs)
        }
    }

    fun len(): Int = seqs.size

    fun getOriginalSlices(idx: Int, len: Int): List<Pair<Int, String>> {
        var last: Triple<Int, Int, Int>? = null
        val rv = mutableListOf<Pair<Int, String>>()

        for (offset in 0 until len) {
            val (slice, stringIdx, charIdx) = seqs[idx + offset]
            last = when (val current = last) {
                null -> Triple(stringIdx, charIdx, slice.length)
                else -> {
                    if (current.first == stringIdx) {
                        Triple(stringIdx, current.second, current.third + slice.length)
                    } else {
                        rv += current.first to strings[current.first].substring(current.second, current.second + current.third)
                        Triple(stringIdx, charIdx, slice.length)
                    }
                }
            }
        }

        val current = last
        if (current != null) {
            rv += current.first to strings[current.first].substring(current.second, current.second + current.third)
        }

        return rv
    }

    fun values(): List<String> = seqs.map { it.first }
}

private fun pushValues(v: MutableList<MutableList<Pair<Boolean, String>>>, idx: Int, emphasized: Boolean, s: String) {
    while (v.size < idx + 1) {
        v += mutableListOf<Pair<Boolean, String>>()
    }
    if (emphasized) {
        for (segment in s.asDiffableStr().tokenizeLinesAndNewlines()) {
            v[idx] += !segment.asDiffableStr().endsWithNewline() to segment
        }
    } else {
        v[idx] += false to s
    }
}

/** Represents the expanded textual change with inline highlights. */
data class InlineChange(
    private val tagValue: ChangeTag,
    private val oldIndexValue: Int?,
    private val newIndexValue: Int?,
    private val valuesValue: List<Pair<Boolean, String>>,
) {
    companion object {
        fun from(change: Change<String>): InlineChange =
            InlineChange(change.tag(), change.oldIndex(), change.newIndex(), listOf(false to change.value()))
    }

    /** Returns the change tag. */
    fun tag(): ChangeTag = tagValue

    /** Returns the old index if available. */
    fun oldIndex(): Int? = oldIndexValue

    /** Returns the new index if available. */
    fun newIndex(): Int? = newIndexValue

    /** Returns the changed values. */
    fun values(): List<Pair<Boolean, String>> = valuesValue

    /** Iterates over all potentially lossy UTF-8 decoded values. */
    fun iterStringsLossy(): Iterator<Pair<Boolean, String>> = values().iterator()

    /** Returns `true` if this change does not end in a newline. */
    fun missingNewline(): Boolean = values().lastOrNull()?.second?.asDiffableStr()?.endsWithNewline() != true

    override fun toString(): String {
        val rendered = buildString {
            for ((emphasized, value) in iterStringsLossy()) {
                val marker = if (!emphasized) {
                    ""
                } else {
                    when (tagValue) {
                        ChangeTag.Equal -> ""
                        ChangeTag.Delete -> "-"
                        ChangeTag.Insert -> "+"
                    }
                }
                append(marker)
                append(value)
                append(marker)
            }
        }
        return rendered + if (missingNewline()) "\n" else ""
    }
}

internal fun iterInlineChanges(diff: TextDiff, op: DiffOp, deadline: TimeMark?): Iterator<InlineChange> {
    val tuple = op.asTagTuple()

    if (tuple.tag == DiffTag.Equal || tuple.tag == DiffTag.Insert || tuple.tag == DiffTag.Delete) {
        return sequence {
            val iter = diff.iterChanges(op)
            while (iter.hasNext()) {
                yield(InlineChange.from(iter.next()))
            }
        }.iterator()
    }

    var oldIndex = tuple.oldRange.first
    var newIndex = tuple.newRange.first
    val oldSlices = diff.oldSlices().subList(tuple.oldRange.first, tuple.oldRange.exclusiveEnd())
    val newSlices = diff.newSlices().subList(tuple.newRange.first, tuple.newRange.exclusiveEnd())

    if (upperSeqRatio(oldSlices, newSlices) < MIN_RATIO) {
        return sequence {
            val iter = diff.iterChanges(op)
            while (iter.hasNext()) {
                yield(InlineChange.from(iter.next()))
            }
        }.iterator()
    }

    val oldLookup = MultiLookup.new(oldSlices)
    val newLookup = MultiLookup.new(newSlices)

    val ops = captureDiffSlicesDeadline(
        Algorithm.Patience,
        oldLookup.values(),
        newLookup.values(),
        deadline,
    )

    if (getDiffRatio(ops, oldLookup.len(), newLookup.len()) < MIN_RATIO) {
        return sequence {
            val iter = diff.iterChanges(op)
            while (iter.hasNext()) {
                yield(InlineChange.from(iter.next()))
            }
        }.iterator()
    }

    val oldValues = mutableListOf<MutableList<Pair<Boolean, String>>>()
    val newValues = mutableListOf<MutableList<Pair<Boolean, String>>>()

    for (diffOp in ops) {
        when (diffOp) {
            is DiffOp.Equal -> {
                for ((idx, slice) in oldLookup.getOriginalSlices(diffOp.oldIndex, diffOp.len)) {
                    pushValues(oldValues, idx, false, slice)
                }
                for ((idx, slice) in newLookup.getOriginalSlices(diffOp.newIndex, diffOp.len)) {
                    pushValues(newValues, idx, false, slice)
                }
            }

            is DiffOp.Delete -> {
                for ((idx, slice) in oldLookup.getOriginalSlices(diffOp.oldIndex, diffOp.oldLen)) {
                    pushValues(oldValues, idx, true, slice)
                }
            }

            is DiffOp.Insert -> {
                for ((idx, slice) in newLookup.getOriginalSlices(diffOp.newIndex, diffOp.newLen)) {
                    pushValues(newValues, idx, true, slice)
                }
            }

            is DiffOp.Replace -> {
                for ((idx, slice) in oldLookup.getOriginalSlices(diffOp.oldIndex, diffOp.oldLen)) {
                    pushValues(oldValues, idx, true, slice)
                }
                for ((idx, slice) in newLookup.getOriginalSlices(diffOp.newIndex, diffOp.newLen)) {
                    pushValues(newValues, idx, true, slice)
                }
            }
        }
    }

    val rv = mutableListOf<InlineChange>()

    for (values in oldValues) {
        rv += InlineChange(ChangeTag.Delete, oldIndex, null, values)
        oldIndex += 1
    }

    for (values in newValues) {
        rv += InlineChange(ChangeTag.Insert, null, newIndex, values)
        newIndex += 1
    }

    return rv.iterator()
}

private fun IntRange.exclusiveEnd(): Int =
    if (last < first) {
        first
    } else {
        last + 1
    }
