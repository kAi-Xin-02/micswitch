package com.kaixin.micswitch.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.core.app.NotificationCompat
import com.kaixin.micswitch.R
import com.kaixin.micswitch.core.PermissionHandler
import com.kaixin.micswitch.core.MicController
import com.kaixin.micswitch.ui.MainActivity

class FloatingService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private val CHANNEL_ID = "MicSwitchChannel"
    private val NOTIFICATION_ID = 1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            if (!Settings.canDrawOverlays(this)) {
                PermissionHandler.showToast(this, "❌ Need overlay permission!")
                stopSelf()
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
                val notification = createNotification()
                startForeground(NOTIFICATION_ID, notification)
            }

            setupFloatingBubble()
            PermissionHandler.showToast(this, "✅ Floating button ready")
        } catch (e: Exception) {
            e.printStackTrace()
            PermissionHandler.showToast(this, "Error: ${e.message}")
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mic Switch Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Mic switch service running"
                setShowBadge(false)
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mic Switch Active")
            .setContentText("Service is running")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
        }
        return START_STICKY
    }

    private fun setupFloatingBubble() {
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            floatingView = LayoutInflater.from(this).inflate(R.layout.floating_bubble, null)

            if (floatingView == null) {
                PermissionHandler.showToast(this, "❌ Failed to create floating view")
                return
            }

            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = 50
                y = 200
            }

            windowManager?.addView(floatingView, params)
            setupTouch()
        } catch (e: Exception) {
            e.printStackTrace()
            PermissionHandler.showToast(this, "Bubble error: ${e.message}")
        }
    }

    private fun setupTouch() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var clickStartTime = 0L
        var isMoved = false

        floatingView?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    clickStartTime = System.currentTimeMillis()
                    isMoved = false

                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY

                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        isMoved = true
                        params?.x = initialX - deltaX.toInt()
                        params?.y = initialY + deltaY.toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()

                    val clickDuration = System.currentTimeMillis() - clickStartTime

                    if (!isMoved && clickDuration < 300) {
                        performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun performClick() {
        try {
            floatingView?.animate()?.alpha(0.5f)?.setDuration(100)?.withEndAction {
                floatingView?.animate()?.alpha(1f)?.setDuration(100)?.start()
            }?.start()

            MicController.switchMic(this)
        } catch (e: Exception) {
            PermissionHandler.showToast(this, "Switch error: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            floatingView?.let {
                windowManager?.removeView(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}