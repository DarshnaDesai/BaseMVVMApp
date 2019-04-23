package com.georeminder.ui.login

import android.animation.LayoutTransition
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.view.ContextThemeWrapper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextSwitcher
import android.widget.TextView
import com.georeminder.R
import com.georeminder.base.BaseFragment
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.databinding.FragmentLoginBinding
import com.georeminder.di.component.DaggerLoginComponent
import com.georeminder.di.component.LoginComponent
import com.georeminder.di.module.LoginModule
import com.georeminder.ui.dashboard.DashboardActivity
import com.georeminder.ui.dialogs.ListDialog
import com.georeminder.ui.forgotpassword.ForgotPasswordActivity
import com.georeminder.utils.AppUtils
import com.georeminder.utils.getTextValue
import java.util.*
import javax.inject.Inject


/**
 * Created by Darshna Desai on 17/12/18.
 */
class LoginFragment : BaseFragment<LoginViewModel>(), View.OnClickListener {

    @Inject
    lateinit var prefs: Prefs

    @Inject
    internal lateinit var viewModel: LoginViewModel

    private lateinit var binding: FragmentLoginBinding
    private var isLogin = true
    private var selectedDate = Date().time / 1000
    private var selectedGender = 1

    companion object {
        private var fragment: LoginFragment? = null

        fun newInstance(): Fragment {
            if (fragment == null) fragment = LoginFragment()
            return fragment!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.frag = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onAttach(context: Context?) {
        val loginComponent: LoginComponent = DaggerLoginComponent
                .builder()
                .networkComponent(getNetworkComponent())
                .localDataComponent(getLocalDataComponent())
                .loginModule(LoginModule(this))
                .build()
        loginComponent.injectLoginFragment(this)
        super.onAttach(context)
    }

    override fun getViewModel(): LoginViewModel {
        return viewModel
    }

    private fun init() {
        val drawable = binding.ivLogo.drawable
        if (drawable is Animatable) {
            (drawable as Animatable).start()
        }

        setObservables()

        setTextSwitchers(binding.textView, getString(R.string.msg_dont_have_account))
        setTextSwitchers(binding.tvSign, getString(R.string.msg_sign_in_continue))
        ((binding.llMain) as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        setListeners()
    }

    private fun setObservables() {
        viewModel.getUserModel().observe({ this@LoginFragment.lifecycle }, { userModel ->
            userModel?.let {
                AppUtils.showSnackBar(binding.root, it.message)
                startActivity(DashboardActivity.newIntent(context!!))
                activity?.finish()
            }
        })

        viewModel.getValidationError().observe({ this@LoginFragment.lifecycle }, { validationError ->
            validationError?.let {
                AppUtils.showSnackBar(binding.root, getString(validationError.msg))
            }
        })
    }

    private fun setListeners() {
        binding.etDob.setOnClickListener(this)
        binding.etGender.setOnClickListener(this)
    }

    private fun setTextSwitchers(view: TextSwitcher, text: String) {
        view.setFactory {
            TextView(ContextThemeWrapper(activity, R.style.myTextStyle), null, 0)
        }
        val inAnim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        val outAnim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
        inAnim.duration = 300
        outAnim.duration = 300
        view.inAnimation = inAnim
        view.outAnimation = outAnim
        view.setText(text)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.etDob -> {
                val newCalendar = Calendar.getInstance()
                val fromDatePickerDialog = DatePickerDialog(context!!, DatePickerDialog.OnDateSetListener { view, year
                                                                                                            , monthOfYear, dayOfMonth ->
                    val newDate = Calendar.getInstance()
                    newDate.set(year, monthOfYear, dayOfMonth)
                    selectedDate = newDate.time.time / 1000
                    binding.etDob.setText(AppUtils.formatDate(newDate.time))
                }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH))
                fromDatePickerDialog.datePicker.maxDate = Date().time
                fromDatePickerDialog.show()
            }
            R.id.etGender -> {
                //val gender = Arrays.asList(resources.getStringArray(R.array.array_gender))
                //showSpinnerDialog(gender)
                showSpinnerDialog(arrayListOf("Male", "Female"))
            }
        }
    }

    private fun showSpinnerDialog(data: ArrayList<Any>) {
        val contentView = View.inflate(context, R.layout.dialog_list, null)
        val dialog = ListDialog(context!!, getString(R.string.app_name), false, data, object : ListDialog.OnItemClick {
            override fun selectedItem(position: Int, selectedItem: Any) {
                selectedGender = position + 1
                binding.etGender.setText(selectedItem.toString())
            }
        })
        dialog.setContentView(contentView)
        dialog.show()
    }

    fun onSubmit() {
        if (!isLogin) {
            viewModel.checkSignUpValidation(binding.etFname.getTextValue(), binding.etLname.getTextValue(), binding.etEmail.getTextValue()
                    , binding.etPassword.getTextValue(), selectedDate.toString(), selectedGender.toString()
                    , binding.etPhone.getTextValue())
        } else {
            viewModel.checkSignInValidation(binding.etEmail.getTextValue(), binding.etPassword.getTextValue())
        }
    }

    fun onForgotPassword() {
        startActivity(ForgotPasswordActivity.newIntent(context!!))
    }

    fun onChangePage() {
        isLogin = !isLogin
        if (isLogin) {
            showHideView(View.VISIBLE, View.GONE, getString(R.string.msg_dont_have_account), getString(R.string.msg_sign_in_continue)
                    , getString(R.string.action_sign_in))
        } else {
            showHideView(View.GONE, View.VISIBLE, getString(R.string.msg_already_have_account), getString(R.string.msg_sign_up_continue)
                    , getString(R.string.action_sign_up))
        }
    }

    private fun showHideView(signInVisibility: Int, signUpVisibility: Int, bottomText: String, topText: String, buttonText: String) {
        binding.ivLogo.visibility = signInVisibility
        binding.tvForgotPassword.visibility = signInVisibility

        binding.etFname.visibility = signUpVisibility
        binding.etLname.visibility = signUpVisibility
        binding.etDob.visibility = signUpVisibility
        binding.etGender.visibility = signUpVisibility
        binding.etPhone.visibility = signUpVisibility

        binding.textView.setText(bottomText)
        binding.tvSign.setText(topText)
        binding.btnSign.text = buttonText
    }

    override fun internetErrorRetryClicked() {
        onSubmit()
    }

}