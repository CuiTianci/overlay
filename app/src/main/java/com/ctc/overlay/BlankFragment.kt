package com.ctc.overlay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ctc.easyoverlay.BackgroundLaunchPermissionUtil
import com.ctc.easyoverlay.OverlayDirectionActivity


class BlankFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_blank, container, false)
        rootView.findViewById<TextView>(R.id.tv_fragment).setOnClickListener {
            BackgroundLaunchPermissionUtil.startBackgroundLaunchPermissionGrantActivity(
                fragment = this,
                autoBack = true
            ) {
                Log.e("cui", "result$it")
            }
            startActivity(Intent(context, OverlayDirectionActivity::class.java))
        }
        return rootView
    }
}