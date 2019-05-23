package com.learnteachcenter.ltcreikiclockv3.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import com.learnteachcenter.ltcreikiclockv3.model.Position
import com.learnteachcenter.ltcreikiclockv3.model.Reiki
import com.learnteachcenter.ltcreikiclockv3.model.ReikiGenerator
import com.learnteachcenter.ltcreikiclockv3.model.ReikiRepository

class ReikiViewModel(private val generator: ReikiGenerator = ReikiGenerator(),
                     private val repository: ReikiRepository = ReikiRepository()) : ViewModel() {

    private val reikiLiveData = MutableLiveData<Reiki>()
    private val saveLiveData = MutableLiveData<Boolean>()

    fun getReikiLiveData(): LiveData<Reiki> = reikiLiveData
    fun getSaveLiveData(): LiveData<Boolean> = saveLiveData

    var id = ObservableField<String>("")
    var title = ObservableField<String>("")
    var description = ObservableField<String>("")
    var playMusic = true

    lateinit var reiki: Reiki

    fun updateReiki() {
        reiki = generator.generateReiki(
            id.get() ?: "",
            title.get() ?: "",
            description.get() ?: "",
            playMusic,
            emptyList()
            )
        reikiLiveData.postValue(reiki)
    }

    fun saveReiki() {
        updateReiki()
        return if(canSaveReiki()) {
            repository.addReiki(reiki)
            saveLiveData.postValue(true)
        } else {
            saveLiveData.postValue(false)
        }
    }

    private fun canSaveReiki(): Boolean {
        val title = this.title.get()
        title?.let {
            return title.isNotEmpty()
        }
        return false
    }
}
