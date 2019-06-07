package com.learnteachcenter.ltcreikiclockv3.authentication.signup

import com.google.gson.annotations.Expose
import com.learnteachcenter.ltcreikiclockv3.authentication.User

data class SignUpResponse (
    @Expose var _id: String,
    @Expose var name: String,
    @Expose var email: String,
    @Expose var avatar: String
)