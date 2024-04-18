package org.itmo.itmoevent.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import org.itmo.itmoevent.databinding.FragmentUserEventsBinding
import org.itmo.itmoevent.model.data.entity.EventRequest
import org.itmo.itmoevent.model.data.entity.EventShort
import org.itmo.itmoevent.view.adapters.EventRequestAdapter
import org.itmo.itmoevent.view.adapters.EventAdapter
import org.itmo.itmoevent.view.fragments.base.BaseFragment
import org.itmo.itmoevent.viewmodel.MainViewModel
import org.itmo.itmoevent.viewmodel.UserEventsViewModel


class UserEventsFragment : BaseFragment<FragmentUserEventsBinding>() {

    private var _model: UserEventsViewModel? = null
    private val model: UserEventsViewModel
        get() = _model as UserEventsViewModel
    private val eventItemViewModel: MainViewModel by activityViewModels()

    private var requestAdapter: EventRequestAdapter? = null
    private var eventAdapter: EventAdapter? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUserEventsBinding
        get() = { inflater, container, attach ->
            FragmentUserEventsBinding.inflate(inflater, container, attach)
        }

    override fun setup(view: View, savedInstanceState: Bundle?) {
        val model: UserEventsViewModel by viewModels {
            UserEventsViewModel.UserEventsViewModelFactory(
                application.eventRequestRepository,
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
        requestAdapter = requestAdapter ?: EventRequestAdapter()
        eventAdapter = eventAdapter ?: EventAdapter(::onEventClicked)

        viewBinding.run {
            userEventsRequestsRv.layoutManager = LinearLayoutManager(context)
            userEventsRequestsRv.adapter = requestAdapter

            userEventsOrganizedRv.layoutManager = LinearLayoutManager(context)
            userEventsOrganizedRv.adapter = eventAdapter
        }
    }

    private fun registerViewListeners() {
        viewBinding.run {
            userEventsRoleEdit.setOnItemClickListener { _, _, position, _ ->
                model.roleNameIndex.value = position
            }
        }
    }

    private fun observeLiveData() {
        viewBinding.run {
            handleContentItemViewByLiveData<List<String>?>(
                model.roleListLiveData, userEventsGroupRoleEvents, userEventsProgressBarMain.root
            ) { roles ->
                (viewBinding.userEventsRoleEdit as? MaterialAutoCompleteTextView)?.setSimpleItems(
                    roles?.toTypedArray() ?: emptyArray()
                )
            }

            handleContentItemViewByLiveData<List<EventRequest>?>(
                model.eventRequestsLiveData, userEventsGroupRequests
            ) { eventRequests ->
                requestAdapter?.eventRequests = eventRequests ?: emptyList()
            }

            handleContentItemViewByLiveData<List<EventShort>?>(
                model.roleEventListLiveData, userEventsOrganizedRv,
                userEventsProgressBarRoleEvents.progressBar,
            ) { eventList ->
                eventAdapter?.eventList = eventList ?: emptyList()
            }

        }
    }

    private fun onEventClicked(eventId: Int) {
        eventItemViewModel.selectEventItem(eventId)
    }

}
