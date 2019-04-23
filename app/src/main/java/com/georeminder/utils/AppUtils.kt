package com.georeminder.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.annotation.DrawableRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.georeminder.BuildConfig
import com.georeminder.R
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.ui.login.LoginActivity
import com.georeminder.utils.constants.RequestStatus
import com.georeminder.utils.filePick.FileUri
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and

/**
 * Created by Darshna Desai on 19/12/18.
 */
object AppUtils {

    /**
     * A method which returns the state of internet connectivity of user's phone.
     */
    fun hasInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun logE(title: String = "GEO REMINDER", msg: String) {
        Log.e(title, msg)
    }

    /**
     * A common method used in whole application to show a snack bar
     */
    fun showSnackBar(v: View, msg: String) {
        val mSnackBar = Snackbar.make(v, msg, Snackbar.LENGTH_LONG)
        val view = mSnackBar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        //  params.gravity = Gravity.TOP
        view.layoutParams = params
        view.setBackgroundColor(Color.DKGRAY)
        val mainTextView = view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        // val mainTextView: TextView = view.findViewById(android.support.design.R.id.snackbar_text)
        mainTextView.setTextColor(Color.WHITE)
        mainTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, v.context.resources.getDimension(R.dimen.medium))
        mainTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        mainTextView.maxLines = 4
        mainTextView.gravity = Gravity.CENTER_HORIZONTAL
        mSnackBar.show()
    }

    /**
     * A common method to logout the user and redirect to login screen
     */
    fun logoutUser(context: Context?) {
        context?.let {
            Prefs.getInstance(it)?.userDataModel = null
            Prefs.getInstance(it)?.accessToken = ""
            val i = Intent(context, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            it.startActivity(i)
        }
    }

    /**
     * A method to convert the password in SHA1
     */
    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    fun SHA1(text: String?): String {
        val md = MessageDigest.getInstance("SHA-1")
        if (text != null) {
            md.update(text.toByteArray(charset("iso-8859-1")), 0, text.length)
        }
        val sha1hash = md.digest()
        return convertToHex(sha1hash)
    }

    private fun convertToHex(data: ByteArray): String {
        val buf = StringBuilder()
        for (b in data) {
            var halfbyte = (b.toInt() ushr 4) and 0x0F
            var two_halfs = 0
            do {
                if (0 <= halfbyte && halfbyte <= 9)
                    buf.append(('0'.toInt() + halfbyte).toChar())
                else
                    buf.append(('a'.toInt() + (halfbyte - 10)).toChar())
                halfbyte = (b and 0x0F).toInt()
            } while (two_halfs++ < 1)
        }
        return buf.toString()
    }


    fun formatDate(year: Int, month: Int, day: Int, opFormat: String): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = 0
        cal.set(year, month, day)
        val date = cal.time
        val sdf = SimpleDateFormat(opFormat, Locale.getDefault())
        return sdf.format(date)
    }

    fun formatDate(date: Date, opFormat: String = AppConstants.DEFAULT_DATE_FORMAT): String {
        return SimpleDateFormat(opFormat, Locale.getDefault()).format(date)
    }

    @JvmStatic
    fun formatDate(date: String?, opFormat: String = AppConstants.DEFAULT_DATE_FORMAT): String {
        return if (date != null && date != "" && date != "0") {
            SimpleDateFormat(opFormat, Locale.getDefault()).format(Date(date.toLong() * 1000))
        } else {
            ""
        }
    }

    @JvmStatic
    fun formatDate(date: String?): String {
        return if (date != null && date != "" && date != "0") {
            SimpleDateFormat(AppConstants.DEFAULT_DATE_FORMAT, Locale.getDefault()).format(Date(date.toLong() * 1000))
        } else {
            ""
        }
    }

    @JvmStatic
    fun getValue(value: String?) = if (value == null) "" else value

    @JvmStatic
    fun getValue(value: Int?) = if (value == null) 1 else value


    fun getRequestStatus(context: Context, status: Int): Pair<String, Int> {
        when (status) {
            RequestStatus.AVAILABLE -> return Pair(context.getString(R.string.title_accepted), ContextCompat.getColor(context, R.color.greenButton))
            RequestStatus.REJECTED -> return Pair(context.getString(R.string.title_rejected), ContextCompat.getColor(context, R.color.redButton))
            RequestStatus.PENDING -> return Pair(context.getString(R.string.title_pending), ContextCompat.getColor(context, R.color.yellowStatus))
            RequestStatus.CANCELLED -> return Pair(context.getString(R.string.title_cancelled), ContextCompat.getColor(context, R.color.redButton))
        }
        return Pair(context.getString(R.string.title_pending), ContextCompat.getColor(context, R.color.yellowStatus))
    }

    fun formatDate(date: Long): String {
        return formatDate(Date(date), AppConstants.DEFAULT_DATE_FORMAT)
    }

    /**
     * A method to get the external directory of phone, to store the image file
     */
    fun getWorkingDirectory(): File {
        val directory = File(Environment.getExternalStorageDirectory(), BuildConfig.APPLICATION_ID)
        if (!directory.exists()) {
            directory.mkdir()
        }
        return directory
    }

    /**
     * This method is for making new jpg file.
     * @param activity Instance of activity
     * @param prefix file name prefix
     */
    fun createImageFile(activity: Activity, prefix: String): FileUri? {
        val fileUri = FileUri()

        var image: File? = null
        try {
            image = File.createTempFile(prefix + System.currentTimeMillis().toString(), ".jpg", getWorkingDirectory())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (image != null) {
            fileUri.file = image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri.imageUrl = (FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileprovider", image))
            } else {
                fileUri.imageUrl = (Uri.parse("file:" + image.absolutePath))
            }
        }
        return fileUri
    }

    /**
     * A method to show device keyboard for user input
     */
    fun showKeyboard(activity: Activity?, view: EditText) {
        try {
            val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            Log.e("Exception showKeyboard", e.toString())
        }
    }

    /**
     * A method to hide the device's keyboard
     */
    fun hideKeyboard(activity: Activity) {
        if (activity.currentFocus != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }

    fun vectorToBitmap(resources: Resources, @DrawableRes id: Int): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}