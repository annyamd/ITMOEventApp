package org.itmo.itmoevent.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContentLiveDataProvider<T>(
    private val isDisabled: Boolean,
    private val scope: CoroutineScope,
    private val loadContent: (ContentLiveDataProvider<T>) -> Deferred<T?>
) {

    private fun load() = scope.launch {
        if (!isDisabled) {
            contentIsLoading.value = true
            val deferred = loadContent(this@ContentLiveDataProvider)
            val content = deferred.await()
            if (content == null) {
                contentCall.value = ContentItemCallResult.Error("Err")
            } else {
                contentCall.value = ContentItemCallResult.Success(content)
            }
            contentIsLoading.value = false
        }
    }

    private val contentIsDisabled: StateFlow<Boolean> = MutableStateFlow(isDisabled)
    private val contentIsLoading = MutableStateFlow(false)
    private val contentCall: MutableStateFlow<ContentItemCallResult<T>?> = MutableStateFlow(null)

    val contentLiveData: LiveData<ContentItemUIState> = combine(
        contentIsDisabled, contentIsLoading, contentCall
    ) { isDisabled, isLoading, callResult ->
        if (isDisabled) {
            ContentItemUIState.Disabled
        } else if (isLoading) {
            ContentItemUIState.Loading
        } else {
            when (callResult) {
                is ContentItemCallResult.Success<T> ->
                    ContentItemUIState.Success(callResult.content)

                is ContentItemCallResult.Error ->
                    ContentItemUIState.Error(callResult.message)

                else -> ContentItemUIState.Loading
            }
        }

    }.stateIn(
        scope = scope,
        started = SharingStarted.Lazily,
        initialValue = ContentItemUIState.Start
    ).asLiveData()

    init {
        load()
    }

    sealed class ContentItemUIState {
        data object Disabled : ContentItemUIState()
        data object Loading : ContentItemUIState()
        data class Success<T>(val content: T?) : ContentItemUIState()
        data class Error(val errorMessage: String?) : ContentItemUIState()
        data object Start : ContentItemUIState()

    }

    private sealed class ContentItemCallResult<T> {
        data class Success<T>(val content: T? = null) : ContentItemCallResult<T>()
        data class Error<T>(val message: String? = null) : ContentItemCallResult<T>()
    }

}