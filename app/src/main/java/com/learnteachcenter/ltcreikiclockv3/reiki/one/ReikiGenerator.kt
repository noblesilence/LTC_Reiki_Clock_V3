package com.learnteachcenter.ltcreikiclockv3.reiki.one

import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position

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