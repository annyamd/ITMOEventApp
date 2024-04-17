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
import org.itmo.itmoevent.databinding.FragmentPlaceBinding
import org.itmo.itmoevent.model.data.entity.Place
import org.itmo.itmoevent.viewmodel.ContentItemLiveDataProvider
import org.itmo.itmoevent.viewmodel.PlaceViewModel

class PlaceFragment : Fragment(R.layout.fragment_place) {
    private var viewBinding: FragmentPlaceBinding? = null
    private var placeId: Int? = null
    private var model: PlaceViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentPlaceBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val placeId = requireArguments().getInt(PLACE_ID_ARG)
        this.placeId = placeId

        val model: PlaceViewModel by viewModels {
            val application = requireActivity().application as? EventApplication
                ?: throw IllegalStateException("Application must be EventApplication implementation")
            PlaceViewModel.PlaceViewModelModelFactory(
                placeId,
                application.placeRepository,
                application.roleRepository
            )
        }
        this.model = model

        viewBinding?.run {
            handleContentItemViewByLiveData<Place>(
                model.placeLiveData,
                placeContent,
                placeProgressBarMain.root
            ) { place ->
                placeName.text = place.name
                placeAddress.text = place.description
                placeFormat.text = place.format
                placeRoom.text = place.room
                placeDesc.text = place.description
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
        const val PLACE_ID_ARG: String = "placeId"
    }

}