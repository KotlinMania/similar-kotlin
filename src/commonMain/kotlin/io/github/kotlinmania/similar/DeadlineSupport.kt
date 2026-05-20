// port-lint: source deadline_support.rs
package io.github.kotlinmania.similar

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Checks if a deadline was exeeded.
 */
fun deadlineExceeded(deadline: TimeMark?): Boolean {
    return when (deadline) {
        null -> false
        else -> deadline.hasPassedNow()
    }
}

/**
 * Converst a duration into a deadline.  This can be a noop on wasm
 */
fun durationToDeadline(add: Duration): TimeMark? {
    return TimeSource.Monotonic.markNow() + add
}
