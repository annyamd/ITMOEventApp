package org.itmo.itmoevent.view.fragments.binding

import androidx.viewbinding.ViewBinding

interface ContentBinding<T : ViewBinding, E> {
    fun bindContentToView(viewBinding: T, content: E)
}
