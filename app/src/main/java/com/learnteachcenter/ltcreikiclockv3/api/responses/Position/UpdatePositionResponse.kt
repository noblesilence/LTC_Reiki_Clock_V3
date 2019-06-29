package com.learnteachcenter.ltcreikiclockv3.api.responses.Position

import com.google.gson.annotations.Expose

data class UpdatePositionResponse  (
    @Expose val success: Boolean = false,
    @Expose val message: String? = ""
)