package com.nexmo.callingusers;

import android.app.Application;

import com.nexmo.sdk.conversation.client.ConversationClient;

public class ConversationClientApplication extends Application {

    private ConversationClient conversationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        conversationClient = new ConversationClient.ConversationClientBuilder().context(this).build();
    }

    public ConversationClient getConversationClient() {
        return conversationClient;
    }
}
