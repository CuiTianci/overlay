package com.ctc.overlay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ctc.easyoverlay.*


class BlankFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_blank, container, false)
        rootView.findViewById<TextView>(R.id.tv_fragment).setOnClickListener {
            when (BackgroundLaunchPermissionUtil.startBackgroundLaunchPermissionGrantActivity(
                fragment = this,
                autoBack = true,
                permissionGrantResultListener = object : PermissionResultListener {
                    override fun onPermissionResult(result: Boolean) {
                        Log.e("cui", "result:$result")
                    }

                }
            )) {
                OpenSettingState.STATE_COMMON_OVERLAY -> startActivity(
                    Intent(
                        context,
                        OverlayDirectionActivity::class.java
                    )
                )
                OpenSettingState.STATE_MI_START_IN_BACKGROUND -> startActivity(
                    Intent(
                        context,
                        StartInBackgroundActivity::class.java
                    )
                )
            }
        }
        return rootView
    }
}