package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.ReikiGenerator
import com.learnteachcenter.ltcreikiclockv3.model.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.model.room.RoomRepository

class ReikiViewModel(private val generator: ReikiGenerator = ReikiGenerator(),
                     private val repository: ReikiRepository = RoomRepository()) : ViewModel() {

    private val reikiLiveData = MutableLiveData<Reiki>()
    private val saveLiveData = MutableLiveData<Boolean>()

    fun getReikiLiveData(): LiveData<Reiki> = reikiLiveData
    fun getSaveLiveData(): LiveData<Boolean> = saveLiveData

    var title = ObservableField<String>("")
    var description = ObservableField<String>("")
    var playMusic = true

    lateinit var reiki: Reiki

    fun updateReiki() {
        reiki = generator.generateReiki(title.get() ?: "", description.get() ?: "", playMusic)
        reikiLiveData.postValue(reiki)
    }

    fun saveReiki() {
        updateReiki()
        return if(canSaveReiki()) {
            repository.saveReiki(reiki)
            saveLiveData.postValue(true)
        } else {
            saveLiveData.postValue(false)
        }
    }

    fun canSaveReiki(): Boolean {
        val title = this.title.get()
        title?.let {
            return title.isNotEmpty()
        }
        return false
    }
}
