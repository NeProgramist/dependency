package com.example.dependency

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_first.*


class FirstActivity : AppCompatActivity() {

    private var client = listOf( "client_id" to "a264011d2e9ae976c16b",
        "client_secret" to "ffcee3abb43442223a7512606a5e1a5cd0a9e63a")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
    }

    fun submit(view: View) {
        val repo = repositoryEdit.text.toString()
        val substr = repo.substringAfter('/')
        val path = if (substr == repo) "" else "/$substr"
        "https://api.github.com/repos/${userEdit.text}/${repo.substringBefore('/')}/contents${path}"
            .httpGet(client)
            .responseString { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        userEdit.setTextColor(Color.RED)
                        repositoryEdit.setTextColor(Color.RED)
                        Log.i("-ERROR", "FIRST - $ex")
                    }
                    is Result.Success -> {
                        val mainIntent = Intent(this, MainActivity::class.java)
                        mainIntent.putExtra("urls", getUrl(result.get()))
                        startActivity(mainIntent)
                    }
                }
            }
    }

    private fun getUrl(data: String) =
        data
            .substring(2)
            .split("},{")
            .filter {
                (it.contains(".cpp")
                        || it.contains(".h"))
                        && !it.contains("stdafx")
                        && !it.contains("targetver.h")
            }
            .joinToString("\n") {
                it.substringAfterBefore("url", "\",", 3)
            }
}
