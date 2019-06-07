package com.learnteachcenter.ltcreikiclockv3.authentication

data class User (
    val name: String,
    val email: String,
    val password: String,
    val password2: String,
    val avatarUrl: String
)