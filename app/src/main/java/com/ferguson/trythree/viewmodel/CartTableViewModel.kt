package com.ferguson.trythree.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CartTableViewModel: ViewModel() {

    private val _tableData = mutableStateListOf<List<String>>()
    val tableData: List<List<String>> = _tableData
    var callMade: Boolean = false
    fun loadCartTable(userId: Int) {
        callMade = false
        val columnsToInclude = listOf("isbn", "title", "price", "copies_selected")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://undcemcs02.und.edu/~collin.l.ferguson/457/2/mobile_database.php")
                val postData = "action=get_cart&user_id=$userId"
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

                // You can parse `response` as JSON here if needed
                println("NetworkResponse: $response")
                val responseJson = JSONObject(response)

                val dataArray = responseJson.getJSONArray("data")
                val tableRows = mutableListOf<List<String>>()
                var header: List<String> = emptyList()

                if (dataArray.length() > 0) {
                    val firstItem = dataArray.getJSONObject(0)
                    val allKeys = firstItem.keys().asSequence().toList()

                    header = allKeys.filter { it in columnsToInclude }
                    tableRows.add(header)
                }

                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    val row = header.map { key -> item.optString(key) }
                    tableRows.add(row)
                }

                _tableData.clear()
                _tableData.addAll(tableRows)
                callMade = true

            } catch (e: Exception) {
                println("NetworkError$e")
                _tableData.clear()
                _tableData.add(listOf("Error"))
                _tableData.add(listOf(e.message ?: "Unknown error"))
            }
        }
    }
    suspend fun purchaseCart(customerId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("http://undcemcs02.und.edu/~collin.l.ferguson/457/2/mobile_database.php")
                val postData = "action=purchase_cart&user_id=$customerId"
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
                println(responseJson)

            } catch (e: Exception) {
                println("NetworkError: $e")
            }
        }
    }

    fun clearCart(){
        _tableData.clear()
    }
}