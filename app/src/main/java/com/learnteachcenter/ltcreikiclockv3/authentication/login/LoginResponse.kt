package com.learnteachcenter.ltcreikiclockv3.authentication.login

import com.google.gson.annotations.Expose

data class LoginResponse (
    @Expose var success: Boolean,
    @Expose var token: String
)