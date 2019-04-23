package com.georeminder.ui.dashboard.profile

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.view.KeyEvent.ACTION_DOWN
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.georeminder.R
import com.georeminder.base.BaseFragment
import com.georeminder.customView.BottomDialog
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.data.remote.ApiService
import com.georeminder.databinding.DialogFileChooseBinding
import com.georeminder.databinding.FragmentProfileBinding
import com.georeminder.di.component.DaggerNetworkLocalComponent
import com.georeminder.di.component.NetworkLocalComponent
import com.georeminder.ui.dialogs.ListDialog
import com.georeminder.utils.AppConstants.PICK_IMAGE_CAMERA_REQUEST_CODE
import com.georeminder.utils.AppConstants.PICK_IMAGE_GALLERY_REQUEST_CODE
import com.georeminder.utils.AppUtils
import com.georeminder.utils.filePick.FilePickUtils
import com.georeminder.utils.getTextValue
import com.georeminder.utils.nonEditable
import java.util.*
import javax.inject.Inject

/**
 * Created by Darshna Desai on 8/1/19.
 */
class ProfileFragment : BaseFragment<ProfileViewModel>(), View.OnTouchListener, ListDialog.OnItemClick {

    override fun internetErrorRetryClicked() {

    }

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var apiService: ApiService

    lateinit var binding: FragmentProfileBinding
    lateinit var vModel: ProfileViewModel

    private var isEditMode = false
    private var selectedDate = Date().time / 1000
    private var selectedGender = 1
    private var filePickUtils: FilePickUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val requestsComponent: NetworkLocalComponent = DaggerNetworkLocalComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .build()
        requestsComponent.injectProfile(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.frag = this
        binding.viewModel = vModel
        binding.setLifecycleOwner(this)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        binding.etGender.setOnTouchListener(this)
        binding.etDob.setOnTouchListener(this)
        binding.tvChangePassword.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.actionToChangePassword))
    }

    private val mOnFileChoose = object : FilePickUtils.OnFileChoose {
        override fun onFileChoose(fileUri: String, requestCode: Int) {
            Glide.with(context!!).load(fileUri).into(binding.ivProfileImage)
            vModel.imagePath = fileUri
        }
    }

    private fun init() {
        filePickUtils = FilePickUtils(activity!!, mOnFileChoose)
        vModel.setInjectable(apiService, prefs)
        setObservables()
        vModel.getProfile()
    }


    private fun setObservables() {
        vModel.getUserDataModel().observe({ this.lifecycle }, { baseModel ->
            baseModel?.let {
                //AppUtils.showSnackBar(binding.root, baseModel.message)
            }
        })
    }

    /**
     * On change between profile to editable profile.
     */
    fun onSwitchChange() {
        if (binding.viewSwitcher.currentView == binding.tvName) {
            isEditMode = true
            binding.tvEdit.text = getString(R.string.action_submit)
            updateClick(binding.inputPhone, binding.inputGender, binding.inputDob, true)
        } else {
            isEditMode = false
            binding.tvEdit.text = getString(R.string.action_edit)
            updateClick(binding.inputPhone, binding.inputGender, binding.inputDob, false)
            vModel.updateProfile(binding.etName.getTextValue(), binding.etGender.getTextValue(),
                    binding.etPhone.getTextValue(), selectedDate.toString())
        }
        binding.viewSwitcher.showNext()
    }

    /**
     * navigate to change password fragment.
     */
    fun onChangePassword(): View.OnClickListener {
        return Navigation.createNavigateOnClickListener(R.id.actionToChangePassword)
    }

    /**
     * on pick image for change profile image.
     */
    fun onChangeProfileImage() {
        if (isEditMode) {
            val bottomDialog = BottomDialog(context!!, R.style.DialogStyle)
            val dialogBinding = DialogFileChooseBinding.inflate(layoutInflater, null)
            dialogBinding.tvCamera.setOnClickListener {
                onPickImgCamera()
                bottomDialog.dismiss()
            }
            dialogBinding.tvGallery.setOnClickListener {
                onPickImgGallery()
                bottomDialog.dismiss()
            }
            bottomDialog.setContentView(dialogBinding.root)
            bottomDialog.show()
        }
    }

    /**
     * pick image from camera
     */
    private fun onPickImgCamera() {
        filePickUtils?.requestImageCamera(PICK_IMAGE_CAMERA_REQUEST_CODE, false, false)
    }

    /**
     * pic image from Gallery
     */
    private fun onPickImgGallery() {
        filePickUtils?.requestImageGallery(PICK_IMAGE_GALLERY_REQUEST_CODE, false, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        filePickUtils?.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * When user switch view from profile to edit profile
     * or Edit Profile to Profile.
     */
    private fun updateClick(phoneInput: TextInputLayout, genderInput: TextInputLayout,
                            dobInput: TextInputLayout, isClickable: Boolean) {
        phoneInput.nonEditable(isClickable)
        genderInput.nonEditable(isClickable)
        dobInput.nonEditable(isClickable)
    }

    private fun showSpinnerDialog(data: ArrayList<Any>) {
        val contentView = View.inflate(context, R.layout.dialog_list, null)
        val dialog = ListDialog(context!!, getString(R.string.app_name), false, data, this)
        dialog.setContentView(contentView)
        dialog.show()
    }

    override fun getViewModel(): ProfileViewModel {
        vModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        return vModel
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     * the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (isEditMode && event?.action == ACTION_DOWN)
            when (v?.id) {
                R.id.etGender ->
                    showSpinnerDialog(arrayListOf("Male", "Female"))
                R.id.etDob -> {
                    val newCalendar = Calendar.getInstance()
                    val fromDatePickerDialog = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        val newDate = Calendar.getInstance()
                        newDate.set(year, monthOfYear, dayOfMonth)
                        selectedDate = newDate.time.time / 1000
                        binding.etDob.setText(AppUtils.formatDate(newDate.time))
                    }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH))
                    fromDatePickerDialog.datePicker.maxDate = Date().time
                    fromDatePickerDialog.show()
                }
            }
        return true
    }

    /**
     * On selectdItem for Gender List
     */
    override fun selectedItem(position: Int, selectedItem: Any) {
        selectedGender = position + 1
        binding.etGender.setText(selectedItem.toString())
    }
}