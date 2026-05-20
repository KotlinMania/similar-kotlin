// port-lint: source utils.rs
package io.github.kotlinmania.similar

import io.github.kotlinmania.similar.text.TextDiff

private data class SliceRemapper(
    private val source: String,
    private val indexes: List<Pair<Int, Int>>,
) {
    companion object {
        fun new(source: String, slices: List<String>): SliceRemapper {
            var offset = 0
            val indexes = slices.map { item ->
                val start = offset
                offset += item.length
                start to offset
            }
            return SliceRemapper(source, indexes)
        }
    }

    fun slice(range: IntRange): String? {
        val end = range.exclusiveEnd()
        if (range.first < 0 || end < range.first || end > indexes.size) {
            return null
        }
        if (range.first == end) {
            val start = if (range.first == indexes.size) {
                source.length
            } else {
                indexes.getOrNull(range.first)?.first ?: return null
            }
            return source.substring(start, start)
        }
        val start = indexes.getOrNull(range.first)?.first ?: return null
        val stop = indexes.getOrNull(end - 1)?.second ?: return null
        return source.substring(start, stop)
    }
}

/**
 * A remapper that can remap diff operations to the original strings.
 *
 * When a [TextDiff] is created from strings and internal tokenization is used,
 * this remapper takes a range in the tokenized sequences and maps it back to
 * the original input text.
 */
class TextDiffRemapper private constructor(
    private val old: SliceRemapper,
    private val new: SliceRemapper,
) {
    companion object {
        /** Creates a new remapper from strings and slices. */
        fun new(
            oldSlices: List<String>,
            newSlices: List<String>,
            old: String,
            new: String,
        ): TextDiffRemapper =
            TextDiffRemapper(
                old = SliceRemapper.new(old, oldSlices),
                new = SliceRemapper.new(new, newSlices),
            )

        /** Creates a new remapper from a text diff and the original strings. */
        fun fromTextDiff(diff: TextDiff, old: String, new: String): TextDiffRemapper =
            TextDiffRemapper(
                old = SliceRemapper.new(old, diff.oldSlices()),
                new = SliceRemapper.new(new, diff.newSlices()),
            )
    }

    /** Slices into the old string. */
    fun sliceOld(range: IntRange): String? = old.slice(range)

    /** Slices into the new string. */
    fun sliceNew(range: IntRange): String? = new.slice(range)

    /** Given a diff operation, yields the changes it encodes against the original strings. */
    fun iterSlices(op: DiffOp): List<Pair<ChangeTag, String>> =
        when (op) {
            is DiffOp.Equal -> listOf(
                ChangeTag.Equal to requireNotNull(old.slice(op.oldIndex until op.oldIndex + op.len)) {
                    "slice out of bounds"
                },
            )

            is DiffOp.Insert -> listOf(
                ChangeTag.Insert to requireNotNull(new.slice(op.newIndex until op.newIndex + op.newLen)) {
                    "slice out of bounds"
                },
            )

            is DiffOp.Delete -> listOf(
                ChangeTag.Delete to requireNotNull(old.slice(op.oldIndex until op.oldIndex + op.oldLen)) {
                    "slice out of bounds"
                },
            )

            is DiffOp.Replace -> listOf(
                ChangeTag.Delete to requireNotNull(old.slice(op.oldIndex until op.oldIndex + op.oldLen)) {
                    "slice out of bounds"
                },
                ChangeTag.Insert to requireNotNull(new.slice(op.newIndex until op.newIndex + op.newLen)) {
                    "slice out of bounds"
                },
            )
        }
}

/** Shortcut for diffing two slices. */
fun <T : Comparable<T>> diffSlices(
    alg: Algorithm,
    old: List<T>,
    new: List<T>,
): List<Pair<ChangeTag, List<T>>> =
    captureDiffSlices(alg, old, new)
        .flatMap { op -> op.iterSlices(old, new).toList() }

/** Shortcut for making a character-level diff. */
fun diffChars(alg: Algorithm, old: String, new: String): List<Pair<ChangeTag, String>> {
    val diff = TextDiff.configure().algorithm(alg).diffChars(old, new)
    val remapper = TextDiffRemapper.fromTextDiff(diff, old, new)
    return diff.ops().flatMap { remapper.iterSlices(it) }
}

/** Shortcut for making a word-level diff. */
fun diffWords(alg: Algorithm, old: String, new: String): List<Pair<ChangeTag, String>> {
    val diff = TextDiff.configure().algorithm(alg).diffWords(old, new)
    val remapper = TextDiffRemapper.fromTextDiff(diff, old, new)
    return diff.ops().flatMap { remapper.iterSlices(it) }
}

/** Shortcut for making a unicode word-level diff. */
fun diffUnicodeWords(alg: Algorithm, old: String, new: String): List<Pair<ChangeTag, String>> {
    val diff = TextDiff.configure().algorithm(alg).diffUnicodeWords(old, new)
    val remapper = TextDiffRemapper.fromTextDiff(diff, old, new)
    return diff.ops().flatMap { remapper.iterSlices(it) }
}

/** Shortcut for making a grapheme-level diff. */
fun diffGraphemes(alg: Algorithm, old: String, new: String): List<Pair<ChangeTag, String>> {
    val diff = TextDiff.configure().algorithm(alg).diffGraphemes(old, new)
    val remapper = TextDiffRemapper.fromTextDiff(diff, old, new)
    return diff.ops().flatMap { remapper.iterSlices(it) }
}

/** Shortcut for making a line diff. */
fun diffLines(alg: Algorithm, old: String, new: String): List<Pair<ChangeTag, String>> {
    val changes = mutableListOf<Pair<ChangeTag, String>>()
    val iter = TextDiff.configure().algorithm(alg).diffLines(old, new).iterAllChanges()
    while (iter.hasNext()) {
        val change = iter.next()
        changes += change.tag() to change.value()
    }
    return changes
}

private fun IntRange.exclusiveEnd(): Int =
    if (last < first) {
        first
    } else {
        last + 1
    }
