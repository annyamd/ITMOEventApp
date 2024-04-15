package org.itmo.itmoevent.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import org.itmo.itmoevent.EventApplication
import org.itmo.itmoevent.R
import org.itmo.itmoevent.databinding.FragmentUserEventsBinding
import org.itmo.itmoevent.model.data.entity.EventRequest
import org.itmo.itmoevent.model.data.entity.EventShort
import org.itmo.itmoevent.view.adapters.EventRequestAdapter
import org.itmo.itmoevent.view.adapters.EventAdapter
import org.itmo.itmoevent.viewmodel.ContentItemLiveDataProvider
import org.itmo.itmoevent.viewmodel.MainViewModel
import org.itmo.itmoevent.viewmodel.UserEventsViewModel
import java.lang.IllegalStateException


class UserEventsFragment : Fragment(R.layout.fragment_user_events),
    EventAdapter.OnEventListClickListener {
    private var viewBinding: FragmentUserEventsBinding? = null

//    private val model: UserEventsViewModel by viewModels {
//        val application = requireActivity().application as? EventApplication
//            ?: throw IllegalStateException("Application must be EventApplication implementation")
//        UserEventsViewModel.UserEventsViewModelFactory(
//            application.eventRequestRepository,
//            application.roleRepository,
//            application.eventRepository
//        )
//    }

    private val eventItemViewModel: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentUserEventsBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model: UserEventsViewModel by viewModels {
            val application = requireActivity().application as? EventApplication
                ?: throw IllegalStateException("Application must be EventApplication implementation")
            UserEventsViewModel.UserEventsViewModelFactory(
                application.eventRequestRepository,
                application.roleRepository,
                application.eventRepository
            )
        }

        val requestsAdapter = EventRequestAdapter()
        val eventsAdapter = EventAdapter(this)

        viewBinding?.run {
            userEventsRequestsRv.layoutManager = LinearLayoutManager(context)
            userEventsRequestsRv.adapter = requestsAdapter

            userEventsRoleEdit.setOnItemClickListener { _, _, position, _ ->
                model.roleNameIndex.value = position
            }
            userEventsProgressBarRoleEvents.progressBar.visibility = GONE

            userEventsOrganizedRv.layoutManager = LinearLayoutManager(context)
            userEventsOrganizedRv.adapter = eventsAdapter
            eventsAdapter.eventList = listOf()


            //            main item, has no privileges
            handleContentItemViewByLiveData<List<String>?>(
                model.roleListLiveData,
                userEventsGroupRoleEvents,
                userEventsProgressBarMain.root
            ) { roles ->
                viewBinding?.run {
                    (viewBinding?.userEventsRoleEdit as? MaterialAutoCompleteTextView)?.setSimpleItems(
                        roles?.toTypedArray() ?: emptyArray()
                    )
                }
            }

            handleContentItemViewByLiveData<List<EventRequest>?>(
                model.eventRequestsLiveData,
                userEventsGroupRequests
            ) { eventRequests ->
                viewBinding?.run {
                    requestsAdapter.eventRequests = eventRequests ?: emptyList()
                }
            }

            handleContentItemViewByLiveData<List<EventShort>?>(
                model.roleEventListLiveData,
                userEventsOrganizedRv,
                userEventsProgressBarRoleEvents.progressBar,
            ) { eventList ->
                viewBinding?.run {
                    eventsAdapter.eventList = eventList ?: emptyList()
                }
            }

        }
    }

    private fun <T> handleContentItemViewByLiveData(
        livedata: LiveData<ContentItemLiveDataProvider.ContentItemUIState>,
        contentView: View,
        progressBar: View? = null,
        needToShow: Boolean = true,
        ifDisabled: (() -> Unit)? = null,
        bindContent: (T) -> Unit
    ) {
        livedata.observe(this.viewLifecycleOwner) { state ->
            when (state) {
                is ContentItemLiveDataProvider.ContentItemUIState.Success<*> -> {
                    val content = state.content!! as T
                    bindContent(content)
                    if (needToShow) {
                        show(contentView)
                        progressBar?.let {
                            hide(progressBar)
                        }
                    }
                }

                is ContentItemLiveDataProvider.ContentItemUIState.Error -> {
                    if (needToShow) {
                        show(contentView)
                        progressBar?.let {
                            hide(progressBar)
                        }
                    }
                    state.errorMessage?.let {
                        showShortToast(it)
                    }
                }

                is ContentItemLiveDataProvider.ContentItemUIState.Disabled -> {
                    if (needToShow) {
                        hide(contentView)
                    }
                    ifDisabled?.invoke()
                    showShortToast("Blocked")
                }

                is ContentItemLiveDataProvider.ContentItemUIState.Loading -> {
                    if (needToShow) {
                        hide(contentView)
                        progressBar?.let {
                            show(progressBar)
                        }
                    }
                }

                is ContentItemLiveDataProvider.ContentItemUIState.Start -> {
                }
            }
        }
    }


    private fun show(view: View?) {
//        view?.visibility = View.VISIBLE
        view?.isVisible = true
    }

    private fun hide(view: View?) {
//        view?.visibility = GONE
        view?.isVisible = false
    }

    private fun showShortToast(text: String) {
        Toast.makeText(
            context,
            text,
            Toast.LENGTH_LONG
        ).show()
    }


    override fun onEventClicked(eventId: Int) {
        eventItemViewModel.selectEventItem(eventId)
    }

}
