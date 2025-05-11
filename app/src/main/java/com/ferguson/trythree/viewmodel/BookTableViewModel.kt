package com.ferguson.trythree.viewmodels

import com.ferguson.trythree.`class`.BookRow

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class BookTableViewModel : ViewModel() {
    private val _bookRows = mutableStateListOf<BookRow>()
    val bookRows: List<BookRow> = _bookRows

    fun loadBookTable() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url =
                    URL("http://undcemcs02.und.edu/~collin.l.ferguson/457/2/mobile_database.php")
                val postData = "action=get_books"
                val postBytes = postData.toByteArray(Charsets.UTF_8)

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    setRequestProperty("Content-Length", postBytes.size.toString())
                }

                connection.outputStream.use { os ->
                    os.write(postBytes)
                }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(response)
                val dataArray = responseJson.getJSONArray("data")

                val newRows = mutableListOf<BookRow>()

                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    val dbkey = item.optInt("dbkey")
                    val isbn = item.optString("isbn")
                    val title = item.optString("title")
                    val price = item.optDouble("price")

                    newRows.add(BookRow(dbkey, isbn, title, price))
                }

                _bookRows.clear()
                _bookRows.addAll(newRows)

            } catch (e: Exception) {
                println("NetworkError$e")
                _bookRows.clear()
                _bookRows.add(BookRow(-1, "Error", e.message ?: "Unknown error", 0.0))
            }
        }
    }

    fun updateChecked(isbn: String, checked: Boolean) {
        _bookRows.find { it.isbn == isbn }?.isChecked = checked
    }

    fun updateQuantity(isbn: String, quantity: Int) {
        _bookRows.find { it.isbn == isbn }?.quantity = quantity
    }

    fun getSelectedBooks(): Map<Int, Int> {
        return _bookRows.filter { it.isChecked }.associate { it.dbkey to it.quantity }
    }

    fun addBookstoCart(customerId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val selectedBooks = getSelectedBooks()
            val bookArray = JSONArray()

            selectedBooks.forEach { (dbkey, qty) ->
                val bookObject = JSONObject().apply {
                    put("book_dbkey", dbkey)
                    put("copies_selected", qty)
                }
                bookArray.put(bookObject)
            }

            println("Book Array: $bookArray")

            try {
                val url =
                    URL("http://undcemcs02.und.edu/~collin.l.ferguson/457/2/mobile_database.php")
                val postData = "action=move_to_cart&user_id=$customerId&selected_books=${
                    URLEncoder.encode(
                        bookArray.toString(),
                        "UTF-8"
                    )
                }"
                val postBytes = postData.toByteArray(Charsets.UTF_8)

                println("HERE")
                println(postData)
                println(postBytes)

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    setRequestProperty("Content-Length", postBytes.size.toString())
                }

                connection.outputStream.use { os ->
                    os.write(postBytes)
                }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(response)
                println(responseJson)
            } catch (e: Exception) {
                println("NetworkError$e")
                //_bookRows.clear()
                // _bookRows.add(BookRow(-1, "Error", e.message ?: "Unknown error", 0.0))
            }
        }
    }
}

