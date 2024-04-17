package org.itmo.itmoevent.view.fragments.base

import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.viewbinding.ViewBinding
import com.google.android.material.tabs.TabLayout
import org.itmo.itmoevent.viewmodel.ContentItemLiveDataProvider

abstract class BaseFragment<T : ViewBinding> : EventAppFragment<T>() {

    protected fun <T> handleContentItemViewByLiveData(
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

    protected fun show(view: View?) {
        view?.visibility = View.VISIBLE
    }

    protected fun hide(view: View?) {
        view?.visibility = TabLayout.GONE
    }

    protected fun showShortToast(text: String) {
        Toast.makeText(
            context,
            text,
            Toast.LENGTH_LONG
        ).show()
    }

}
