package com.ctc.overlay

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ctc.easyoverlay.*
import com.ctc.overlay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        fun initView() {
            val onClickListener = View.OnClickListener {
                val pageType =
                    BackgroundLaunchPermissionUtil.startBackgroundLaunchPermissionGrantActivity(
                        this,
                        autoBack = true,
                        permissionGrantResultListener = object :
                            PermissionResultListener {
                            override fun onPermissionResult(granted: Boolean) {
                                permissionResultToast(granted)
                            }
                        }
                    )
                when (it.id) {
                    binding.btnRequestPermission.id -> {
                        if (pageType == OpenSettingState.STATE_COMMON_OVERLAY) {
                            startActivity(Intent(this, OverlayDirectionActivity::class.java))
                        } else if (pageType == OpenSettingState.STATE_MI_START_IN_BACKGROUND) {
                            startActivity(Intent(this, StartInBackgroundActivity::class.java))
                        }
                    }
                    binding.btnRequestOnMi.id -> {
                        if (pageType != OpenSettingState.STATE_FAILED) {
                            startActivity(Intent(this, StartInBackgroundActivity::class.java))
                        }
                    }
                    binding.btnRequestOnOthers.id -> {
                        if (pageType != OpenSettingState.STATE_FAILED) {
                            startActivity(Intent(this, OverlayDirectionActivity::class.java))
                        }
                    }
                }
            }
            binding.btnRequestPermission.setOnClickListener(onClickListener)
            binding.btnRequestOnMi.setOnClickListener(onClickListener)
            binding.btnRequestOnOthers.setOnClickListener(onClickListener)
            binding.btnStartActivityForResult.setOnClickListener {
                BackgroundLaunchPermissionUtil.startActivityForResult(this,
                    intent = Intent(this, TargetActivity::class.java),
                    activityResultListener = object : ActivityResultListener {
                        override fun onResult(resultCode: Int, data: Intent?) {
                            if (resultCode == RESULT_OK) {
                                data?.let {
                                    it.extras?.let { bundle ->
                                        val number =
                                            bundle.getInt(TargetActivity.EXTRA_RANDOM_NUMBER)
                                        shortToast("MainActivity:The random number is:$number")
                                    }
                                }
                            }
                        }
                    })
            }
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun permissionResultToast(granted: Boolean) = shortToast("permissionGranted:$granted")

    private fun shortToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}