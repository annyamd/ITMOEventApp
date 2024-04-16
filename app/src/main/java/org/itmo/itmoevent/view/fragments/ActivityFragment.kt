package org.itmo.itmoevent.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.google.android.material.tabs.TabLayout
import org.itmo.itmoevent.EventApplication
import org.itmo.itmoevent.R
import org.itmo.itmoevent.databinding.FragmentActivBinding
import org.itmo.itmoevent.model.data.entity.EventsActivity
import org.itmo.itmoevent.model.data.entity.Place
import org.itmo.itmoevent.view.util.DisplayDateFormats
import org.itmo.itmoevent.viewmodel.ContentItemLiveDataProvider
import org.itmo.itmoevent.viewmodel.EventActivityViewModel
import java.time.format.DateTimeFormatter

class ActivityFragment : Fragment(R.layout.fragment_activ) {

    private var viewBinding: FragmentActivBinding? = null
    private var activityId: Int? = null
    private var model: EventActivityViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentActivBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activityId = requireArguments().getInt(ACTIVITY_ID_ARG)
        this.activityId = activityId

        val model: EventActivityViewModel by viewModels {
            val application = requireActivity().application as? EventApplication
                ?: throw IllegalStateException("Application must be EventApplication implementation")
            EventActivityViewModel.ActivityViewModelModelFactory(
                activityId,
                application.eventActivityRepository,
                application.placeRepository,
                application.roleRepository
            )
        }

        viewBinding?.run {
            handleContentItemViewByLiveData<EventsActivity>(
                model.activityInfoLiveData,
                activityContent,
                activityProgressBarMain.root
            ) { event ->
                activityInfo.run {
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

            handleContentItemViewByLiveData<Place>(
                model.placeLiveData,
                activityInfo.eventPlaceCard.root
            ) { place ->
                activityInfo.run {
                    eventChipPlace.text = place.name
                    eventPlaceCard.placeItemTitle.text = place.name
                    eventPlaceCard.placeItemDesc.text = place.description
                    eventPlaceCard.placeItemFormat.text = place.format
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
        view?.visibility = View.VISIBLE
    }

    private fun hide(view: View?) {
        view?.visibility = TabLayout.GONE
    }

    private fun showShortToast(text: String) {
        Toast.makeText(
            context,
            text,
            Toast.LENGTH_LONG
        ).show()
    }


    companion object {
        const val ACTIVITY_ID_ARG: String = "activityId"
    }

}
