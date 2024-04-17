package org.itmo.itmoevent.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.itmo.itmoevent.databinding.EventsListItemBinding
import org.itmo.itmoevent.model.data.entity.EventShort
import org.itmo.itmoevent.view.util.DisplayDateFormats
import java.time.format.DateTimeFormatter


class EventAdapter(
    private val onEventListClickListener: OnEventListClickListener
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    var eventList: List<EventShort> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class EventViewHolder(val binding: EventsListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewBinding = EventsListItemBinding.inflate(inflater, parent, false)
        return EventViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.binding.run {
            val event = eventList[position]
            eventItemHeader.text = event.title
            eventItemDesc.text = event.shortDesc
            eventItemStatus.text = event.status
            val formatter = DateTimeFormatter.ofPattern(DisplayDateFormats.DATE_EVENT_SHORT)
            eventItemTime.text = event.start?.format(formatter) ?: "Не выбрано"

            this.root.setOnClickListener {
                onEventListClickListener.onEventClicked(event.id)
            }
        }
    }

    interface OnEventListClickListener {
        fun onEventClicked(eventId: Int)
    }

}
