package com.chris_guzman.a3usingevents;

import android.app.Application;
import android.util.Log;

import com.nexmo.sdk.conversation.client.ConversationClient;

/**
 * Created by chrisguzman on 6/5/17.
 */

public class ConversationApplication extends Application {

    private ConversationClient conversationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        conversationClient = new ConversationClient.ConversationClientBuilder().context(this).logLevel(Log.VERBOSE).build();
    }

    public ConversationClient getConversationClient() {
        return conversationClient;
    }
}
