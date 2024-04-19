package org.itmo.itmoevent.view.fragments.binding

import android.content.Context
import org.itmo.itmoevent.R
import org.itmo.itmoevent.databinding.EventInfoBinding
import org.itmo.itmoevent.model.data.entity.Event
import org.itmo.itmoevent.view.util.DisplayDateFormats
import java.time.format.DateTimeFormatter

class EventInfoContentBinding(private val context: Context) :
    ContentBinding<EventInfoBinding, Event> {
    override fun bindContentToView(viewBinding: EventInfoBinding, content: Event) {
        val emptyFieldText = context.getString(R.string.not_filled)
        viewBinding.run {
            content.run {
                val formatter =
                    DateTimeFormatter.ofPattern(DisplayDateFormats.DATE_EVENT_FULL)
                eventTitle.text = title ?: emptyFieldText
                eventShortDesc.text = shortDesc ?: emptyFieldText
                eventDescLong.text = longDesc ?: emptyFieldText
                eventChipStatus.text = status ?: emptyFieldText
                eventChipTime.text = startDate?.format(formatter) ?: emptyFieldText
                eventDetailsAge.text = participantAgeLowest?.toString() ?: emptyFieldText
                eventDetailsParticipantsMax.text = participantLimit?.toString() ?: emptyFieldText
                eventDetailsTimeHold.text =
                    context.getString(
                        R.string.event_duration,
                        startDate?.format(formatter) ?: emptyFieldText,
                        endDate?.format(formatter) ?: emptyFieldText
                    )
                eventDetailsTimeRegister.text =
                    context.getString(
                        R.string.event_duration,
                        regStart?.format(formatter) ?: emptyFieldText,
                        regEnd?.format(formatter) ?: emptyFieldText
                    )
                eventDetailsTimePrepare.text =
                    context.getString(
                        R.string.event_duration,
                        preparingStart?.format(formatter) ?: emptyFieldText,
                        preparingEnd?.format(formatter) ?: emptyFieldText
                    )
            }
        }
    }
}