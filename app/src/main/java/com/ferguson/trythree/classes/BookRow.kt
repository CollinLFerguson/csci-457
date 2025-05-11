package com.ferguson.trythree.classes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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
//{
//    var isChecked by mutableStateOf(false)
//    var quantity by mutableStateOf(0)
//}