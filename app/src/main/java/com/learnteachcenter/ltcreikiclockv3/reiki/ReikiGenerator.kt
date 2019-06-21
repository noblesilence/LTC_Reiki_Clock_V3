package com.learnteachcenter.ltcreikiclockv3.reiki

import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position

class ReikiGenerator {
    fun generateReiki(
        id: String = "",
        seqNo: Int = 0,
        title: String = "",
        description: String? = "",
        playMusic: Boolean = true,
        positions: List<Position>? = listOf()): Reiki {
        return Reiki(id, seqNo, title, description, playMusic, positions)
    }

    fun generatePosition(
        id: String = "",
        seqNo: Int = 0,
        reikiId: String = "",
        title: String = "",
        duration: String = "01:00"): Position {
        return Position(id, seqNo, reikiId, title, duration)
    }
}