package com.learnteachcenter.ltcreikiclockv3.reiki.one

import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position

class ReikiGenerator {
    fun generateReiki(
        id: String = "",
        seqNo: Int = 1,
        title: String = "",
        description: String? = "",
        playMusic: Boolean = true,
        positions: List<Position>? = listOf()): Reiki {
        return Reiki(id, seqNo, title, description, playMusic, positions)
    }
}