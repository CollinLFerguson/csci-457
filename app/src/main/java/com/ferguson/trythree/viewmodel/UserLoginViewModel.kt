package com.ferguson.trythree.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferguson.trythree.classes.ActiveUser
import com.ferguson.trythree.classes.LoginState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class UserLoginViewModel : ViewModel() {

    val loginResult = mutableStateOf<LoginState>(LoginState.Idle)

    fun login(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://undcemcs02.und.edu/~collin.l.ferguson/457/2/mobile_database.php")
                val postData = "action=login&username=${URLEncoder.encode(username, "UTF-8")}&password=${URLEncoder.encode(password, "UTF-8")}"
                val postBytes = postData.toByteArray(Charsets.UTF_8)

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    setRequestProperty("Content-Length", postBytes.size.toString())
                }

                connection.outputStream.use { it.write(postBytes) }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                if (!json.getBoolean("error")) {
                    val userObj = json.getJSONObject("user")
                    val user = ActiveUser(
                        id = userObj.getInt("id"),
                        username = userObj.getString("username"),
                        usertype = userObj.getString("usertype")
                    )
                    loginResult.value = LoginState.Success(user)
                } else {
                    loginResult.value = LoginState.Failure("Invalid credentials")
                }
            } catch (e: Exception) {
                loginResult.value = LoginState.Failure(e.message ?: "Network error")
            }
        }
    }
    fun logout() {
        loginResult.value = LoginState.Idle
    }
}




