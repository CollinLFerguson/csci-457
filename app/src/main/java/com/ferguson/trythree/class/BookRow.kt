package com.ferguson.trythree.`class`

data class BookRow(
    val dbkey: Int,
    val isbn: String,
    val title: String,
    val price: Double,
    val times_purchased: Int = 0,
    val times_sold: Int = 0,
    var isChecked: Boolean = false,
    var quantity: Int = 0
)