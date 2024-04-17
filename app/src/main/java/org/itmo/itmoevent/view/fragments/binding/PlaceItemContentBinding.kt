package org.itmo.itmoevent.view.fragments.binding

import org.itmo.itmoevent.databinding.PlaceItemBinding
import org.itmo.itmoevent.model.data.entity.Place

class PlaceItemContentBinding : ContentBinding<PlaceItemBinding, Place> {
    override fun bindContentToView(viewBinding: PlaceItemBinding, content: Place) {
        viewBinding.run {
            placeItemTitle.text = content.name
            placeItemDesc.text = content.description
            placeItemFormat.text = content.format
        }
    }

}