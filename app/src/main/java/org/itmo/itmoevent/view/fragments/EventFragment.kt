package org.itmo.itmoevent.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.GONE
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import org.itmo.itmoevent.EventApplication
import org.itmo.itmoevent.R
import org.itmo.itmoevent.databinding.FragmentEventBinding
import org.itmo.itmoevent.model.data.entity.Event
import org.itmo.itmoevent.model.data.entity.EventShort
import org.itmo.itmoevent.model.data.entity.Participant
import org.itmo.itmoevent.model.data.entity.Place
import org.itmo.itmoevent.model.data.entity.UserRole
import org.itmo.itmoevent.view.adapters.EventAdapter
import org.itmo.itmoevent.view.adapters.ParticipantAdapter
import org.itmo.itmoevent.view.adapters.UserAdapter
import org.itmo.itmoevent.view.fragments.base.BaseFragment
import org.itmo.itmoevent.view.util.DisplayDateFormats
import org.itmo.itmoevent.viewmodel.EventViewModel
import org.itmo.itmoevent.viewmodel.MainViewModel
import java.lang.IllegalStateException
import java.time.format.DateTimeFormatter

class EventFragment : BaseFragment<FragmentEventBinding>() {

    private var eventId: Int? = null
    private var model: EventViewModel? = null
    private val mainViewModel: MainViewModel by activityViewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentEventBinding
        get() = { inflater, container, attach ->
            FragmentEventBinding.inflate(inflater, container, attach)
    }

    private val activitiesAdapter = EventAdapter(object : EventAdapter.OnEventListClickListener {
        override fun onEventClicked(eventId: Int) {
            mainViewModel.selectActivity(eventId)
        }
    })

    private val orgAdapter = UserAdapter()
    private val participantsAdapter =
        ParticipantAdapter(object : ParticipantAdapter.OnParticipantMarkChangeListener {
            override fun changeMark(participantId: Int, isChecked: Boolean) {
                showShortToast("participant $participantId - $isChecked")
                model?.markEventParticipant(participantId, isChecked)
            }
        })

    private val tabItemsIndexViewMap by lazy {
        viewBinding.run {
            mapOf(
                0 to eventSubsectionAcivitiesRv,
                1 to eventSubsectionOrgGroup,
                2 to eventSubsectionParticipantsGroup
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun setup(view: View, savedInstanceState: Bundle?) {
        val eventId = requireArguments().getInt(EVENT_ID_ARG)
        this.eventId = eventId

        val model: EventViewModel by viewModels {
            val application = requireActivity().application as? EventApplication
                ?: throw IllegalStateException("Application must be EventApplication implementation")
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
                    show(tabItemsIndexViewMap.get(tab?.position ?: 0))
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    hide(tabItemsIndexViewMap.get(tab?.position ?: 0))
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}

            })

            model.run {
                handleContentItemViewByLiveData<Place>(
                    placeLiveData,
                    eventInfo.eventPlaceCard.root
                ) { place ->
                    viewBinding.eventInfo.run {
                        eventChipPlace.text = place.name
                        eventPlaceCard.placeItemTitle.text = place.name
                        eventPlaceCard.placeItemDesc.text = place.description
                        eventPlaceCard.placeItemFormat.text = place.format

                        eventPlaceCard.root.setOnClickListener {
                            mainViewModel.selectPlace(place.id)
                        }
                    }
                }


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

                handleContentItemViewByLiveData<Event>(
                    eventInfoLiveData,
                    eventContent,
                    eventProgressBarMain.root
                ) { event ->
                    viewBinding.eventInfo?.run {
                        val formatter =
                            DateTimeFormatter.ofPattern(DisplayDateFormats.DATE_EVENT_FULL)
                        event.run {
                            eventTitle.text = title
                            eventShortDesc.text = shortDesc
                            eventDescLong.text = longDesc
                            eventChipStatus.text = status
                            eventChipTime.text = startDate.format(formatter)
                            eventDetailsTimeHold.text =
                                getString(
                                    R.string.event_duration,
                                    startDate.format(formatter),
                                    endDate.format(formatter)
                                )
                            eventDetailsTimeRegister.text =
                                getString(
                                    R.string.event_duration,
                                    regStart.format(formatter),
                                    regEnd.format(formatter)
                                )
                            eventDetailsTimePrepare.text =
                                getString(
                                    R.string.event_duration,
                                    preparingStart.format(formatter),
                                    preparingEnd.format(formatter)
                                )
                            eventDetailsAge.text = participantAgeLowest.toString()
                            eventDetailsParticipantsMax.text = participantLimit.toString()
                        }
                    }
                }

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

    private fun blockTabItem(index: Int) {
        viewBinding.run {
            eventSubsectionsTab.getTabAt(index)?.view?.isClickable = false
        }
    }

    private fun switchVisibility(view: View) {
        when (view.visibility) {
            VISIBLE -> view.visibility = GONE
            GONE -> view.visibility = VISIBLE
            INVISIBLE -> view.visibility = VISIBLE
        }
    }

    companion object {
        const val EVENT_ID_ARG: String = "eventId"
    }

}
