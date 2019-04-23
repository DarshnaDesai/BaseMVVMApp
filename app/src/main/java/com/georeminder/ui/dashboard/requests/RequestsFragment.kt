package com.georeminder.ui.dashboard.requests

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.georeminder.R
import com.georeminder.base.BaseFragment
import com.georeminder.databinding.FragmentRequestsBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent


/**
 * Created by Darshna Desai on 8/1/19.
 */
class RequestsFragment : BaseFragment<RequestsViewModel>(), TabLayout.BaseOnTabSelectedListener<TabLayout.Tab?> {

    private lateinit var viewModels: RequestsViewModel
    private lateinit var binding: FragmentRequestsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // binding.tvLabel.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_navigationRequestsFragment_to_navigationHomeFragment/*, bundle*/))

        /*val bundle = Bundle()
     bundle.putString("argtest", "DDD")*/
        binding.newReminder.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.requestListToAddRequest/*, bundle*/))

        setupTabLayout()
        binding.tabs.addOnTabSelectedListener(this)
        setCurrentTabFragment(0)
    }

    override fun onAttach(context: Context?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        requestsComponent.injectRequestFragment(this)
        super.onAttach(context)
    }

    override fun getViewModel(): RequestsViewModel {
        viewModels = ViewModelProviders.of(this).get(RequestsViewModel::class.java)
        return viewModels
    }

    private fun setupTabLayout() {
        binding.tabs.addTab(binding.tabs.newTab().setText(getString(R.string.title_received)), true)
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
        val fragment = ReceivedRequestsFragment()
        when (tabPosition) {
            0 -> {
                // this.findNavController().navigate(R.id.requestToRequestList/*, bundle*/)
                //Navigation.createNavigateOnClickListener(R.id.requestToRequestList/*, bundle*/)
                replaceFragment(fragment)
            }
            1 -> {
                //this.findNavController().navigate(R.id.requestToRequestList/*, bundle*/)
                // Navigation.createNavigateOnClickListener(R.id.requestToRequestList/*, bundle*/)
                fragment.isRequested = true
                replaceFragment(fragment)
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