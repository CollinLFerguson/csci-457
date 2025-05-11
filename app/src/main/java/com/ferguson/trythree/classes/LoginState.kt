package com.ferguson.trythree.classes

sealed class LoginState {
    object Idle : LoginState()
    data class Success(val user: ActiveUser) : LoginState()
    data class Failure(val errorMessage: String) : LoginState()
}