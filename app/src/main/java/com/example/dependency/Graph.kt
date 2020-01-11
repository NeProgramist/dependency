package com.example.dependency

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.*
import android.graphics.Path
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log

data class Graph (val elements: List<Element>) {

    private val border = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = STROKE
    }

    private val fillGreen = Paint().apply {
        color = Color.parseColor("#008577")
        strokeWidth = 5f
        style = FILL
    }

    private val fillGray = Paint().apply {
        color = Color.GRAY
        strokeWidth = 5f
        style = FILL
    }

    private val textPaint = TextPaint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        textSize = 30f
        style = FILL
    }

    private val blockWidth = 90f
    private val blockHeight = 90f
    private val deltaHeight = 350f
    private val deltaWidth = 30f

    private val blocks = createBlocks(elements)
    private val root = getRoot()


    private fun getRoot() : Block {
        val notRoot = mutableSetOf<String>()

        for(block in blocks) {
            block.imports.forEach {
                notRoot.add(it.substringBefore('.').toLowerCase())
            }
        }

        val res = blocks.filter {
            it.header.substringBefore('.').toLowerCase() !in notRoot
        }

        Log.i("ROOT", res.toString())
        return if(res.isNotEmpty()) res[0] else blocks[0]
    }

    private fun createBlocks(elements: List<Element>) : List<Block>{
        val blocks = mutableListOf<Block>()

        val headers = elements
            .filter { it.name.contains(".h")}
            .map { it.name.substringBefore(".h") }

        val codes = elements
            .filter { it.name.contains(".cpp")}
            .map { it.name.substringBefore(".cpp") }

        for (header in headers) {
            var cpp: String? = null
            var imports = elements
                .find {it.name == "$header.h"}!!
                .import
                .toMutableList()
            for (code in codes) {
                if (header == code) {
                    cpp = "$code.cpp"
                    imports = imports.union(elements.find {it.name == cpp}!!.import).toMutableList()

                    imports.remove("$header.h")
                    break
                }
            }

            blocks.add(Block("$header.h", cpp, imports))
        }
        return blocks
    }

    fun draw(canvas: Canvas,
                    width: Int,
                    block: Block = root,
                    visited: MutableSet<Block> = mutableSetOf(),
                    count: Float = 1f/2) {

        val centerH = deltaHeight*count
        val color = if (block.code == null) fillGray else fillGreen

        canvas.drawRoundRect(width-blockWidth,
            centerH-blockHeight,
            width+blockWidth,
            centerH+blockHeight,
            20f, 20f, border)

        canvas.drawRoundRect(width-blockWidth,
            centerH-blockHeight,
            width+blockWidth,
            centerH+blockHeight,
            20f, 20f, color)

        val text = block.header.substringBefore(".h")
        val staticLayout =
            StaticLayout.Builder.obtain(
                text, 0, text.length, textPaint, 200
            ).build()

        canvas.save()
        canvas.translate(width-blockWidth, centerH-blockHeight)
        staticLayout.draw(canvas)
        canvas.restore()

        block.height = centerH.toInt()
        block.width = width

        var index = 1
        block.imports.forEach{
            val cur = blocks.find { el -> it.toLowerCase() == el.header.toLowerCase() }!!
            if (cur !in visited) {
                visited.add(cur)
                val x = width/(block.imports.size+2) + index*(blockWidth*2+deltaWidth).toInt()
                when(block.height.toFloat()>=cur.height.toFloat())
                {
                    false -> canvas.drawLine(block.width.toFloat(), block.height.toFloat()-blockHeight, x.toFloat(), deltaHeight*(count+1)+blockHeight, fillGray )
                    true -> canvas.drawLine(block.width.toFloat(), block.height.toFloat()+blockHeight, x.toFloat(), deltaHeight*(count+1)-blockHeight, fillGray )
                }
                draw(canvas, width/(block.imports.size+2) + index*(blockWidth*2+deltaWidth).toInt(), cur, visited, count+1)
                index++
            } else {
                when(block.height.toFloat()>=cur.height.toFloat())
                {
                    true -> canvas.drawLine(block.width.toFloat(), block.height.toFloat()-blockHeight, cur.width.toFloat(), cur.height.toFloat()+blockHeight, fillGray )
                    false-> canvas.drawLine(block.width.toFloat(), block.height.toFloat()+blockHeight, cur.width.toFloat(), cur.height.toFloat()-blockHeight, fillGray )
                }
            }
        }
    }

}