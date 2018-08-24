package com.nexmo.push_notifications.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.nexmo.push_notifications.R
import com.nexmo.sdk.conversation.client.Image
import com.nexmo.sdk.conversation.client.Member
import com.nexmo.sdk.conversation.client.Text
import com.nexmo.sdk.conversation.push.PushNotification
import com.nexmo.sdk.conversation.push.PushNotification.*
import com.nexmo.sdk.conversation.client.event.container.Invitation




const val NOTIFICATION_CHANNEL_ID = "misc"
const val NOTIFICATION_CHANNEL_NAME = "Miscellaneous"
const val NOTIFICATION_ID = 1

class StitchPushReceiver : BroadcastReceiver() {
    private val TAG = StitchPushReceiver::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")
        val bundle = intent?.extras

        if (context != null && bundle != null
                && intent.action == PushNotification.CONVERSATION_PUSH_ACTION
                && bundle.containsKey(PushNotification.CONVERSATION_PUSH_TYPE)) {

            handlePush(bundle, context)
        }
    }

    private fun handlePush(bundle: Bundle?, context: Context) {
        val type = bundle?.getString(PushNotification.CONVERSATION_PUSH_TYPE)
        when(type) {
            PushNotification.ACTION_TYPE_IMAGE -> handleImage(bundle, context)
            PushNotification.ACTION_TYPE_TEXT -> handleText(bundle, context)
            PushNotification.ACTION_TYPE_INVITE -> handleInvite(bundle, context)
            else -> { throw IllegalArgumentException("unhandled push notification type") }
        }
    }

    private fun handleInvite(bundle: Bundle, context: Context) {
        val invitation = PushInvitation(
                senderMemberId = bundle.getString(PushNotification.ACTION_TYPE_INVITED_BY_MEMBER_ID),
                conversationName =  bundle.getString(PushNotification.ACTION_TYPE_INVITE_CONVERSATION_NAME),
                senderUsername = bundle.getString(PushNotification.ACTION_TYPE_INVITED_BY_USERNAME),
                invitedMember =  bundle.getParcelable(Member::class.java.simpleName),
                conversationId =  bundle.getString(PushNotification.ACTION_TYPE_INVITE_CONVERSATION_ID)
        )
        showNotification(context, "Invitation from ${invitation.senderUsername} to ${invitation.conversationName}")
    }

    private fun handleText(bundle: Bundle, context: Context){
        val text = bundle.getParcelable(Text::class.java.simpleName) as Text
        showNotification(context, "${text.member.name}: ${text.text}")
    }

    private fun handleImage(bundle: Bundle, context: Context) {
        val image = bundle.getParcelable(Image::class.java.simpleName) as Image
        showNotification(context, "New image from ${image.member.name}")
    }

    private fun showNotification(context: Context, payload: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).apply {
                setContentTitle("Nexmo Stitch Notification")
                //max characters for a push notification is 4KB or about 1000 characters
                setContentText(payload.substring(0, Math.min(1000, payload.length)))
                setAutoCancel(true)
                setVibrate(longArrayOf(0, 100, 100, 100, 100, 100))
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                setSmallIcon(R.drawable.ic_send_black_24dp)
        }

        //On Android versions starting with Oreo (SDK version 26), any local notification your app is trying to build needs to be attached to a NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        // Build and issue the notification. All pending notifications with same id will be canceled.
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

}

data class PushInvitation constructor(val senderMemberId: String, val conversationName: String,
                                      val senderUsername: String, val invitedMember: Member, val conversationId: String)