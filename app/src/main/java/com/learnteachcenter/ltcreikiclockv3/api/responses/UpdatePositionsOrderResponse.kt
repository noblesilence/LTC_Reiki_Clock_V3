package com.learnteachcenter.ltcreikiclockv3.api.responses

import com.google.gson.annotations.Expose

data class UpdatePositionsOrderResponse (
    @Expose val success: Boolean = false,
    @Expose val error: String? = ""
)