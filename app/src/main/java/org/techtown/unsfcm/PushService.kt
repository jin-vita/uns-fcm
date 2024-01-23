package org.techtown.unsfcm

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushService : FirebaseMessagingService() {
    companion object {
        private const val TAG: String = "PushService"
    }

    override fun onNewToken(token: String) {
        AppData.error(TAG, "Refreshed token: $token")
        broadcastToActivity("TOKEN", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        AppData.error(TAG, "---- message notification ----")
        AppData.debug(TAG, "title: ${message.notification?.title}")
        AppData.debug(TAG, "body: ${message.notification?.body}")
        AppData.error(TAG, "---- message data ----")
        AppData.debug(TAG, "data: ${message.data}")
        broadcastToActivity(message.data["sender"].toString(), message.data["body"].toString())
    }

    private fun broadcastToActivity(channel: String, data: String) = with(Intent(AppData.ACTION_REMOTE_DATA)) {
        AppData.error(TAG, "broadcastToActivity called. data : $data")
        putExtra(Extras.COMMAND, "PUSH")
        putExtra("channel", channel)
        putExtra("data", data)
        sendBroadcast(this)
    }
}