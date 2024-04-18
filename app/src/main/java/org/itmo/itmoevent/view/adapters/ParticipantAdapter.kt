package org.itmo.itmoevent.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.itmo.itmoevent.databinding.ParticipantListItemBinding
import org.itmo.itmoevent.model.data.entity.Participant

class ParticipantAdapter(private val onParticipantMarkChangeListener: (Int, Boolean) -> Unit) :
    RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder>() {

    var participantList: List<Participant> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ParticipantViewHolder(val binding: ParticipantListItemBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewBinding = ParticipantListItemBinding.inflate(inflater, parent, false)
        return ParticipantViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return participantList.size
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val participant = participantList[position]

        holder.binding.run {
            participantItemName.text = participant.name
            participantItemLogin.text = participant.email
            participantItemMarkSwitch.isChecked = participant.visited

            participantItemMarkSwitch.setOnCheckedChangeListener { _, isChecked ->
                onParticipantMarkChangeListener.invoke(participant.id, isChecked)
            }
        }
    }

}
