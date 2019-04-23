package com.georeminder.ui.dashboard.requests

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.georeminder.R
import com.georeminder.base.BaseActivity
import com.georeminder.base.BaseBindingAdapter
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.model.other.FriendRequest
import com.georeminder.data.model.other.ReminderType
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.ActivityAddRequestBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent
import com.georeminder.ui.dialogs.ListDialog
import com.georeminder.utils.AppConstants
import com.georeminder.utils.AppConstants.EXTRA_REQUEST_CODE
import com.georeminder.utils.AppUtils
import com.georeminder.utils.getTextValue
import com.google.android.gms.location.places.ui.PlacePicker
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


/**
 * Created by Darshna Desai on 8/1/19.
 */
class AddRequestActivity : BaseActivity<RequestsViewModel>(), View.OnClickListener, BaseBindingAdapter.ItemClickListener<String> {

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    private lateinit var vModel: RequestsViewModel
    private lateinit var binding: ActivityAddRequestBinding
    private var selectedType: Int = 1
    private val PLACE_PICKER_REQUEST: Int = 321
    private var latitude = "0.0"
    private var longitude = "0.0"
    private var selectedAssignTo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        requestsComponent.injectToAddRequest(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_request)
        super.onCreate(savedInstanceState)

        init()
    }

    override fun getViewModel(): RequestsViewModel {
        vModel = ViewModelProviders.of(this).get(RequestsViewModel::class.java)
        return vModel
    }

    private fun init() {
        setToolbarTitle(R.string.title_add_request)
        setToolbarLeftIcon(R.drawable.ic_down_arrow)
        vModel.setInjectable(apiService, prefs)

        //val flowStepNumber = intent.extras.getString("argtest")

        /* val safeArgs = HomeFragmentArgs.fromBundle(intent.extras)
         val flowStepNumber = safeArgs.argtest

         Toast.makeText(this, flowStepNumber, Toast.LENGTH_SHORT).show()*/

        val max = 900
        val min = 200

        binding.fluidSlider.startText = "$min"
        binding.fluidSlider.endText = "$max"

        val total = max - min

        binding.fluidSlider.positionListener = { pos -> binding.fluidSlider.bubbleText = "${min + (total * pos).toInt()}" }
        binding.fluidSlider.position = 0.3f

        setObservables()

        binding.rvReminderItems.layoutManager = LinearLayoutManager(applicationContext)
        val adapter = AddRequestItemAdapter(applicationContext)
        binding.rvReminderItems.adapter = adapter
        adapter.itemClickListener = this

        setListeners()
    }

    private fun setObservables() {
        vModel.getBaseModel().observe({ this.lifecycle }, { baseModel ->
            baseModel?.let {
                AppUtils.showSnackBar(binding.root, baseModel.message)
            }
        })

        vModel.getValidationError().observe({ this.lifecycle }, { validationError ->
            validationError?.let {
                AppUtils.showSnackBar(binding.root, getString(validationError.msg))
            }
        })
    }

    private fun setListeners() {
        binding.etType.setOnClickListener(this)
        binding.etPickLocation.setOnClickListener(this)
        binding.etAssignTo.setOnClickListener(this)
        binding.tvReminderItem.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.etType -> {
                val reminderType: ArrayList<ReminderType> = ArrayList()
                reminderType.add(ReminderType(1, "Food"))
                reminderType.add(ReminderType(2, "Medicines"))
                reminderType.add(ReminderType(3, "Grocery"))
                reminderType.add(ReminderType(4, "Other"))
                showSpinnerDialog(reminderType as ArrayList<Any>)
            }
            R.id.etPickLocation -> {
                // val latLngBounds = LatLngBounds(LatLng(47.64299816, -122.14351988), LatLng(47.64299816, -122.14351988))
                val builder = PlacePicker.IntentBuilder()
                // builder.setLatLngBounds(latLngBounds)
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)

            }
            R.id.tvReminderItem -> {
                (binding.rvReminderItems.adapter as AddRequestItemAdapter).addItemNotify(" ")
            }
            R.id.etAssignTo -> {
                startActivityForResult(AssignToActivity.newIntent(this), AppConstants.EXTRA_REQUEST_CODE)
            }
        }
    }

    override fun onItemClick(view: View, data: String, position: Int) {
        Log.e("POSITION", "=$position")
        (binding.rvReminderItems.adapter as AddRequestItemAdapter).removeItem(position)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PLACE_PICKER_REQUEST -> {
                    val selectedPlace = PlacePicker.getPlace(this, data)
                    binding.etPickLocation.setText(selectedPlace.address)
                    selectedPlace.latLng?.let {
                        latitude = selectedPlace.latLng.latitude.toString()
                        longitude = selectedPlace.latLng.longitude.toString()
                    }
                }
                EXTRA_REQUEST_CODE -> {
                    val friendRequest = data?.getParcelableExtra<FriendRequest>(AppConstants
                            .EXTRA_REQ_STATUS_CHANGE)
                    selectedAssignTo = friendRequest?.user_id!!
                    binding.etAssignTo.setText("${friendRequest.user_first_name} ${friendRequest.user_last_name}")
                }
            }
        }
    }

    private fun showSpinnerDialog(data: ArrayList<Any>) {
        val contentView = View.inflate(this@AddRequestActivity, R.layout.dialog_list, null)
        val dialog = ListDialog(this@AddRequestActivity, getString(R.string.app_name), false, data, object : ListDialog.OnItemClick {
            override fun selectedItem(position: Int, selectedItem: Any) {
                if (selectedItem is ReminderType) {
                    selectedType = selectedItem.id
                    binding.etType.setText(selectedItem.type)
                }
            }
        })
        dialog.setContentView(contentView)
        dialog.show()
    }

    fun onSubmit(view: View) {
        val progress = binding.fluidSlider.bubbleText

        vModel.checkValidation(selectedAssignTo, binding.etTitle.getTextValue(), binding.etDesc.getTextValue()
                , binding.etPickLocation.getTextValue(), getReminderItems(), latitude
                , longitude, progress!!, selectedType.toString())
    }

    override fun internetErrorRetryClicked() {
        onSubmit(binding.root)
    }

    private fun getReminderItems(): String {
        var items = ""
        val data = (binding.rvReminderItems.adapter as AddRequestItemAdapter).getData()
        /*val strings = ArrayList<String>()
        for ((i, j) in data.withIndex()) {
            strings.add(((binding.rvReminderItems).findViewHolderForAdapterPosition(i) as ItemAddReminderBinding).etReminderItem.text.toString())
        }*/
        //   items = TextUtils.join(",", data)
        items = data.asSequence().filter { it.trim() != "" }.joinToString(",")
        // val output = data.joinToString("|", prefix = "<", postfix = ">", limit = 3, truncated = "...more...")
        return items
    }

}