package org.techtown.unsfcm

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException

object AppData {
    val logList by lazy { mutableListOf<String>() }

    const val ACTION_REDIS_DATA = "org.techtown.redis.data"

    var isDebug = true
    fun debug(tag: String, msg: String) {
        if (isDebug) Log.d(tag, msg)
    }

    fun error(tag: String, msg: String) {
        if (isDebug) Log.e(tag, msg)
    }

    fun error(tag: String, msg: String, e: Exception?) {
        if (isDebug) Log.e(tag, msg, e)
    }

    private lateinit var toast: Toast
    fun showToast(context: Context, msg: String) {
        if (::toast.isInitialized) toast.cancel()
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toast.show()
    }

    fun getIP(): String {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val it = en.nextElement()
                val enumIpAddress = it.inetAddresses
                while (enumIpAddress.hasMoreElements()) {
                    val inetAddress = enumIpAddress.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address)
                        return inetAddress.getHostAddress() ?: "unknown IP"
                }
            }
        } catch (_: SocketException) {
        }
        return "unknown IP"
    }

}