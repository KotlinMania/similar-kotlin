// port-lint: source algorithms/lcs.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.DiffOp
import io.github.kotlinmania.similar.asLookup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LcsTest {
    @Test
    fun testTable() {
        val table = makeTable(
            listOf(2, 3).asLookup(),
            0 until 2,
            listOf(0, 1, 2).asLookup(),
            0 until 3,
            null,
        )

        val expected = mapOf<Pair<Int, Int>, Int>(
            (1 to 0) to 1,
            (0 to 0) to 1,
            (2 to 0) to 1,
        )
        assertEquals(expected, checkNotNull(table))
    }

    @Test
    fun testDiff() {
        val old = listOf(0, 1, 2, 3, 4)
        val new = listOf(0, 1, 2, 9, 4)
        val d = Replace.new(Capture())

        lcsDiff(d, old.asLookup(), old.indices, new.asLookup(), new.indices)

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
        val d = Replace.new(Capture())

        lcsDiff(d, old.asLookup(), old.indices, new.asLookup(), new.indices)

        assertEquals(new, applyOps(old, new, d.intoInner().ops()))
    }

    @Test
    fun testPat() {
        val old = listOf(0, 1, 3, 4, 5)
        val new = listOf(0, 1, 4, 5, 8, 9)
        val d = Capture()

        lcsDiff(d, old.asLookup(), old.indices, new.asLookup(), new.indices)

        assertEquals(new, applyOps(old, new, d.ops()))
    }

    @Test
    fun testSame() {
        val old = listOf(0, 1, 2, 3, 4, 4, 4, 5)
        val d = Capture()

        lcsDiff(d, old.asLookup(), old.indices, old.asLookup(), old.indices)

        assertEquals(listOf(DiffOp.Equal(0, 0, old.size)), d.ops())
    }

    @Test
    fun testFinishCalled() {
        val slice = listOf(1, 2)
        val slice2 = listOf(1, 2, 3)
        val d = LcsFinishHook()

        lcsDiff(d, slice.asLookup(), slice.indices, slice2.asLookup(), slice2.indices)
        assertTrue(d.finished)

        val same = LcsFinishHook()
        lcsDiff(same, slice.asLookup(), slice.indices, slice.asLookup(), slice.indices)
        assertTrue(same.finished)

        val empty = LcsFinishHook()
        lcsDiff(empty, emptyList<Int>().asLookup(), 0 until 0, emptyList<Int>().asLookup(), 0 until 0)
        assertTrue(empty.finished)
    }

    @Test
    fun testBadRangeRegression() {
        val d = Capture()

        lcsDiff(d, listOf(0).asLookup(), 0 until 1, listOf(0, 0).asLookup(), 0 until 2)

        assertEquals(
            listOf(
                DiffOp.Equal(0, 0, 1),
                DiffOp.Insert(1, 1, 1),
            ),
            d.intoOps(),
        )
    }
}

private class LcsFinishHook : DiffHook<Nothing> {
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
