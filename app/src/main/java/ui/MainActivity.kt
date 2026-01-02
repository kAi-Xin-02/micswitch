package com.kaixin.micswitch.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kaixin.micswitch.R
import com.kaixin.micswitch.core.MicController
import com.kaixin.micswitch.core.MicDetector
import com.kaixin.micswitch.core.PermissionHandler
import com.kaixin.micswitch.databinding.ActivityMainBinding
import com.kaixin.micswitch.service.FloatingService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var installedApps = mutableListOf<AppInfo>()

    data class AppInfo(val name: String, val packageName: String) {
        override fun toString() = name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkMethod()
        loadApps()
        setupButtons()
    }

    private fun setupUI() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    private fun checkMethod() {
        val method = PermissionHandler.detectMethod()
        val statusText = when (method) {
            PermissionHandler.Method.ROOT -> "✅ Root Access Detected"
            PermissionHandler.Method.SHIZUKU -> "✅ Shizuku Access Detected"
            PermissionHandler.Method.NONE -> "❌ Root Access Required"
        }
        binding.statusText.text = statusText

        if (method == PermissionHandler.Method.NONE) {
            binding.statusCard.setCardBackgroundColor(getColor(android.R.color.holo_red_light))
        } else {
            binding.statusCard.setCardBackgroundColor(getColor(android.R.color.holo_green_light))
        }
    }

    private fun loadApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)

        installedApps.clear()
        for (app in apps) {
            val name = app.loadLabel(pm).toString()
            val pkg = app.activityInfo.packageName
            installedApps.add(AppInfo(name, pkg))
        }
        installedApps.sortBy { it.name }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, installedApps)
        binding.spinnerAppA.adapter = adapter
        binding.spinnerAppB.adapter = adapter

        installedApps.forEachIndexed { index, app ->
            if (app.packageName.contains("discord")) binding.spinnerAppA.setSelection(index)
            if (app.packageName.contains("whatsapp")) binding.spinnerAppB.setSelection(index)
        }
    }

    private fun setupButtons() {
        binding.btnStart.setOnClickListener {
            if (PermissionHandler.currentMethod() == PermissionHandler.Method.NONE) {
                Toast.makeText(this, "❌ Root Access Required", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
                return@setOnClickListener
            }

            val autoDetected = MicDetector.detectVoiceApps(this)
            if (autoDetected.first != null && autoDetected.second != null) {
                val appAName = installedApps.find { it.packageName == autoDetected.first }?.name ?: "App 1"
                val appBName = installedApps.find { it.packageName == autoDetected.second }?.name ?: "App 2"

                MicController.setApps(autoDetected.first!!, autoDetected.second!!, appAName, appBName)
                Toast.makeText(this, "✅ Auto-detected: $appAName & $appBName", Toast.LENGTH_LONG).show()
            } else {
                val appA = installedApps[binding.spinnerAppA.selectedItemPosition]
                val appB = installedApps[binding.spinnerAppB.selectedItemPosition]

                if (appA.packageName == appB.packageName) {
                    Toast.makeText(this, "Select different apps", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                MicController.setApps(appA.packageName, appB.packageName, appA.name, appB.name)
                Toast.makeText(this, "✅ Manual selection: ${appA.name} & ${appB.name}", Toast.LENGTH_SHORT).show()
            }

            startService(Intent(this, FloatingService::class.java))

            binding.btnStart.isEnabled = false
            binding.btnStop.isEnabled = true
        }

        binding.btnStop.setOnClickListener {
            stopService(Intent(this, FloatingService::class.java))
            MicController.reset()
            Toast.makeText(this, "⏹️ Service Stopped", Toast.LENGTH_SHORT).show()

            binding.btnStart.isEnabled = true
            binding.btnStop.isEnabled = false
        }

        binding.btnHelp.setOnClickListener {
            showHelpDialog()
        }

        binding.btnGithub.setOnClickListener {
            openGitHub()
        }
    }

    private fun showHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("📖 How to Use")
            .setMessage("""
            1. Root access required
            2. Grant overlay permission
            3. Join VC in both apps
            4. Tap bubble to switch mic
            
            ⚠️ IMPORTANT:
            • Mic permission switches between apps
            • You may need to tap unmute in the app
            • Both VCs stay connected
            • If not working, toggle mic button in app
            
            📧 Contact: star.light0x.2@gmail.com
        """.trimIndent())
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun openGitHub() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kAi-Xin-02"))
        startActivity(intent)
    }

    private fun requestOverlayPermission() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs overlay permission to show floating bubble")
            .setPositiveButton("Grant") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}