package com.example.dependency

data class Block(val header: String, var code: String? = null, var imports: List<String>) {
    var width: Int = 0
    var height : Int = 0
}
