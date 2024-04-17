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
        viewBinding.run {
            content.run {
                val formatter =
                    DateTimeFormatter.ofPattern(DisplayDateFormats.DATE_EVENT_FULL)
                eventTitle.text = title
                eventShortDesc.text = shortDesc
                eventDescLong.text = longDesc
                eventChipStatus.text = status
                eventChipTime.text = startDate.format(formatter)
                eventDetailsAge.text = participantAgeLowest.toString()
                eventDetailsParticipantsMax.text = participantLimit.toString()
                eventDetailsTimeHold.text =
                    context.getString(
                        R.string.event_duration,
                        startDate.format(formatter),
                        endDate.format(formatter)
                    )
                eventDetailsTimeRegister.text =
                    context.getString(
                        R.string.event_duration,
                        regStart.format(formatter),
                        regEnd.format(formatter)
                    )
                eventDetailsTimePrepare.text =
                    context.getString(
                        R.string.event_duration,
                        preparingStart.format(formatter),
                        preparingEnd.format(formatter)
                    )
            }
        }
    }
}