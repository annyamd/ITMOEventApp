package org.itmo.itmoevent.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import org.itmo.itmoevent.databinding.EventInfoBinding
import org.itmo.itmoevent.databinding.FragmentEventBinding
import org.itmo.itmoevent.databinding.PlaceItemBinding
import org.itmo.itmoevent.model.data.entity.Event
import org.itmo.itmoevent.model.data.entity.EventShort
import org.itmo.itmoevent.model.data.entity.Participant
import org.itmo.itmoevent.model.data.entity.Place
import org.itmo.itmoevent.model.data.entity.UserRole
import org.itmo.itmoevent.view.adapters.EventAdapter
import org.itmo.itmoevent.view.adapters.ParticipantAdapter
import org.itmo.itmoevent.view.adapters.UserAdapter
import org.itmo.itmoevent.view.fragments.base.BaseFragment
import org.itmo.itmoevent.view.fragments.binding.ContentBinding
import org.itmo.itmoevent.view.fragments.binding.EventInfoContentBinding
import org.itmo.itmoevent.view.fragments.binding.PlaceItemContentBinding
import org.itmo.itmoevent.viewmodel.EventViewModel
import org.itmo.itmoevent.viewmodel.MainViewModel

class EventFragment : BaseFragment<FragmentEventBinding>() {

    private var eventId: Int? = null
    private var model: EventViewModel? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val eventInfoContentBinding: ContentBinding<EventInfoBinding, Event> by lazy {
        EventInfoContentBinding(requireActivity())
    }
    private val placeContentBinding: ContentBinding<PlaceItemBinding, Place> =
        PlaceItemContentBinding()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentEventBinding
        get() = { inflater, container, attach ->
            FragmentEventBinding.inflate(inflater, container, attach)
        }

    private val activitiesAdapter by lazy {
        EventAdapter(object : EventAdapter.OnEventListClickListener {
            override fun onEventClicked(eventId: Int) {
                mainViewModel.selectActivity(eventId)
            }
        })
    }

    private val orgAdapter by lazy { UserAdapter() }

    private val participantsAdapter by lazy {
        ParticipantAdapter(object : ParticipantAdapter.OnParticipantMarkChangeListener {
            override fun changeMark(participantId: Int, isChecked: Boolean) {
                showShortToast("participant $participantId - $isChecked")
                model?.markEventParticipant(participantId, isChecked)
            }
        })
    }

    private val tabItemsIndexViewMap by lazy {
        viewBinding.run {
            mapOf(
                0 to eventSubsectionAcivitiesRv,
                1 to eventSubsectionOrgGroup,
                2 to eventSubsectionParticipantsGroup
            )
        }
    }

    override fun setup(view: View, savedInstanceState: Bundle?) {
        val eventId = requireArguments().getInt(EVENT_ID_ARG)
        this.eventId = eventId

        val model: EventViewModel by viewModels {
            EventViewModel.EventViewModelFactory(
                eventId,
                application.eventDetailsRepository,
                application.roleRepository
            )
        }
        this.model = model

        viewBinding.run {
            eventSubsectionAcivitiesRv.layoutManager = LinearLayoutManager(context)
            eventSubsectionAcivitiesRv.adapter = activitiesAdapter
            eventSubsectionOrganisatorsRv.layoutManager = LinearLayoutManager(context)
            eventSubsectionOrganisatorsRv.adapter = orgAdapter
            eventSubsectionParticipantsRv.layoutManager = LinearLayoutManager(context)
            eventSubsectionParticipantsRv.adapter = participantsAdapter

            eventInfo.run {
                eventDescHeader.setOnClickListener {
                    switchVisibility(eventDescLong)
                }

                eventDetailsHeader.setOnClickListener {
                    switchVisibility(eventDetailsGroup)
                }
            }

            eventSubsectionOrganisatorsRoleSelect.roleEdit.run {
                this.setOnItemClickListener { _, _, _, _ ->
                    model.roleName.value = this.text.toString()
                }
            }

            eventSubsectionParticipantsMarkAll.setOnClickListener {
                model.markEventParticipantsAll()
            }

            eventEditBtn.setOnClickListener {

            }

            eventSubsectionsTab.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    show(tabItemsIndexViewMap[tab?.position ?: 0])
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    hide(tabItemsIndexViewMap[tab?.position ?: 0])
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}

            })

            model.run {
                handleContentItemViewByLiveData(
                    placeLiveData,
                    eventInfo.eventPlaceCard.root,
                    bindContent = ::bindPlace
                )

                handleContentItemViewByLiveData<List<EventShort>>(
                    activitiesLiveData,
                    eventSubsectionAcivitiesRv,
                    eventSubsectionsProgressBar.root,
                    false,
                    { blockTabItem(0) }
                ) { activities ->
                    viewBinding.run {
                        activitiesAdapter.eventList = activities
                    }
                }

                handleContentItemViewByLiveData<List<Participant>>(
                    participantsLiveData,
                    eventSubsectionParticipantsRv,
                    eventSubsectionsProgressBar.root,
                    false,
                    { blockTabItem(2) }
                ) { participants ->
                    viewBinding.run {
                        participantsAdapter.participantList = participants
                    }
                }

                handleContentItemViewByLiveData<List<UserRole>>(
                    orgsLiveData,
                    eventSubsectionOrgGroup,
                    eventSubsectionsProgressBar.root,
                    false,
                    { blockTabItem(1) }
                ) { }

                handleContentItemViewByLiveData(
                    eventInfoLiveData,
                    eventContent,
                    eventProgressBarMain.root,
                    bindContent = ::bindEventInfo
                )

                rolesList.observe(this@EventFragment.viewLifecycleOwner) { roles ->
                    (eventSubsectionOrganisatorsRoleSelect.roleEdit as? MaterialAutoCompleteTextView)?.setSimpleItems(
                        roles.toTypedArray()
                    )
                }

                roleOrganizersList.observe(this@EventFragment.viewLifecycleOwner) { orgs ->
                    orgs?.let {
                        orgAdapter.userList = orgs
                    }
                }

            }
        }
    }

    private fun bindEventInfo(event: Event) {
        eventInfoContentBinding.bindContentToView(viewBinding.eventInfo, event)
    }

    private fun bindPlace(place: Place) {
        viewBinding.eventInfo.run {
            placeContentBinding.bindContentToView(eventPlaceCard, place)
            eventChipPlace.text = place.name

            eventPlaceCard.root.setOnClickListener {
                mainViewModel.selectPlace(place.id)
            }
        }
    }

    private fun blockTabItem(index: Int) {
        viewBinding.eventSubsectionsTab.getTabAt(index)?.view?.isClickable = false
    }

    private fun switchVisibility(view: View) {
        view.isVisible = !view.isVisible
    }

    companion object {
        const val EVENT_ID_ARG: String = "eventId"
    }

}
