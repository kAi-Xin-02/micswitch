package com.kaixin.micswitch.core

import android.content.Context
import android.media.AudioManager
import com.topjohnwu.superuser.Shell

object MicDetector {

    fun getAppsUsingMic(): List<String> {
        val apps = mutableListOf<String>()
        try {
            // Check which apps have audio focus
            val result = Shell.cmd("dumpsys audio | grep -A 5 'Audio Focus'").exec()
            if (result.isSuccess) {
                result.out.forEach { line ->
                    if (line.contains("package=")) {
                        val pkg = line.substringAfter("package=").substringBefore(" ")
                        if (pkg.isNotEmpty()) apps.add(pkg)
                    }
                }
            }

            // Also check media.audio_policy for active inputs
            val micResult = Shell.cmd("dumpsys media.audio_policy | grep -A 3 'Input'").exec()
            if (micResult.isSuccess) {
                micResult.out.forEach { line ->
                    if (line.contains("package:")) {
                        val pkg = line.substringAfter("package:").trim()
                        if (pkg.isNotEmpty() && !apps.contains(pkg)) apps.add(pkg)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return apps.take(2)
    }

    fun detectVoiceApps(context: Context): Pair<String?, String?> {
        val commonVoiceApps = mapOf(
            "com.discord" to "Discord",
            "com.whatsapp" to "WhatsApp",
            "com.google.android.apps.tachyon" to "Google Meet",
            "us.zoom.videomeetings" to "Zoom",
            "com.skype.raider" to "Skype",
            "org.telegram.messenger" to "Telegram",
            "com.facebook.orca" to "Messenger"
        )

        val detectedApps = getAppsUsingMic()
        val voiceApps = mutableListOf<String>()

        detectedApps.forEach { pkg ->
            commonVoiceApps.keys.forEach { knownPkg ->
                if (pkg.contains(knownPkg)) voiceApps.add(pkg)
            }
        }

        if (voiceApps.size < 2) {
            val pm = context.packageManager
            commonVoiceApps.keys.forEach { pkg ->
                try {
                    pm.getPackageInfo(pkg, 0)
                    if (!voiceApps.contains(pkg)) voiceApps.add(pkg)
                } catch (e: Exception) {
                }
            }
        }

        return if (voiceApps.size >= 2) {
            Pair(voiceApps[0], voiceApps[1])
        } else {
            Pair(null, null)
        }
    }
}