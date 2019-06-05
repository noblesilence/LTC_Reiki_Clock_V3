package com.learnteachcenter.ltcreikiclockv3.reiki.session

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import com.learnteachcenter.ltcreikiclockv3.reiki.position.Position
import com.learnteachcenter.ltcreikiclockv3.reiki.one.Reiki

class ReikiAndAllPositions {

    @Embedded
    var reiki: Reiki? = null

    @Relation(parentColumn = "id",
        entityColumn = "reikiId")
    var positions: List<Position> = ArrayList()
}