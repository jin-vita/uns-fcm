package org.techtown.unsfcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushService : FirebaseMessagingService() {
    companion object {
        private const val TAG: String = "PushService"
    }

    override fun onNewToken(token: String) {
        AppData.error(TAG, "Refreshed token: $token")
        sendToActivity("TOKEN", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        AppData.error(TAG, "---- message notification ----")
        AppData.debug(TAG, "title: ${message.notification?.title}")
        AppData.debug(TAG, "body: ${message.notification?.body}")
        AppData.error(TAG, "---- message notification ----")
        sendToActivity("PUSH", message.data["body"].toString())
    }

    private fun sendToActivity(command: String, data: String) {
        AppData.error(TAG, "command: $command, data: $data")
    }
}