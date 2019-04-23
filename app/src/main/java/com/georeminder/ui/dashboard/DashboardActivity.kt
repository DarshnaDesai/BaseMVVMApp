package com.georeminder.ui.dashboard

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.georeminder.R
import com.georeminder.databinding.ActivityDashboardBinding
import com.georeminder.ui.dashboard.profile.ProfileFragment

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        if(intent.extras != null){
            Log.e("From notification", intent.extras.getString("FromNotification"))
        }
        setupNavigation()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, DashboardActivity::class.java)
        }

        fun start(context: Context) {
            val intent = Intent(context, DashboardActivity::class.java)
            context.startActivity(intent)
        }
    }

    private fun setupNavigation() {
        val navController = Navigation.findNavController(this, R.id.navigationContainer)
        NavigationUI.setupWithNavController(binding.navigation, navController)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.navigationHomeFragment -> {
                Toast.makeText(baseContext, "Home Fragment", Toast.LENGTH_SHORT).show()
            }
            R.id.navigationRequestsFragment -> {
                Toast.makeText(baseContext, "Requests Fragment", Toast.LENGTH_SHORT).show()
            }
            R.id.navigationFriendsFragment -> {
                Toast.makeText(baseContext, "Friends Fragment", Toast.LENGTH_SHORT).show()
            }
        }
        return item?.onNavDestinationSelected(findNavController(R.id.navigationContainer))!!
                && super.onOptionsItemSelected(item)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = getCurrentFragment()
        if (fragment is ProfileFragment) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getCurrentFragment(): Fragment? {
        return (supportFragmentManager.findFragmentById(R.id.navigationContainer) as NavHostFragment)
                .childFragmentManager.fragments[0]
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("onNewIntent", "CALLED")
        if(intent?.hasExtra("FromNotification")!!){
            Log.e("From notification", intent.getStringExtra("FromNotification"))
        }
    }
}
