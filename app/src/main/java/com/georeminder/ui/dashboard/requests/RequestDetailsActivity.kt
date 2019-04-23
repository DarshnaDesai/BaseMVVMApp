package com.georeminder.ui.dashboard.requests

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.InflateException
import android.view.View
import android.widget.Toast
import com.georeminder.GeoReminderApp
import com.georeminder.R
import com.georeminder.base.BaseActivity
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.other.ReminderData
import com.georeminder.data.model.other.RequestStateChange
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.ActivityRequestDetailsBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppConstants.EXTRA_REQ_STATUS_CHANGE
import com.georeminder.utils.AppUtils
import com.georeminder.utils.constants.RequestStatus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import javax.inject.Inject

/**
 * Created by Darshna Desai on 18/1/19.
 */
class RequestDetailsActivity : BaseActivity<RequestsViewModel>(), View.OnClickListener, OnMapReadyCallback {

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    private lateinit var viewModels: RequestsViewModel
    private lateinit var binding: ActivityRequestDetailsBinding

    private var map: GoogleMap? = null
    private lateinit var locationManager: LocationManager
    private val MY_LOCATION_REQUEST_CODE = 329
    private var position: Int = 0

    companion object {
        fun newIntent(context: Context, data: ReminderData, position: Int, isRequested: Boolean):
                Intent {
            val intent = Intent(context, RequestDetailsActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_DATA, data)
            intent.putExtra(AppConstants.EXTRA_IS_REQUESTED, isRequested)
            intent.putExtra(AppConstants.EXTRA_POSITION, position)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        requestsComponent.injectRequestDetail(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_request_details)
        binding.setLifecycleOwner(this)
        val data = intent.getParcelableExtra(AppConstants.EXTRA_DATA) as ReminderData
        binding.reminderData = data
        binding.isRequested = intent.getBooleanExtra(AppConstants.EXTRA_IS_REQUESTED, false)
        position = intent.getIntExtra(AppConstants.EXTRA_POSITION, 0)
        super.onCreate(savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)

        init()
    }

    override fun getViewModel(): RequestsViewModel {
        viewModels = ViewModelProviders.of(this).get(RequestsViewModel::class.java)
        return viewModels
    }

    private fun init() {
        setToolbarTitle(R.string.title_details)
        setToolbarLeftIcon(R.drawable.ic_down_arrow)
        viewModels.setInjectable(apiService, prefs)
        initializeMap()
        setObservables()
        setListeners()
    }

    private fun initializeMap() {
        try {
            MapsInitializer.initialize(this)
            binding.mapView.getMapAsync(this)
        } catch (e: InflateException) {
            AppUtils.logE("Map initialize", "Inflate exception")
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this
                        , android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        /*&& ActivityCompat.checkSelfPermission(context!!,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(/*android.Manifest.permission.ACCESS_COARSE_LOCATION, */android.Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_LOCATION_REQUEST_CODE)
        }
    }

    private fun setObservables() {
        viewModels.getBaseModel().observe({ this.lifecycle }, { baseModel ->
            baseModel?.let {
                AppUtils.showSnackBar(binding.root, baseModel.message)
            }
        })

        viewModels.getRequestStateModel().observe({ this.lifecycle }, { requestState ->
            requestState?.let {
                binding.reminderData?._status = it.status
                binding.executePendingBindings()
                addReminder(requestState)
            }
        })
    }

    private fun addReminder(it: RequestStateChange) {
        if (it.status == RequestStatus.AVAILABLE) {
            val reminder: ReminderData = binding.reminderData!!
            (applicationContext!! as GeoReminderApp).getRepository().add(reminder,
                    success = {
                        Toast.makeText(this@RequestDetailsActivity, "Success adding reminder ${reminder.title}", Toast.LENGTH_SHORT).show()
                    },
                    failure = {
                        Toast.makeText(this@RequestDetailsActivity, "Failure adding reminder ${reminder.title}", Toast.LENGTH_SHORT).show()
                    })
        }
    }

    private fun setListeners() {
        binding.btnAccept.setOnClickListener(this)
        binding.btnReject.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnAccept -> {
                viewModels.callUpdateRequestStatusApi(binding.reminderData?.id.toString(),
                        RequestStatus.AVAILABLE.toString(), position)
            }
            R.id.btnReject -> {
                viewModels.callUpdateRequestStatusApi(binding.reminderData?.id.toString(),
                        RequestStatus.REJECTED.toString(), position)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            onMapAndPermissionReady()
        }
    }

    private fun onMapAndPermissionReady() {
        if (map != null && ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map?.isMyLocationEnabled = true
            centerCameraToCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun centerCameraToCurrentLocation() {
        /*val bestProvider = locationManager.getBestProvider(Criteria(), false)
        val location = locationManager.getLastKnownLocation(bestProvider)
        //if (location != null) {
        if (binding.reminderData != null && binding.reminderData!!.latitude != null && binding.reminderData!!.longitude != null) {
             val latLng = LatLng(location.latitude, location.longitude)
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }*/

        map?.run {
            val latLng = LatLng(binding.reminderData!!.latitude?.toDouble()!!, binding.reminderData!!.longitude?.toDouble()!!)
            val markerOptions = MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            markerOptions.position(latLng)
            addMarker(markerOptions)
            // moveCamera(CameraUpdateFactory.newLatLng(latLng))
            animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        map?.run {
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
        }

        onMapAndPermissionReady()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun internetErrorRetryClicked() {
    }

    override fun onBackPressed() {
        val position = intent.getIntExtra(AppConstants.EXTRA_POSITION, 0)
        val status = binding.reminderData!!.status
        val requestStateChange = RequestStateChange(position, status)
        val intent = Intent()
        intent.putExtra(EXTRA_REQ_STATUS_CHANGE, requestStateChange)
        setResult(Activity.RESULT_OK, intent)
        finish()
        super.onBackPressed()
    }

}