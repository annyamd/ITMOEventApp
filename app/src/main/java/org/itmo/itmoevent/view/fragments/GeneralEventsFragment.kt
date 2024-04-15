package org.itmo.itmoevent.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import org.itmo.itmoevent.EventApplication
import org.itmo.itmoevent.R
import org.itmo.itmoevent.databinding.FragmentGeneralEventsBinding
import org.itmo.itmoevent.view.adapters.EventAdapter
import org.itmo.itmoevent.viewmodel.MainViewModel
import org.itmo.itmoevent.viewmodel.MainEventsViewModel
import org.itmo.itmoevent.viewmodel.MainEventsViewModel.Function
import org.itmo.itmoevent.viewmodel.MainEventsViewModel.Function.*
import java.lang.IllegalStateException


class GeneralEventsFragment : Fragment(R.layout.fragment_general_events),
    EventAdapter.OnEventListClickListener {
    private var viewBinding: FragmentGeneralEventsBinding? = null
    private val eventItemViewModel: MainViewModel by activityViewModels()

    private val model: MainEventsViewModel by viewModels {
        val application = requireActivity().application as? EventApplication
            ?: throw IllegalStateException("Application must be EventApplication implementation")
        MainEventsViewModel.MainEventsViewModelFactory(
            application.roleRepository,
            application.eventRepository
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentGeneralEventsBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventAdapter = EventAdapter(this)

        viewBinding?.run {
            generalEventsRv.adapter = eventAdapter
            generalEventsRv.layoutManager = LinearLayoutManager(context)

            genEventsFilterBtn.setOnClickListener {
                model.filterInputTitleLiveData.value = genEventsFilterNameTl.editText?.text.toString()
                model.filterInputDateStartLiveData.value = genEventsFilterFromTl.editText?.text.toString()
                model.filterInputDateEndLiveData.value = genEventsFilterToTl.editText?.text.toString()
                model.filterInputFormatLiveData.value = genEventsFilterFormatTl.editText?.text.toString()
                model.filterInputStatusLiveData.value = genEventsFilterStatusTl.editText?.text.toString()

                model.submitFilterForm()
            }


            model.eventsLiveData.observe(this@GeneralEventsFragment.viewLifecycleOwner) { eventList ->
                if (eventList == null) {
                    Toast.makeText(
                        context,
                        getString(R.string.no_found_message),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    eventAdapter.eventList = emptyList()
                } else {
                    eventAdapter.eventList = eventList
                }
            }

            model.isEventListLoading.observe(this@GeneralEventsFragment.viewLifecycleOwner) { isLoading ->
                generalEventsRv.isVisible = !isLoading
                genEventsProgressBar.root.isVisible = isLoading
            }

            model.disabledFunctionsLiveData.observe(this@GeneralEventsFragment.viewLifecycleOwner) { disabledFunctions ->
                val funcViewMap = getFuncViewMap(this)
                disabledFunctions.forEach { func ->
                    val viewToHide = funcViewMap[func]
                    if (viewToHide != null) {
                        viewToHide.isVisible = false
                    }
                }
            }

            handleBaseInputByLiveData(model.filterResultDateStartLiveData, genEventsFilterFromTl)
            handleBaseInputByLiveData(model.filterResultDateEndLiveData, genEventsFilterToTl)
            handleBaseInputByLiveData(model.filterResultTitleLiveData, genEventsFilterNameTl)

        }
    }

    private fun handleBaseInputByLiveData(liveData: LiveData<MainEventsViewModel.InputResult>, textInputLayout: TextInputLayout) {
        liveData.observe(this.viewLifecycleOwner) { result ->
            when (result) {
                is MainEventsViewModel.InputResult.Error ->
                    textInputLayout.error = result.text
                is MainEventsViewModel.InputResult.Success -> {
                    textInputLayout.error = null
                }
            }
        }
    }

    private fun getFuncViewMap(binding: FragmentGeneralEventsBinding): Map<Function, View> = mapOf(
        SEE_EVENTS to binding.generalEventsRv,
        FILTER_EVENTS to binding.genEventsFilterCard
    )

    override fun onEventClicked(eventId: Int) {
        eventItemViewModel.selectEventItem(eventId)
    }

}
