// port-lint: source algorithms/capture.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.Algorithm
import io.github.kotlinmania.similar.DiffOp
import kotlin.test.Test
import kotlin.test.assertEquals

class CaptureTest {
    @Test
    fun testCaptureHookGrouping() {
        val old = (1 until 100).toList()
        val new = old.toMutableList()
        new[10] = 1000
        new[13] = 1000
        new[16] = 1000
        new[34] = 1000

        val d = Replace.new(Capture())
        diffSlices(Algorithm.Myers, d, old, new)

        val groups = d.intoInner().intoGroupedOps(3)
        assertEquals(2, groups.size)
        assertEquals(
            listOf(
                DiffOp.Equal(7, 7, 3),
                DiffOp.Replace(10, 1, 10, 1),
                DiffOp.Equal(11, 11, 2),
                DiffOp.Replace(13, 1, 13, 1),
                DiffOp.Equal(14, 14, 2),
                DiffOp.Replace(16, 1, 16, 1),
                DiffOp.Equal(17, 17, 3),
            ),
            groups.first(),
        )
        assertEquals(
            listOf(
                DiffOp.Equal(31, 31, 3),
                DiffOp.Replace(34, 1, 34, 1),
                DiffOp.Equal(35, 35, 3),
            ),
            groups.last(),
        )
    }
}
