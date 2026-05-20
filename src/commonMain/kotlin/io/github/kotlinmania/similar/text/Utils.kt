// port-lint: source text/utils.rs
package io.github.kotlinmania.similar.text

/** Quick and dirty way to get an upper sequence ratio. */
fun <T> upperSeqRatio(seq1: List<T>, seq2: List<T>): Float {
    val n = seq1.size + seq2.size
    return if (n == 0) {
        1.0f
    } else {
        2.0f * minOf(seq1.size, seq2.size).toFloat() / n.toFloat()
    }
}

/**
 * Internal utility to calculate an upper bound for a ratio for
 * `getCloseMatches`. This is based on Python's difflib approach
 * of considering the two sets to be multisets.
 *
 * It counts the number of matches without regard to order, which is an
 * obvious upper bound.
 */
class QuickSeqRatio<T>(seq: List<T>) {
    private val counts: Map<T, Int> =
        buildMap {
            for (word in seq) {
                put(word, (get(word) ?: 0) + 1)
            }
        }

    fun calc(seq: List<T>): Float {
        val n = counts.size + seq.size
        if (n == 0) {
            return 1.0f
        }

        val available = mutableMapOf<T, Int>()
        var matches = 0
        for (word in seq) {
            val count = available[word] ?: counts[word] ?: 0
            available[word] = count - 1
            if (count > 0) {
                matches += 1
            }
        }

        return 2.0f * matches.toFloat() / n.toFloat()
    }
}
