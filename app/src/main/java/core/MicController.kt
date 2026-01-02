package com.kaixin.micswitch.core

import android.content.Context

object MicController {

    private var appA: String? = null
    private var appB: String? = null
    private var currentActive: String? = null
    private var appAName: String? = null
    private var appBName: String? = null

    fun setApps(packageA: String, packageB: String, nameA: String, nameB: String) {
        appA = packageA
        appB = packageB
        appAName = nameA
        appBName = nameB
        currentActive = packageA
    }

    fun getAppA() = appA
    fun getAppB() = appB
    fun getCurrentActive() = currentActive

    fun switchMic(context: Context): String? {
        if (appA == null || appB == null) return null

        val target = if (currentActive == appA) appB else appA
        val targetName = if (target == appA) appAName else appBName
        val other = if (target == appA) appB else appA
        val otherName = if (other == appA) appAName else appBName

        val revokeSuccess = PermissionHandler.revokeMic(context, other!!)
        Thread.sleep(200)
        val grantSuccess = PermissionHandler.grantMic(context, target!!)

        if (revokeSuccess && grantSuccess) {
            currentActive = target
            PermissionHandler.showToast(context, "🎤 → $targetName")
            return target
        } else if (grantSuccess) {
            currentActive = target
            PermissionHandler.showToast(context, "🎤 → $targetName (partial)")
            return target
        } else {
            PermissionHandler.showToast(context, "❌ Switch failed")
            return null
        }
    }

    fun reset() {
        appA = null
        appB = null
        appAName = null
        appBName = null
        currentActive = null
    }
}