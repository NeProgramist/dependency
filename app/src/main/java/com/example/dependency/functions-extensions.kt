package com.example.dependency

fun String.substringAfterBefore(start: String, end: String, shift: Int = 0) =
    substringAfter(start).substring(shift).substringBefore(end)

