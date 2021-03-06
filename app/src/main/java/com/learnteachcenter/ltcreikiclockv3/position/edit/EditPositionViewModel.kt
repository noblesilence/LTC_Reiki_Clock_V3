package com.learnteachcenter.ltcreikiclockv3.position.edit

import android.arch.lifecycle.ViewModel
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.position.Position
import com.learnteachcenter.ltcreikiclockv3.repositories.ReikiRepository

class EditPositionViewModel (val repository: ReikiRepository = Injection.provideReikiRepository()) : ViewModel() {
    fun updatePosition(position: Position) = repository.updatePosition(position)
}