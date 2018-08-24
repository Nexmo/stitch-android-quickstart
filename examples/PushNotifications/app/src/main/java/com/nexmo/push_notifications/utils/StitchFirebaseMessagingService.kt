package com.nexmo.push_notifications.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService


class StitchFirebaseMessagingService : FirebaseMessagingService() {
    val TAG = "Messaging Service: "
    val conversationClient = Stitch.getInstance(this.applicationContext).client

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        Log.d(TAG, "Refreshed token: $p0")
        conversationClient.pushDeviceToken = p0
    }
}