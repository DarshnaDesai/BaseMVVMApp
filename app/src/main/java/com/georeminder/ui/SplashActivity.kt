package com.georeminder.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.graphics.drawable.Animatable2Compat
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.georeminder.R
import com.georeminder.data.local.prefs.Prefs
import com.georeminder.databinding.ActivitySplashBinding
import com.georeminder.ui.dashboard.DashboardActivity
import com.georeminder.ui.introduction.IntroductionActivity
import com.georeminder.ui.login.LoginActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.util.HalfSerializer.onComplete
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME: Long = 5000

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.activity = this
        startAnimation()

        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(object : OnCompleteListener<InstanceIdResult> {
                    override fun onComplete(task: Task<InstanceIdResult>) {

                        if (!task.isSuccessful) {
                            Log.e("getInstanceId failed", "FAILED")
                            return
                        }

                        val token = task.result?.token
                        Log.d("TOKEN = ", token)
                    }

                })
    }

    @SuppressLint("CheckResult")
    private fun startAnimation() {
        val vectorDrawable = binding.ivMap.drawable
        /**
         * Above 21 callback is available for animation END
         */
        if (vectorDrawable is AnimatedVectorDrawableCompat) {
            vectorDrawable.start()
            vectorDrawable.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    super.onAnimationEnd(drawable)
                    startApp()
                }
            })
        }
        /**
         * Below 21 callback is not available for animation END
         */
        if (vectorDrawable is AnimatedVectorDrawable) {
            vectorDrawable.start()
            Observable.timer(SPLASH_TIME, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        startApp()
                    }
        }
        animateTextAndLogo()
    }

    private fun startApp() {
        if (Prefs.getInstance(this@SplashActivity)?.userDataModel != null) {
            startActivity(DashboardActivity.newIntent(this@SplashActivity))
        } else {
            //startActivity(LoginActivity.newIntent(this@SplashActivity))
            IntroductionActivity.start(this@SplashActivity)
        }
    }

    private fun animateTextAndLogo() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener { updatedAnimation ->
                val animatedValue = updatedAnimation.animatedValue as Float
                binding.ivLogo.alpha = animatedValue
                binding.tvAppName.alpha = animatedValue
            }
            duration = SPLASH_TIME
            start()
        }

    }
}
