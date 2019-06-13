package com.learnteachcenter.ltcreikiclockv3.reiki.one

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.util.Log
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.repositories.ReikiRepository
import com.learnteachcenter.ltcreikiclockv3.util.Resource

class ReikiViewModel(private val generator: ReikiGenerator = ReikiGenerator(),
                     private val repository: ReikiRepository = Injection.provideReikiRepository()
) : ViewModel() {

    private val reikiLiveData = MutableLiveData<Reiki>()
    private val saveLiveData = MutableLiveData<Resource<Reiki>>()

    fun getReikiLiveData(): LiveData<Reiki> = reikiLiveData
    fun getSaveLiveData(): LiveData<Resource<Reiki>> = saveLiveData

    var id = ObservableField<String>("")
    var seqNo = ObservableField<Int>(0)
    var title = ObservableField<String>("")
    var description = ObservableField<String>("")
    var playMusic = true

    lateinit var reiki: Reiki

    fun updateReiki() {
        reiki = generator.generateReiki(
            id.get() ?: "",
            seqNo.get() ?: 0,
            title.get() ?: "",
            description.get() ?: "",
            playMusic,
            emptyList()
            )
        reikiLiveData.postValue(reiki)
    }

    fun saveReiki() {
        updateReiki()

        if(canSaveReiki()) {
            Log.wtf("Reiki", "saveReiki")
        } else {
            Log.wtf("Reiki", "Title cannot be empty.")
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
