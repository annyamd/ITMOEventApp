package org.itmo.itmoevent.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    val eventId = MutableLiveData<Int>()
    val exitIntended = MutableLiveData<Boolean>()

    fun selectEventItem(id: Int) {
        eventId.value = id
    }

    fun exit() {
        exitIntended.value = true
    }

}