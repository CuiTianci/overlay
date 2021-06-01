package com.ctc.easyoverlay

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.lang.reflect.Method
import kotlin.coroutines.CoroutineContext

/**
 * 常规手机：
 * Android9.0及以下，可以从后台启动Activity。
 * Android10.0及以上，需要SYSTEM_ALERT_WINDOW权限。
 * <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
 * 官方文档总介绍了其他特殊情况：
 * 小米手机：
 * 在系统中存在一个特殊权限：Start in background\Display pop-up window。
 * 小米手机（无论Android及MIUI版本）能否后台启动Activity取决于上述权限。
 * 官方文档说明：https://dev.mi.com/console/doc/detail?pId=1735
 */
class BackgroundLaunchPermissionUtil {

    companion object {

        const val TAG = "BackgroundLaunchPermissionUtil"

        /**
         * 判断是否可以后台启动Activity。
         */
        fun isPermissionGranted(context: Context): Boolean =
            isCommonDevicePermissionGranted(context) || isMiPermissionGranted(context) >= 2

        /**
         * 打开系统授权页。
         * @param fragment 当前上下文。
         * @param activity 当前上下文。
         * @param permissionGrantResultListener 授权结果回调。
         * @param autoBack 是否在用户开启权限后跳回应用。
         * @return 尝试开启页面，结果。
         */
        fun startBackgroundLaunchPermissionGrantActivity(
            activity: FragmentActivity? = null,
            fragment: Fragment? = null,
            autoBack: Boolean = false,
            permissionGrantResultListener: PermissionResultListener,
        ): OpenSettingState {
            if (BuildConfig.DEBUG && !(activity != null || fragment != null)) {
                error("Activity和Fragment不得同时为空")
            }
            return if (isMi()) {
                openMiPermissionSettings(
                    activity,
                    fragment,
                    autoBack,
                    permissionGrantResultListener
                )
            } else {
                openCommonSettings(activity, fragment, autoBack, permissionGrantResultListener)
            }
        }

        /**
         * 启动Activity，并获取"结果"，不同与常规，这个结果可以在回调中直接获取。
         * @param fragment 当前上下文。
         * @param activity 当前上下文。
         * @param intent 用于启动Activity的intent。
         * @param activityResultListener ActivityResult.
         */
        fun startActivityForResult(
            activity: FragmentActivity? = null,
            fragment: Fragment? = null,
            intent: Intent,
            activityResultListener: ActivityResultListener
        ) {
            getInvisibleFragment(activity, fragment).startIntentForResult(
                intent,
                activityResultListener
            )
        }

        /**
         * 常规设备判断条件：API29以下不需权限。API29及以上需要SYSTEM_ALERT_WINDOW权限。
         */
        private fun isCommonDevicePermissionGranted(context: Context) =
            !isMi() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || Settings.canDrawOverlays(
                context
            ))

        /**
         * 小米手机权限单独判断。
         * Start in background\Display pop-up window
         * @return -1：非小米设备；0：不具备startInBackground\Display pop-up window权限；1：具备startIn...,但不满足常规Android10.0设备SystemAlertWindow权限。2：具备后台启动界面能力。
         */
        private fun isMiPermissionGranted(context: Context): Int {
            if (!isMi()) return -1
            val miResult = try {
                val ops: AppOpsManager =
                    context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val op = 10021
                val method: Method = ops.javaClass.getMethod(
                    "checkOpNoThrow",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                method.invoke(
                    ops,
                    op,
                    Process.myUid(),
                    context.packageName
                ) == AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            val commonResultType =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || Settings.canDrawOverlays(
                        context
                    )
                ) 2 else 1
            return if (miResult) commonResultType else 0
        }

        /**
         * 启动“应用上层显示”--SYSTEM_ALERT_WINDOW授权页。
         * @param activity 上下文。
         * @param fragment 上下文。
         * @param autoBack 是否在用户开启权限后跳回应用。
         * @return 是否正常启动Activity。
         */
        private fun openCommonSettings(
            activity: FragmentActivity? = null,
            fragment: Fragment? = null,
            autoBack: Boolean = false,
            permissionGrantResultListener: PermissionResultListener,
            forBackgroundLaunch: Boolean = true
        ): OpenSettingState {
            if (BuildConfig.DEBUG && !(activity != null || fragment != null)) {
                error("Activity和Fragment不得同时为空")
            }
            return try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${activity?.packageName ?: fragment!!.context!!.packageName}")
                )
                getInvisibleFragment(activity, fragment).openBackgroundLaunchPermissionActivity(
                    intent,
                    autoBack,
                    permissionGrantResultListener,
                    forBackgroundLaunch
                )
                OpenSettingState.STATE_COMMON_OVERLAY
            } catch (e: java.lang.Exception) {
                OpenSettingState.STATE_FAILED
            }
        }

        /**
         * 启动小米手机授权。
         * @param activity 上下文。
         * @param fragment 上下文。
         * @param autoBack 是否在用户开启权限后跳回应用。
         * @return 是否正常启动Activity。
         */
        private fun openMiPermissionSettings(
            activity: FragmentActivity? = null,
            fragment: Fragment? = null,
            autoBack: Boolean = false,
            permissionGrantResultListener: PermissionResultListener,
        ): OpenSettingState {
            if (BuildConfig.DEBUG && !(activity != null || fragment != null)) {
                error("Activity和Fragment不得同时为空")
            }
            val context = activity ?: fragment!!.context!!
            return when (isMiPermissionGranted(context)) {
                0 -> {
                    try {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${activity?.packageName ?: fragment!!.context!!.packageName}")
                        )
                        getInvisibleFragment(
                            activity,
                            fragment
                        ).openBackgroundLaunchPermissionActivity(
                            intent,
                            autoBack,
                            permissionGrantResultListener
                        )
                        OpenSettingState.STATE_MI_START_IN_BACKGROUND
                    } catch (e: java.lang.Exception) {
                        OpenSettingState.STATE_FAILED
                    }
                }
                1 -> {
                    openCommonSettings(activity, fragment, autoBack, permissionGrantResultListener)
                }
                else -> {
                    OpenSettingState.STATE_FAILED
                }
            }
        }

        /**
         * 判断是否具备Overlay权限。
         */
        fun hasOverlayPermission(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

        /**
         * 启动申请Overlay权限的弹窗。
         * @param fragment 当前上下文。
         * @param activity 当前上下文。
         * @param permissionGrantResultListener 授权结果回调。
         * @param autoBack 是否在用户开启权限后跳回应用。
         * @return 尝试开启页面，结果。
         */
        fun startOverlayPermissionGrantActivity(
            activity: FragmentActivity? = null,
            fragment: Fragment? = null,
            autoBack: Boolean = false,
            permissionGrantResultListener: PermissionResultListener
        ): OpenSettingState {
            return openCommonSettings(
                activity,
                fragment,
                autoBack,
                permissionGrantResultListener,
                false
            )
        }

        /**
         * 判断是否为小米手机。
         */
        private fun isMi(): Boolean = Build.BRAND.equals("xiaomi", true)

        /**
         * 获取隐藏的Fragment。
         * @param activity 当前上下文。
         * @param fragment 当前上下文。
         */
        private fun getInvisibleFragment(
            activity: FragmentActivity? = null,
            fragment: Fragment? = null,
        ): InvisibleFragment {
            if (BuildConfig.DEBUG && !(activity != null || fragment != null)) {
                error("Activity和Fragment不得同时为空")
            }
            val fragmentManager =
                activity?.supportFragmentManager ?: fragment!!.childFragmentManager
            val existedFragment =
                fragmentManager.findFragmentByTag(InvisibleFragment.INVISIBLE_FRAGMENT_TAG)
            return if (existedFragment != null) {
                existedFragment as InvisibleFragment
            } else {
                val invisibleFragment = InvisibleFragment()
                fragmentManager.beginTransaction().add(
                    invisibleFragment,
                    InvisibleFragment.INVISIBLE_FRAGMENT_TAG
                )
                    .commitNowAllowingStateLoss()
                invisibleFragment
            }
        }
    }
}

/**
 * 调用启动页方法会得到的几种结果。
 */
enum class OpenSettingState {
    STATE_FAILED,//启动页面失败。
    STATE_COMMON_OVERLAY,//启动常规SystemAlertWindow权限页面。
    STATE_MI_START_IN_BACKGROUND//启动小米的StartInBackground权限页面。
}

/**
 * 同于启动权限请求Activity的隐藏的Fragment。
 */
class InvisibleFragment : Fragment() {

    private lateinit var permissionGrantResultListener: PermissionResultListener
    private lateinit var permissionCheckJob: Job

    private lateinit var activityResultListener: ActivityResultListener

    /**
     * 启动Activity。
     * @param intent 相应意图。
     * @param autoBack 是否在用户开启权限后跳回应用。
     * @param permissionGrantResultListener 授权结果回调。
     */
    fun openBackgroundLaunchPermissionActivity(
        intent: Intent,
        autoBack: Boolean = false,
        permissionGrantResultListener: PermissionResultListener,
        forBackgroundLaunch: Boolean = true
    ) {
        this.permissionGrantResultListener = permissionGrantResultListener
        startActivityForResult(intent, REQUEST_CODE_FOR_BACKGROUND_LAUNCH_PERMISSION)
        if (autoBack) {
            //后台错误日志，协程内部调用invokeSuspend方法时产生空指针异常。无解，故捕获该异常，对整体功能影响不大。
            val exceptionHandler =
                CoroutineExceptionHandler { _: CoroutineContext, _: Throwable ->
                    Log.wtf(BackgroundLaunchPermissionUtil.TAG, "unknownExceptionInner")
                }
            try {
                //轮询判断是否授予权限，授予时自动返回当前Activity。
                permissionCheckJob = lifecycleScope.launch(Dispatchers.Default + exceptionHandler) {
                    while (isActive) {
                        delay(500)
                        val permissionGranted =
                            if (forBackgroundLaunch) BackgroundLaunchPermissionUtil.isPermissionGranted(
                                context!!
                            ) else BackgroundLaunchPermissionUtil.hasOverlayPermission(context!!)
                        if (permissionGranted) {
                            val backIntent = Intent(context, requireActivity().javaClass)
                            backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            backIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(backIntent)
                            cancel()
                        }
                    }
                }
            } catch (_: Exception) {
                Log.wtf(BackgroundLaunchPermissionUtil.TAG, "unknownExceptionOuter")
            }
        }
    }

    /**
     * 通过intent启动Activity。
     * @param intent 用于启动Activity的intent。
     * @param activityResultListener ActivityResult.
     */
    fun startIntentForResult(
        intent: Intent,
        activityResultListener: ActivityResultListener
    ) {
        this.activityResultListener = activityResultListener
        startActivityForResult(intent, REQUEST_CODE_FOR_START_INTENT)
    }

    /**
     * 处理授权结果。
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (this::permissionCheckJob.isInitialized && permissionCheckJob.isActive) {
            permissionCheckJob.cancel()
        }
        when (requestCode) {
            REQUEST_CODE_FOR_BACKGROUND_LAUNCH_PERMISSION -> {
                if (this::permissionGrantResultListener.isInitialized) {
                    permissionGrantResultListener.onPermissionResult(
                        BackgroundLaunchPermissionUtil.isPermissionGranted(context!!)
                    )
                }
            }
            REQUEST_CODE_FOR_START_INTENT -> {
                if (this::activityResultListener.isInitialized) {
                    activityResultListener.onResult(resultCode, data)
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_FOR_BACKGROUND_LAUNCH_PERMISSION = 1
        const val REQUEST_CODE_FOR_START_INTENT = 100
        const val INVISIBLE_FRAGMENT_TAG = "com.background.launch.invisible.fragment"
    }
}

interface PermissionResultListener {
    fun onPermissionResult(result: Boolean)
}

interface ActivityResultListener {
    fun onResult(resultCode: Int, data: Intent?)
}