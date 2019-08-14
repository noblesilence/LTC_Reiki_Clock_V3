package com.learnteachcenter.ltcreikiclockv3.authentication.forgotpassword

import com.google.gson.annotations.Expose

data class ForgotPasswordResponse (
    @Expose var success: Boolean,
    @Expose var message: String
)