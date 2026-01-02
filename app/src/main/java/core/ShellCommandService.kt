package com.kaixin.micswitch.core

import android.system.Os
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * This service runs in the Shizuku process and allows us to execute shell commands.
 */
class ShellCommandService {

    companion object {
        fun runCommand(command: String): Int {
            try {
                val process = Runtime.getRuntime().exec(command.split(" ").toTypedArray())
                process.waitFor()
                return process.exitValue()
            } catch (e: Exception) {
                e.printStackTrace()
                return -1
            }
        }
    }
}
