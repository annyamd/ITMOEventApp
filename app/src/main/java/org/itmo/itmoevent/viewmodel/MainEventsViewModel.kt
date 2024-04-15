package org.itmo.itmoevent.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.itmo.itmoevent.model.data.entity.EventShort
import org.itmo.itmoevent.model.data.entity.enums.PrivilegeName
import org.itmo.itmoevent.model.data.entity.enums.PrivilegeName.*
import org.itmo.itmoevent.model.repository.EventRepository
import org.itmo.itmoevent.model.repository.RoleRepository
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date


class MainEventsViewModel(
    private val roleRepository: RoleRepository,
    private val eventsRepository: EventRepository
) : ViewModel() {

    private val disabledFunctions by lazy {
        val privileges = roleRepository.systemPrivileges?.map { it.name }
            ?: throw IllegalStateException("Privileges not cached")
        Function.entries.filter { func ->
            !privileges.contains(mapFuncToPrivilegeName(func))
        }
    }
    val disabledFunctionsLiveData: LiveData<List<Function>> = MutableLiveData(disabledFunctions)

    val isEventListLoading = MutableLiveData<Boolean>()
    val eventsLiveData = MutableLiveData<List<EventShort>?>()

    val filterInputTitleLiveData = MutableLiveData<String?>(null)
    val filterResultTitleLiveData = filterInputTitleLiveData.map { titleReceived ->
        val title = titleReceived ?: ""
        if (title.length > FILTER_TITLE_LENGTH_MAX) {
            InputResult.Error(FILTER_MESSAGE_TOO_LONG)
        } else if (!onlyLetters(title)) {
            InputResult.Error(FILTER_MESSAGE_SYMBOLS_INVALID)
        } else {
            InputResult.Success
        }
    }

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(FILTER_DATE_PATTERN)
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
        FILTER_DATETIME_PATTERN
    )

    val filterInputDateStartLiveData = MutableLiveData<String?>(null)
    val filterResultDateStartLiveData = filterInputDateStartLiveData.map { from ->
        if (from.isNullOrEmpty() || isDateValid(from)) {
            InputResult.Success
        } else {
            InputResult.Error(FILTER_MESSAGE_DATE_FORMAT_INVALID)
        }
    }

    val filterInputDateEndLiveData = MutableLiveData<String?>(null)
    val filterResultDateEndLiveData = filterInputDateEndLiveData.map { to ->
        if (to.isNullOrEmpty() || isDateValid(to)) {
            InputResult.Success
        } else {
            InputResult.Error(FILTER_MESSAGE_DATE_FORMAT_INVALID)
        }
    }

    val filterInputStatusLiveData = MutableLiveData<String?>()
    val filterInputFormatLiveData = MutableLiveData<String?>()

    init {
        viewModelScope.launch {
            eventsLiveData.value = loadEvents()
        }
    }

    fun submitFilterForm() {
        if (filterResultDateStartLiveData.value is InputResult.Success
            && filterResultDateEndLiveData.value is InputResult.Success
            && filterResultTitleLiveData.value is InputResult.Success
        ) {
            val status = filterInputStatusLiveData.value.let {
                if (it.isNullOrEmpty()) null else it
            }
            val format = filterInputFormatLiveData.value.let {
                if (it.isNullOrEmpty()) null else it
            }
            val title = filterInputTitleLiveData.value.let {
                if (it.isNullOrEmpty()) null else it
            }
            val start = parseDate(filterInputDateStartLiveData.value)
            val end = parseDate(filterInputDateEndLiveData.value)

            Log.i(
                "retrofit",
                "request to load status: $status, format: $format, title: $title, start: $start, end: $end"
            )

            viewModelScope.launch {
                val loaded = loadEvents(title, start, end, status, format)
                eventsLiveData.value = loaded
            }
        }
    }
//
//    private fun updateEvents(title: String? = null,
//                             start: Date? = null,
//                             end: Date? = null,
//                             status: String? = null,
//                             format: String? = null
//    ) = viewModelScope.launch {
//        val loaded = loadEvents(title, start, end, status, format)
//        eventsLiveData.value = loaded
//    }

    private fun onlyLetters(s: String) = (s.firstOrNull { !it.isLetter() } == null)
    private fun isDateValid(dateString: String) = FILTER_DATE_REGEX.toRegex().matches(dateString)
    private fun parseDate(dateString: String?): Date? {
        val dateTime: LocalDateTime = try {
            LocalDate.parse(dateString, dateFormatter).atStartOfDay()
        } catch (ex: DateTimeParseException) {
            try {
                LocalDateTime.parse(dateString, dateTimeFormatter)
            } catch (ex: DateTimeParseException) {
                return null
            }
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
    }


    private suspend fun loadEvents(
        title: String? = null,
        from: Date? = null,
        to: Date? = null,
        status: String? = null,
        format: String? = null
    ): List<EventShort>? {
        if (!disabledFunctions.contains(Function.SEE_EVENTS)) {
            Log.i("retrofit", "Start to load by filter")
            isEventListLoading.value = true
            val loaded = eventsRepository.getAllEvents(title, from, to, status, format)
            isEventListLoading.value = false
            return loaded
        }
        Log.i("retrofit", "No privs to start to load by filter")
        return null
    }

    private fun mapFuncToPrivilegeName(func: Function): PrivilegeName =
        when (func) {
            Function.SEE_EVENTS -> VIEW_ALL_EVENTS
            Function.FILTER_EVENTS -> SEARCH_EVENTS_AND_ACTIVITIES
        }


    sealed class InputResult {
        data class Error(val text: String) : InputResult()
        data object Success : InputResult()
    }

    enum class Function {
        SEE_EVENTS,
        FILTER_EVENTS
    }

    companion object {
        private const val FILTER_TITLE_LENGTH_MAX: Int = 32
        private const val FILTER_DATE_REGEX: String =
            "^(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.\\d{4}( ([0-1]?\\d|2[0-3])(:([0-5]?\\d)))?\$"
        private const val FILTER_DATE_PATTERN: String = "dd.MM.yyyy"
        private const val FILTER_DATETIME_PATTERN: String = "dd.MM.yyyy HH:mm"

        private const val FILTER_MESSAGE_TOO_LONG: String = "Строка слишком длинная"
        private const val FILTER_MESSAGE_SYMBOLS_INVALID: String =
            "Строка содержит недопустимые символы"
        private const val FILTER_MESSAGE_DATE_FORMAT_INVALID: String = "Некорректный формат даты"

    }


    class MainEventsViewModelFactory(
        private val roleRepository: RoleRepository,
        private val eventRepository: EventRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainEventsViewModel::class.java)) {
                return MainEventsViewModel(roleRepository, eventRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
