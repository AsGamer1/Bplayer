package com.bseenmedia.bplayer

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.View
import android.view.KeyEvent
import android.app.admin.DevicePolicyManager
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.Manifest
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate
import expo.modules.ReactActivityDelegateWrapper
import expo.modules.splashscreen.SplashScreenManager

class MainActivity : ReactActivity() {
  private var isVolumeUpPressed = false
  private var isVolumeDownPressed = false
  private val holdDuration = 10000L
  private val handler = Handler(Looper.getMainLooper())
  private val shutdownRunnable = Runnable {
    val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("exitApp", true).apply()
    stopService(Intent(this, AppMonitorService::class.java))
    stopLockTask()
    finishAffinity()
    android.os.Process.killProcess(android.os.Process.myPid())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    // @generated begin expo-splashscreen - expo prebuild (DO NOT MODIFY) sync-f3ff59a738c56c9a6119210cb55f0b613eb8b6af
    SplashScreenManager.registerOnActivity(this)
    // @generated end expo-splashscreen
    super.onCreate(null)

    val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("exitApp", false).apply()

    val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val compName = ComponentName(this, KioskDeviceAdminReceiver::class.java)

    if (dpm.isDeviceOwnerApp(packageName)) {
      dpm.setLockTaskPackages(compName, arrayOf(packageName))
      startLockTask()
    } else {
      Toast.makeText(this, "La app no es device owner. Ejecuta el comando ADB.", Toast.LENGTH_LONG).show()
    }

    dpm.setPermissionGrantState(
      compName,
      packageName,
      Manifest.permission.CAMERA,
      DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
    )

    dpm.setPermissionGrantState(
      compName,
      packageName,
      Manifest.permission.ACCESS_FINE_LOCATION,
      DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
    )
    
    // Ocultar barra de estado y de navegación
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.insetsController?.let { controller ->
        controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      }
    } else {
      @Suppress("DEPRECATION")
      window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        or View.SYSTEM_UI_FLAG_FULLSCREEN
        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      )
    }

    // Mantener pantalla encendida
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    // Iniciar el servicio de supervisión
    val intent = Intent(this, AppMonitorService::class.java)
    startService(intent)
  }

  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    when (event.keyCode) {
      KeyEvent.KEYCODE_VOLUME_UP -> {
        isVolumeUpPressed = event.action == KeyEvent.ACTION_DOWN
      }
      KeyEvent.KEYCODE_VOLUME_DOWN -> {
        isVolumeDownPressed = event.action == KeyEvent.ACTION_DOWN
      }
    }

    if (isVolumeUpPressed && isVolumeDownPressed) {
      handler.postDelayed(shutdownRunnable, holdDuration)
    } else {
      handler.removeCallbacks(shutdownRunnable)
    }

    return super.dispatchKeyEvent(event)
  }

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  override fun getMainComponentName(): String = "main"

  /**
   * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
   * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
   */
  override fun createReactActivityDelegate(): ReactActivityDelegate {
    return ReactActivityDelegateWrapper(
      this,
      BuildConfig.IS_NEW_ARCHITECTURE_ENABLED,
      object : DefaultReactActivityDelegate(
        this,
        mainComponentName,
        fabricEnabled
      ){}
    )
  }

  /**
    * Align the back button behavior with Android S
    * where moving root activities to background instead of finishing activities.
    * @see <a href="https://developer.android.com/reference/android/app/Activity#onBackPressed()">onBackPressed</a>
    */
  override fun invokeDefaultOnBackPressed() {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
      if (!moveTaskToBack(false)) {
        // For non-root activities, use the default implementation to finish them.
        super.invokeDefaultOnBackPressed()
      }
      return
    }

    // Use the default back button implementation on Android S
    // because it's doing more than [Activity.moveTaskToBack] in fact.
    super.invokeDefaultOnBackPressed()
  }
}