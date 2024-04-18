package org.itmo.itmoevent.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import org.itmo.itmoevent.R
import org.itmo.itmoevent.databinding.FragmentGeneralEventsBinding
import org.itmo.itmoevent.model.data.entity.EventShort
import org.itmo.itmoevent.view.adapters.EventAdapter
import org.itmo.itmoevent.view.fragments.base.BaseFragment
import org.itmo.itmoevent.viewmodel.MainViewModel
import org.itmo.itmoevent.viewmodel.GeneralEventsViewModel


class GeneralEventsFragment : BaseFragment<FragmentGeneralEventsBinding>() {

    private val eventItemViewModel: MainViewModel by activityViewModels()

    private var _model: GeneralEventsViewModel? = null
    private val model: GeneralEventsViewModel
        get() = _model as GeneralEventsViewModel

    private var eventAdapter: EventAdapter? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGeneralEventsBinding
        get() = { inflater, container, attach ->
            FragmentGeneralEventsBinding.inflate(inflater, container, attach)
        }

    override fun setup(view: View, savedInstanceState: Bundle?) {
        val model: GeneralEventsViewModel by viewModels {
            GeneralEventsViewModel.MainEventsViewModelFactory(
                application.roleRepository,
                application.eventRepository
            )
        }
        _model = model

        setupRecyclerViews()
        registerViewListeners()
        observeLiveData()
    }

    private fun setupRecyclerViews() {
        eventAdapter = eventAdapter ?: EventAdapter(::onEventClicked)
        viewBinding.run {
            generalEventsRv.adapter = eventAdapter
            generalEventsRv.layoutManager = LinearLayoutManager(context)
        }
    }

    private fun registerViewListeners() {
        viewBinding.run {
            genEventsFilterBtn.setOnClickListener {
                model.filterInputTitleLiveData.value =
                    genEventsFilterNameTl.editText?.text.toString()
                model.filterInputDateStartLiveData.value =
                    genEventsFilterFromTl.editText?.text.toString()
                model.filterInputDateEndLiveData.value =
                    genEventsFilterToTl.editText?.text.toString()
                model.filterInputFormatLiveData.value =
                    genEventsFilterFormatTl.editText?.text.toString()
                model.filterInputStatusLiveData.value =
                    genEventsFilterStatusTl.editText?.text.toString()

                model.submitFilterForm()
            }
        }
    }

    private fun observeLiveData() {
        viewBinding.run {
            model.run {
                handleContentItemViewByLiveData<List<EventShort>?>(
                    eventListLiveData, generalEventsRv,
                    genEventsProgressBar.root,
                ) { eventList ->
                    if (eventList == null) {
                        Toast.makeText(
                            context,
                            getString(R.string.no_found_message),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                    eventAdapter?.eventList = eventList ?: emptyList()
                }

                isFilterAvailableLiveData.observe(this@GeneralEventsFragment) { isAvailable ->
                    genEventsFilterCard.isVisible = isAvailable
                }

                handleBaseInputByLiveData(filterResultDateStartLiveData, genEventsFilterFromTl)
                handleBaseInputByLiveData(filterResultDateEndLiveData, genEventsFilterToTl)
                handleBaseInputByLiveData(filterResultTitleLiveData, genEventsFilterNameTl)
            }
        }
    }

    private fun handleBaseInputByLiveData(
        liveData: LiveData<GeneralEventsViewModel.InputResult>,
        textInputLayout: TextInputLayout
    ) {
        liveData.observe(this.viewLifecycleOwner) { result ->
            when (result) {
                is GeneralEventsViewModel.InputResult.Error ->
                    textInputLayout.error = result.text

                is GeneralEventsViewModel.InputResult.Success -> {
                    textInputLayout.error = null
                }
            }
        }
    }

    private fun onEventClicked(eventId: Int) {
        eventItemViewModel.selectEventItem(eventId)
    }

}
