// port-lint: source algorithms/myers.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.DiffOp
import io.github.kotlinmania.similar.asLookup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.time.toDuration

class MyersTest {
    @Test
    fun testFindMiddleSnake() {
        val old = "ABCABBA".encodeToByteArray().asList()
        val new = "CBABAC".encodeToByteArray().asList()
        val maxD = maxD(old.size, new.size)
        val vf = V(maxD)
        val vb = V(maxD)

        assertEquals(
            4 to 1,
            findMiddleSnake(old.asLookup(), old.indices, new.asLookup(), new.indices, vf, vb, null),
        )
    }

    @Test
    fun testDiff() {
        val old = listOf(0, 1, 2, 3, 4)
        val new = listOf(0, 1, 2, 9, 4)
        val d = Replace(Capture())

        myersDiff(d, old.asLookup(), old.indices, new.asLookup(), new.indices)

        assertEquals(
            listOf(
                DiffOp.Equal(0, 0, 3),
                DiffOp.Replace(3, 1, 3, 1),
                DiffOp.Equal(4, 4, 1),
            ),
            d.intoInner().ops(),
        )
    }

    @Test
    fun testContiguous() {
        val old = listOf(0, 1, 2, 3, 4, 4, 4, 5)
        val new = listOf(0, 1, 2, 8, 9, 4, 4, 7)
        val d = Replace(Capture())

        myersDiff(d, old.asLookup(), old.indices, new.asLookup(), new.indices)

        assertEquals(new, applyOps(old, new, d.intoInner().ops()))
    }

    @Test
    fun testPat() {
        val old = listOf(0, 1, 3, 4, 5)
        val new = listOf(0, 1, 4, 5, 8, 9)
        val d = Capture()

        myersDiff(d, old.asLookup(), old.indices, new.asLookup(), new.indices)

        assertEquals(
            listOf(
                DiffOp.Equal(0, 0, 2),
                DiffOp.Delete(2, 1, 2),
                DiffOp.Equal(3, 2, 2),
                DiffOp.Insert(5, 4, 1),
                DiffOp.Insert(5, 5, 1),
            ),
            d.ops(),
        )
    }

    @Test
    fun testDeadlineReached() {
        val old = (0 until 100).toList()
        val new = old.toMutableList().also {
            it[10] = 99
            it[25] = 99
            it[50] = 99
        }
        val d = Replace(Capture())

        myersDiffDeadline(
            d,
            old.asLookup(),
            old.indices,
            new.asLookup(),
            new.indices,
            TimeSource.Monotonic.markNow() - 1.toDuration(DurationUnit.MILLISECONDS),
        )

        assertEquals(new, applyOps(old, new, d.intoInner().ops()))
    }

    @Test
    fun testFinishCalled() {
        val slice = listOf(1, 2)
        val slice2 = listOf(1, 2, 3)
        val d = FinishHook()

        myersDiff(d, slice.asLookup(), slice.indices, slice2.asLookup(), slice2.indices)
        assertTrue(d.finished)

        val same = FinishHook()
        myersDiff(same, slice.asLookup(), slice.indices, slice.asLookup(), slice.indices)
        assertTrue(same.finished)

        val empty = FinishHook()
        myersDiff(empty, emptyList<Int>().asLookup(), 0 until 0, emptyList<Int>().asLookup(), 0 until 0)
        assertTrue(empty.finished)
    }
}

private class FinishHook : DiffHook<Nothing> {
    var finished: Boolean = false

    override fun finish(): DiffHookResult<Nothing> {
        finished = true
        return DiffHookResult.Ok
    }
}

private fun <T> applyOps(old: List<T>, new: List<T>, ops: List<DiffOp>): List<T> =
    buildList {
        for (op in ops) {
            when (op) {
                is DiffOp.Equal -> addAll(old.subList(op.oldIndex, op.oldIndex + op.len))
                is DiffOp.Delete -> {}
                is DiffOp.Insert -> addAll(new.subList(op.newIndex, op.newIndex + op.newLen))
                is DiffOp.Replace -> addAll(new.subList(op.newIndex, op.newIndex + op.newLen))
            }
        }
    }
