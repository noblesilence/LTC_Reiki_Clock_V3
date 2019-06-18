package com.learnteachcenter.ltcreikiclockv3.api.responses

import com.google.gson.annotations.Expose

data class UpdateReikisOrderResponse (
    @Expose val success: Boolean = false,
    @Expose val error: String? = ""
)