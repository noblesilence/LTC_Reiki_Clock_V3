package com.learnteachcenter.ltcreikiclockv3.model.reikis

class ReikiGenerator {
    fun generateReiki(
        id: String = "",
        title: String = "",
        description: String = "",
        playMusic: Boolean = true,
        positions: List<Position> = listOf()): Reiki {
        return Reiki(id, title, description, playMusic, positions)
    }
}