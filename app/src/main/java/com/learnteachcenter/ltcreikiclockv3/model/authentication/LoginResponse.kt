package com.learnteachcenter.ltcreikiclockv3.model.authentication

import com.google.gson.annotations.Expose

data class LoginResponse (
    @Expose var success: Boolean,
    @Expose var token: String
)