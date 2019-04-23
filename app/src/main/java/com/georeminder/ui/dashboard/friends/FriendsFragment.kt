package com.georeminder.ui.dashboard.friends

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.georeminder.R
import com.georeminder.base.BaseFragment
import com.georeminder.customView.BottomDialog
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.api.BaseResponse
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.DialogAddFriendBinding
import com.georeminder.databinding.FragmentRequestsBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent
import com.georeminder.utils.AppUtils
import com.georeminder.utils.getTextValue
import com.georeminder.utils.validator.ValidationErrorModel
import javax.inject.Inject

/**
 * Created by Darshna Desai on 8/1/19.
 */
class FriendsFragment : BaseFragment<FriendsViewModel>(), TabLayout.BaseOnTabSelectedListener<TabLayout.Tab?> {

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var prefs: Prefs

    private lateinit var viewModels: FriendsViewModel
    private lateinit var binding: FragmentRequestsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModels.setInjectable(apiService, prefs)

        binding.newReminder.setOnClickListener { showSearchContactDialog() }

        setupTabLayout()
        binding.tabs.addOnTabSelectedListener(this)
        setCurrentTabFragment(0)

        setObservables()
    }

    private fun setObservables() {

        viewModels.getValidationError().observe({ this.lifecycle }, { validationErrorModel ->
            validationErrorModel?.let {
                AppUtils.showSnackBar(binding.root, getString(it.msg))
            }
        })

        viewModels.getAddFriendData().observe({ this.lifecycle }, { baseResponse ->
            baseResponse?.let {
                AppUtils.showSnackBar(binding.root, (it.message))
            }
        })
    }

    private fun showSearchContactDialog() {
        val bottomDialog = BottomDialog(context!!, R.style.DialogStyle)
        val dialogBinding = DialogAddFriendBinding.inflate(layoutInflater, null)
        dialogBinding.btnAdd.setOnClickListener {
            viewModels.checkValidation(dialogBinding.etEmail.getTextValue())
            bottomDialog.dismiss()
        }
        bottomDialog.setContentView(dialogBinding.root)
        bottomDialog.show()
    }

    override fun onAttach(context: Context?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        requestsComponent.injectFriendsFragment(this)
        super.onAttach(context)
    }

    override fun getViewModel(): FriendsViewModel {
        viewModels = ViewModelProviders.of(this).get(FriendsViewModel::class.java)
        return viewModels
    }

    private fun setupTabLayout() {
        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.title_friends)), true)
        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.title_requested)))
    }

    override fun onTabReselected(p0: TabLayout.Tab?) {
    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        setCurrentTabFragment(tab?.position!!)
    }

    private fun setCurrentTabFragment(tabPosition: Int) {
        when (tabPosition) {
            0 -> {
                // this.findNavController().navigate(R.id.requestToRequestList/*, bundle*/)
                //Navigation.createNavigateOnClickListener(R.id.requestToRequestList/*, bundle*/)
                replaceFragment(FriendsListFragment())
            }
            1 -> {
                replaceFragment(RequestedFriendListFragment())
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fm = activity?.supportFragmentManager
        val ft = fm?.beginTransaction()
        ft?.replace(R.id.frameContainer, fragment)
        ft?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        ft?.commit()
    }

    override fun internetErrorRetryClicked() {

    }
}