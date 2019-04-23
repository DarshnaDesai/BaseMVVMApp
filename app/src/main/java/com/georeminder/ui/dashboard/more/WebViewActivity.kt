package com.georeminder.ui.dashboard.more

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.georeminder.R
import com.georeminder.databinding.ActivityWebviewBinding
import com.georeminder.utils.AppConstants

/**
 * Created by Darshna Desai on 8/2/19.
 */
class WebViewActivity : Activity() {

    private lateinit var binding: ActivityWebviewBinding

    companion object {
        fun newIntent(context: Context, url: String): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_URL, url)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_webview)


        if (intent.hasExtra(AppConstants.EXTRA_URL)) {
            binding.webView.loadUrl(intent.getStringExtra(AppConstants.EXTRA_URL))
        }

    }

}