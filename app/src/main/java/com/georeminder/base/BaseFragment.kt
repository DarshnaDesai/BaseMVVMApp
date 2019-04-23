package com.georeminder.base

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.georeminder.GeoReminderApp
import com.georeminder.R
import com.georeminder.utils.AppUtils

/**
 * Created by Darshna Desai on 17/12/18.
 */
abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    private lateinit var mViewModel: T
    private var progressDialog: Dialog? = null
    private var errorSnackbar: Snackbar? = null
    private val errorClickListener = View.OnClickListener { internetErrorRetryClicked() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = getViewModel()

        mViewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (errorMessage != null) showError(errorMessage) else hideError()
        })

        mViewModel.loadingVisibility.observe(this, Observer { loadingVisibility ->
            loadingVisibility?.let { if (loadingVisibility) showProgress() else hideProgress() }
        })

        mViewModel.apiErrorMessage.observe(this, Observer { apiError ->
            apiError?.let { AppUtils.showSnackBar(this.view!!, it) }
        })

    }

    private fun showError(@StringRes errorMessage: Int) {
        errorSnackbar = Snackbar.make(this.view!!, errorMessage, Snackbar.LENGTH_LONG)
        errorSnackbar?.setAction(R.string.action_retry, errorClickListener)
        errorSnackbar?.show()
    }

    private fun hideError() {
        errorSnackbar?.dismiss()
    }

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract fun getViewModel(): T

    abstract fun internetErrorRetryClicked()

    /* [START] show progress bar*/
    @SuppressLint("InflateParams")
    fun showProgress() {
        if (progressDialog == null) {
            progressDialog = Dialog(context)
            // dialog without title
            progressDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

        // inflating and seeting view of custom dialog
        val view = LayoutInflater.from(context).inflate(R.layout.app_loading_dialog, null, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView2)

        // applying rotate animation
        ObjectAnimator.ofFloat(imageView, View.ROTATION, 360f, 0f).apply {
            repeatCount = ObjectAnimator.INFINITE
            duration = 1500
            interpolator = LinearInterpolator()
            start()
        }
        progressDialog?.setContentView(view)

        // setting background of dialog as transparent
        val window = progressDialog?.window
        window?.setBackgroundDrawable(ContextCompat.getDrawable(context!!, android.R.color
                .transparent))

        // preventing outside touch and setting cancelable false
        progressDialog?.setCancelable(false)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.show()
    }
    /* [END] show progress bar*/

    private fun hideProgress() {
        progressDialog?.dismiss()
    }

    /* [START] Check if an active internet connection is present or not*/
    /* return boolean value true or false */
    fun isInternetAvailable(): Boolean {
        // getting Connectivity service as ConnectivityManager
        return AppUtils.hasInternet(context!!)
    }

    private fun getApplication() = (activity?.applicationContext as GeoReminderApp)

    fun getAppComponent() = getApplication().getAppComponent()

    fun getLocalDataComponent() = getApplication().getLocalDataComponent()

    fun getNetworkComponent() = getApplication().getNetworkComponent()
}