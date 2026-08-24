// port-lint: source algorithms/replace.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.Algorithm
import io.github.kotlinmania.similar.DiffOp
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceTest {
    @Test
    fun testMayersReplace() {
        val old = listOf(
            ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n",
            "a\n",
            "b\n",
            "c\n",
            "================================\n",
            "d\n",
            "e\n",
            "f\n",
            "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n",
        )
        val new = listOf(
            ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n",
            "x\n",
            "b\n",
            "c\n",
            "================================\n",
            "y\n",
            "e\n",
            "f\n",
            "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n",
        )

        val d = Replace(Capture())
        diffSlices(Algorithm.Myers, d, old, new)

        assertEquals(
            listOf(
                DiffOp.Equal(0, 0, 1),
                DiffOp.Replace(1, 1, 1, 1),
                DiffOp.Equal(2, 2, 3),
                DiffOp.Replace(5, 1, 5, 1),
                DiffOp.Equal(6, 6, 3),
            ),
            d.intoInner().ops(),
        )
    }

    @Test
    fun testReplace() {
        val old = listOf(0, 1, 2, 3, 4)
        val new = listOf(0, 1, 2, 7, 8, 9)
        val d = Replace(Capture())

        diffSlices(Algorithm.Myers, d, old, new)

        assertEquals(
            listOf(
                DiffOp.Equal(0, 0, 3),
                DiffOp.Replace(3, 2, 3, 3),
            ),
            d.intoInner().ops(),
        )
    }
}
