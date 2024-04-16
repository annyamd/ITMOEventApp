package org.itmo.itmoevent.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.itmo.itmoevent.EventApplication
import org.itmo.itmoevent.R
import org.itmo.itmoevent.databinding.ActivityMainBinding
import org.itmo.itmoevent.view.fragments.ActivityFragment
import org.itmo.itmoevent.view.fragments.EventFragment
import org.itmo.itmoevent.view.fragments.EventSectionFragment
import org.itmo.itmoevent.view.fragments.ManagementSectionFragment
import org.itmo.itmoevent.view.fragments.ProfileSectionFragment
import org.itmo.itmoevent.view.fragments.TaskSectionFragment
import org.itmo.itmoevent.viewmodel.MainViewModel
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {

    private var viewBinding: ActivityMainBinding? = null
    private val navFragmentsMap: Map<Int, Fragment> = mapOf(
        R.id.nav_item_events to EventSectionFragment(),
        R.id.nav_item_tasks to TaskSectionFragment(),
        R.id.nav_item_manage to ManagementSectionFragment(),
        R.id.nav_item_profile to ProfileSectionFragment()
    )

    private val mainViewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding!!.root)

        val application = application as? EventApplication
            ?: throw IllegalStateException("Application must be EventApplication implementation")
        val roleRepository = application.roleRepository

        lifecycleScope.launch {
            val privileges = roleRepository.loadSystemPrivileges()
            if (privileges == null) {
                Toast.makeText(
                    this@MainActivity,
                    "Не удалось получить системные привилегии",
                    Toast.LENGTH_SHORT
                ).show()
                this@MainActivity.finish()
            } else {

                supportFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.main_fragment_container, navFragmentsMap[R.id.nav_item_events]!!)
                    .addToBackStack(BACK_STACK_TAB_TAG)
                    .commit()


                if (savedInstanceState == null) {
                    viewBinding?.run {
                        mainBottomNavBar.setOnItemSelectedListener { item ->
                            supportFragmentManager.popBackStack(
                                BACK_STACK_TAB_TAG,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )
                            navFragmentsMap[item.itemId]?.run {
                                supportFragmentManager.beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.main_fragment_container, this)
                                    .addToBackStack(BACK_STACK_TAB_TAG)
                                    .commit()
                            }
                            true
                        }
                    }

                    mainViewModel.eventId.observe(this@MainActivity) {
                        val argBundle =
                            bundleOf(EventFragment.EVENT_ID_ARG to mainViewModel.eventId.value)
                        supportFragmentManager.beginTransaction()
                            .setReorderingAllowed(true)
                            .replace<EventFragment>(R.id.main_fragment_container, args = argBundle)
                            .addToBackStack(BACK_STACK_DETAILS_TAG)
                            .commit()
                    }

                    mainViewModel.activityId.observe(this@MainActivity) {
                        val argBundle =
                            bundleOf(ActivityFragment.ACTIVITY_ID_ARG to mainViewModel.activityId.value)
                        supportFragmentManager.beginTransaction()
                            .setReorderingAllowed(true)
                            .replace<ActivityFragment>(R.id.main_fragment_container, args = argBundle)
                            .addToBackStack(BACK_STACK_DETAILS_TAG)
                            .commit()
                    }
                }

                mainViewModel.exitIntended.observe(this@MainActivity) {
                    this@MainActivity.finish()
                }

            }
        }

    }

    companion object {
        private const val BACK_STACK_TAB_TAG: String = "tagSection"
        private const val BACK_STACK_DETAILS_TAG: String = "eventDetails"
    }

}
