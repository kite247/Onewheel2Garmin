package com.floatangels.onewheel2garmin

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.content_info.*


class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val wv = findViewById<View>(R.id.infoWebView) as WebView
        wv.loadUrl("file:///android_asset/info.html")

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id: Int? = item?.itemId

        if (id == android.R.id.home) {
            finish()
        }
        return true
    }

}
