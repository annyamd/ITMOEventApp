package org.itmo.itmoevent.view.fragments.base

import androidx.viewbinding.ViewBinding
import org.itmo.itmoevent.EventApplication

abstract class EventAppFragment<T : ViewBinding> : ViewBindingFragment<T>() {
    protected val application: EventApplication
        get() = requireActivity().application as? EventApplication
        ?: throw IllegalStateException("Application must be EventApplication implementation")
}
