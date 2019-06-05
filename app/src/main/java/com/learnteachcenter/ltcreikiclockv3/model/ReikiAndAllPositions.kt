package com.learnteachcenter.ltcreikiclockv3.model

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation

class ReikiAndAllPositions {

    @Embedded
    var reiki: Reiki? = null

    @Relation(parentColumn = "id",
        entityColumn = "reikiId")
    var positions: List<Position> = ArrayList()
}