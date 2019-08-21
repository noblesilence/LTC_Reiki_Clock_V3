package com.learnteachcenter.ltcreikiclockv3.contact

import com.google.gson.annotations.Expose

data class SendEmailResponse(
    @Expose var success: Boolean,
    @Expose var message: String
)