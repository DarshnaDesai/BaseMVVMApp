package com.georeminder.ui.dashboard.home

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.georeminder.GeoReminderApp
import com.georeminder.R
import com.georeminder.base.BaseFragment
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.other.ReminderData
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.FragmentHomeBinding
import com.georeminder.di.component.DaggerHomeComponent
import com.georeminder.di.component.HomeComponent
import com.georeminder.di.module.HomeModule
import com.georeminder.utils.AppUtils
import com.georeminder.utils.ReminderRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import javax.inject.Inject


/**
 * Created by Darshna Desai on 7/1/19.
 */
class HomeFragment : BaseFragment<HomeViewModel>(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    @Inject
    internal lateinit var viewModel: HomeViewModel

    private lateinit var binding: FragmentHomeBinding
    private var map: GoogleMap? = null
    private lateinit var locationManager: LocationManager
    private val MY_LOCATION_REQUEST_CODE = 329

    private val observer = Observer<String?> { string ->
        if (string != null) {
            AppUtils.showSnackBar(binding.root, string)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        MapsInitializer.initialize(this.activity!!)
        binding.map.onCreate(savedInstanceState)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getData().observe(viewLifecycleOwner, observer)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            binding.map.getMapAsync(this)
        } catch (e: InflateException) {
            AppUtils.logE("Map initialize", "Inflate exception")
        }

        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(context!!,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        /*&& ActivityCompat.checkSelfPermission(context!!,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
            requestPermissions(
                    arrayOf(/*android.Manifest.permission.ACCESS_COARSE_LOCATION, */android.Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_LOCATION_REQUEST_CODE)
        }


        Log.e("Home", "onViewCreated")
        viewModel.setInjectable(apiService, prefs)
        /*val bundle = Bundle()
       bundle.putString("argtest", "DDD")*/
        binding.newReminder.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.homeToAddRequest/*, bundle*/))

        setObservables()

        //viewModel.getData().observe(this, observer)

        /* tvLabel1.setOnClickListener {
             viewModel.setData("Home data")
         }*/
    }

    private fun setObservables() {
        viewModel.getRequestData().observe({ this.lifecycle }, { requestData ->
            requestData?.let {
                // AppUtils.showSnackBar(binding.root, requestData.message)
                showReminders(it.reminderData)
            }
        })
    }

    override fun onAttach(context: Context?) {
        val homeComponent: HomeComponent = DaggerHomeComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .homeModule(HomeModule(this))
                .build()
        homeComponent.injectHomeFragment(this)
        super.onAttach(context)
    }

    override fun getViewModel(): HomeViewModel {
        return viewModel
    }

    override fun internetErrorRetryClicked() {
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            onMapAndPermissionReady()
        }
    }

    private fun onMapAndPermissionReady() {
        if (map != null && ActivityCompat.checkSelfPermission(context!!,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map?.isMyLocationEnabled = true
            binding.currentLocation.setOnClickListener {
                centerCameraToCurrentLocation()
            }

            viewModel.callRequestListApi()
            centerCamera()
            centerCameraToCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun centerCameraToCurrentLocation() {
        val bestProvider = locationManager.getBestProvider(Criteria(), false)
        val location = locationManager.getLastKnownLocation(bestProvider)
        if (location != null) {
            val latLng = LatLng(location.latitude, location.longitude)
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    private fun centerCamera() {
        /*if (intent.extras != null && intent.extras.containsKey(EXTRA_LAT_LNG)) {
            val latLng = intent.extras.get(EXTRA_LAT_LNG) as LatLng
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }*/
    }

    private fun showReminders(reminderData: ArrayList<ReminderData>) {
        for (reminder in reminderData) {
            createMarker(reminder.latitude, reminder.longitude, R.drawable.location_b_view_map)

            /*(context?.applicationContext!! as GeoReminderApp).getRepository().add(reminder,
                    success = {
                        Toast.makeText(context, "Success adding reminder ${reminder.title}", Toast.LENGTH_SHORT).show()
                    },
                    failure = {
                        Toast.makeText(context, "Failure adding reminder ${reminder.title}", Toast.LENGTH_SHORT).show()
                    })*/
        }
        /*map?.run {
            clear()
            for (reminder in getRepository().getAll()) {
                showReminderInMap(this@MainActivity, this, reminder)
            }
        }*/
    }

    private fun createMarker(latitude: String?, longitude: String?, iconResID: Int): Marker? {
        val vectorToBitmap = AppUtils.vectorToBitmap(context?.resources!!, R.drawable.ic_heart)

        return map?.addMarker(MarkerOptions()
                .position(LatLng(latitude?.toDouble()!!, longitude?.toDouble()!!))
                // .anchor(0.5f, 0.5f)
                //.title(title)
                //.snippet(snippet)
                .icon(vectorToBitmap))/*BitmapDescriptorFactory.fromResource(iconResID)*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("Home", "onDestroyView")
        viewModel.getData().removeObserver(observer)
        viewModel.onCleared()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        map?.run {
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            setOnMarkerClickListener(this@HomeFragment)
        }

        onMapAndPermissionReady()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        /* val reminder = getRepository().get(marker.tag as String)
         if (reminder != null) {
             showReminderRemoveAlert(reminder)
         }*/

        return true
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.map.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

}