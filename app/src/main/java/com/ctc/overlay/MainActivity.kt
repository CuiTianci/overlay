package com.ctc.overlay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ctc.easyoverlay.BackgroundLaunchPermissionUtil
import com.ctc.easyoverlay.OpenSettingState
import com.ctc.easyoverlay.OverlayDirectionActivity
import com.ctc.easyoverlay.StartInBackgroundActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.tv_test).setOnClickListener {
          val a =  BackgroundLaunchPermissionUtil.startBackgroundLaunchPermissionGrantActivity(
                this,
                autoBack = true
            ) {
                Log.e("cui", "result$it")
            }
            startActivity(Intent(this, StartInBackgroundActivity::class.java))
        }

        findViewById<TextView>(R.id.tv_test).setOnLongClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
            true
        }
    }
}