package com.nexmo.simpleconversation1;

import android.app.Application;

import com.nexmo.sdk.conversation.client.ConversationClient;

public class ConversationClientApplication extends Application {
    private ConversationClient conversationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        this.conversationClient = new ConversationClient.ConversationClientBuilder().context(this).build();
    }

    public ConversationClient getConversationClient() {
        return this.conversationClient;
    }
}