package com.learnteachcenter.ltcreikiclockv3.api.responses.Position

import com.google.gson.annotations.Expose
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position

data class DeletePositionResponse (
    @Expose
    val success: Boolean = false
)