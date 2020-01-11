package com.example.dependency

import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.fuel.core.FuelError
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.nio.charset.Charset
import java.util.Base64.getDecoder
import android.graphics.drawable.BitmapDrawable


class MainActivity : AppCompatActivity() {

    private var client = listOf( "client_id" to "a264011d2e9ae976c16b",
        "client_secret" to "ffcee3abb43442223a7512606a5e1a5cd0a9e63a")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        val elements = mutableListOf<Element>()

        runBlocking {
        intent
            ?.getStringExtra("urls")
            ?.split("\n")
            ?.forEach {
                val name = it.substringAfterLast('/').substringBefore('?')
                launch(Dispatchers.IO) {
                    val imports = getElement(getResponse(it))
                    elements.add(Element(name, imports))
                }
            }
        }

        val graph = Graph(elements)

        val display = windowManager.defaultDisplay
        val size = Point()

        display.getSize(size)
        val width = size.x
        val height = size.y

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        graph.draw(canvas, width/2)

        imageV.background = BitmapDrawable(resources, bitmap)
    }

    private fun getResponse(url: String) : Result<String, FuelError> {
        val (_, _, result) = url.httpGet(client).responseString()
        return result
    }

    private fun getElement(result: Result<String, FuelError>) : List<String> {
        when (result) {
            is Result.Failure -> {
                val ex = result.getException()
                Log.i("-ERROR", "MAIN - $ex")
                throw Exception(ex)
            }
            is Result.Success -> {
                return parseFile(result.get())
            }
        }
    }

    private fun parseFile(content: String): List<String> {
        val decoded = getDecoder()
            .decode(
                content
                    .substringAfterBefore(""""content":"""", "\",")
                    .split("""\n""")
                    .joinToString("")
            )
            .toString(Charset.forName("UTF-8"))

        return decoded
            .split("\n")
            .filter { it.contains("#include")
                    && it.contains('\"')
                    && !it.contains("stdafx")
            }
            .map { it.substringAfter("#include").trim().removeSurrounding("\"") }
    }
}

