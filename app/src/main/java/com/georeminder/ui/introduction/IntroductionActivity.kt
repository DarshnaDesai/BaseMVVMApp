package com.georeminder.ui.introduction

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import com.georeminder.R
import com.georeminder.databinding.ActivityIntroductionBinding
import com.georeminder.ui.login.LoginActivity


class IntroductionActivity : AppCompatActivity() {
    lateinit var binding: ActivityIntroductionBinding
    var isLastScreen: Boolean = false

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, IntroductionActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_introduction)
        init()
    }

    private fun init() {
        binding.ivIntro.visibility = View.INVISIBLE
        var isScaleAnimationEnable = false
        val roundedImageAnimator = ValueAnimator.ofFloat(0f, 1f)

        roundedImageAnimator.addUpdateListener {
            if (it.animatedValue as Float > 0.75 && !isScaleAnimationEnable) {
                introImageAnimation()
                isScaleAnimationEnable = true
            }
            binding.ivIntroBg.scaleX = it.animatedValue as Float
            binding.ivIntroBg.scaleY = it.animatedValue as Float
        }
        roundedImageAnimator.duration = 1000
        roundedImageAnimator.start()
    }

    private fun introImageAnimation() {
        val introUpAnimator = ValueAnimator.ofFloat(binding.ivIntro.y, binding.ivIntro.y - 50)
        introUpAnimator.addUpdateListener {
            binding.ivIntro.visibility = View.VISIBLE
            binding.ivIntro.y = it.animatedValue as Float
        }
        introUpAnimator.duration = 500
        introUpAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                val introDownAnimator = ValueAnimator.ofFloat(binding.ivIntro.y, binding.ivIntro
                        .y + 60)
                introDownAnimator.addUpdateListener {
                    binding.ivIntro.y = it.animatedValue as Float
                }
                introDownAnimator.duration = 500
                introDownAnimator.start()
            }
        })
        introUpAnimator.start()

        val introScaleAnimator = ValueAnimator.ofFloat(0.5f, 1f)
        introScaleAnimator.addUpdateListener {
            binding.ivIntro.scaleX = it.animatedValue as Float
            binding.ivIntro.scaleY = it.animatedValue as Float
        }
        introScaleAnimator.duration = 1000
        introScaleAnimator.start()
    }

    private fun closeAnimation() {
        val introEndY = binding.ivIntro.x
        val closeAnimator = ValueAnimator.ofFloat(introEndY, (0f - binding.ivIntro.width))
        closeAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Float
            binding.ivIntro.x = animatedValue
        }
        closeAnimator.duration = 1000
        closeAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                boysAnimation()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
        closeAnimator.start()
    }

    fun boysAnimation() {
        val introUpAnimator = ValueAnimator.ofFloat(binding.ivSecondIntro.y, binding.ivSecondIntro.y - 50)
        introUpAnimator.addUpdateListener {
            binding.ivSecondIntro.visibility = View.VISIBLE
            binding.ivSecondIntro.y = it.animatedValue as Float
        }
        introUpAnimator.duration = 500
        introUpAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                val introDownAnimator = ValueAnimator.ofFloat(binding.ivSecondIntro.y, binding.ivSecondIntro
                        .y + 60)
                introDownAnimator.addUpdateListener {
                    binding.ivSecondIntro.y = it.animatedValue as Float
                }
                introDownAnimator.duration = 500
                introDownAnimator.start()
            }
        })
        introUpAnimator.start()

        val introScaleAnimator = ValueAnimator.ofFloat(0.5f, 1f)
        introScaleAnimator.addUpdateListener {
            binding.ivSecondIntro.scaleX = it.animatedValue as Float
            binding.ivSecondIntro.scaleY = it.animatedValue as Float
        }
        introScaleAnimator.duration = 1000
        introScaleAnimator.start()
    }

    fun TextView.animatedTextChange(text: String, duration: Long) {
        val fadeIn = AlphaAnimation(1f, 0f)
        fadeIn.duration = duration
        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                this@animatedTextChange.text = text
                val fadeOut = AlphaAnimation(0f, 1f)
                fadeOut.duration = 500
                this@animatedTextChange.startAnimation(fadeOut)
            }

            override fun onAnimationRepeat(animation: Animation) {

            }


        })
        this.startAnimation(fadeIn)
    }

    /**
     * On Click of Next button if user is on first screen then
     * animate for next intro screen otherwise redirect to him
     * login screen.
     */
    fun onNext(view: View) {
        if (!isLastScreen) {
            closeAnimation()
            binding.tvTitle.animatedTextChange(getString(R.string.label_completely_anonymous), 1000)
            binding.tvDescription.animatedTextChange(getString(R.string.label_all_confess_sent_are_anonymous),
                    1000)
            binding.tvNext.animatedTextChange(getString(R.string.abc_action_mode_done),
                    1000)
            isLastScreen = true
        } else {
            LoginActivity.start(this)
//            DashboardActivity.start(this)
        }
    }
}
