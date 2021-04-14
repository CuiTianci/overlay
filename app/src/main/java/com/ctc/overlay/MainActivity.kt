package com.ctc.overlay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ctc.easyoverlay.BackgroundLaunchPermissionUtil
import com.ctc.easyoverlay.OverlayDirectionActivity
import com.ctc.easyoverlay.PermissionResultListener
import com.ctc.easyoverlay.StartInBackgroundActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.tv_test).setOnClickListener {
            BackgroundLaunchPermissionUtil.startOverlayPermissionGrantActivity(
                activity = this,
                autoBack = true,
                permissionGrantResultListener = object : PermissionResultListener {
                    override fun onPermissionResult(result: Boolean) {
                        Log.e("cui", "result:$result")
                    }
                }
            )
            startActivity(Intent(this, OverlayDirectionActivity::class.java))
        }

        findViewById<TextView>(R.id.tv_test).setOnLongClickListener {
            BackgroundLaunchPermissionUtil.startBackgroundLaunchPermissionGrantActivity(
                activity = this,
                autoBack = true,
                permissionGrantResultListener = object : PermissionResultListener {
                    override fun onPermissionResult(result: Boolean) {
                        Log.e("cui", "result:$result")
                    }
                }
            )
            startActivity(Intent(this, OverlayDirectionActivity::class.java))
            false
        }
    }
}