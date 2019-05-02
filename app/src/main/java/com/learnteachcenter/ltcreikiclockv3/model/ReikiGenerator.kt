package com.learnteachcenter.ltcreikiclockv3.model

class ReikiGenerator {
    fun generateReiki(title: String = "", description: String = "", playMusic: Boolean = true): Reiki {
        return Reiki(title, description, playMusic)
    }
}