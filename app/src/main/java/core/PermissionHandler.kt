package com.kaixin.micswitch.core

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import com.topjohnwu.superuser.Shell

object PermissionHandler {

    private var permissionMethod = Method.NONE

    enum class Method {
        NONE, ROOT, SHIZUKU
    }

    fun detectMethod(): Method {
        if (checkRoot()) {
            permissionMethod = Method.ROOT
            return Method.ROOT
        }
        if (checkShizuku()) {
            permissionMethod = Method.SHIZUKU
            return Method.SHIZUKU
        }
        return Method.NONE
    }

    fun currentMethod() = permissionMethod

    private fun checkRoot(): Boolean {
        return try {
            Shell.getShell().isRoot
        } catch (e: Exception) {
            false
        }
    }

    private fun checkShizuku(): Boolean {
        return try {
            rikka.shizuku.Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun revokeMic(context: Context, packageName: String): Boolean {
        val cmd = "pm revoke $packageName android.permission.RECORD_AUDIO"
        return executeCommand(cmd)
    }

    fun grantMic(context: Context, packageName: String): Boolean {
        val cmd = "pm grant $packageName android.permission.RECORD_AUDIO"
        return executeCommand(cmd)
    }

    fun forceStopApp(packageName: String): Boolean {
        val cmd = "am force-stop $packageName"
        return executeCommand(cmd)
    }

    private fun executeCommand(cmd: String): Boolean {
        return when (permissionMethod) {
            Method.ROOT -> {
                try {
                    Shell.cmd(cmd).exec().isSuccess
                } catch (e: Exception) {
                    false
                }
            }
            Method.SHIZUKU -> {
                try {
                    // For Shizuku, we'll use exec through IActivityManager or fallback
                    executeShizukuCommand(cmd)
                } catch (e: Exception) {
                    false
                }
            }
            Method.NONE -> false
        }
    }

    private fun executeShizukuCommand(cmd: String): Boolean {
        return try {
            // Simple approach - use Runtime with Shizuku context
            val runtime = Runtime.getRuntime()
            val process = if (cmd.startsWith("pm")) {
                runtime.exec(arrayOf("sh", "-c", cmd))
            } else {
                runtime.exec(cmd)
            }
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}