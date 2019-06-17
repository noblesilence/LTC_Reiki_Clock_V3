package com.learnteachcenter.ltcreikiclockv3.api.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateReikiResponse (
    @Expose val success: Boolean = true,
    @Expose val error: String? = ""
)