package com.learnteachcenter.ltcreikiclockv3.api.responses.Reiki

import com.google.gson.annotations.Expose

data class UpdateReikiResponse (
    @Expose val success: Boolean = false,
    @Expose val error: String? = ""
)