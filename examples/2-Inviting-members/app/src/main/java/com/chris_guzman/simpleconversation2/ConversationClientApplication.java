package com.chris_guzman.simpleconversation2;

import android.app.Application;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.event.ConversationClientException;

public class ConversationClientApplication extends Application {
    private ConversationClient conversationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            this.conversationClient = new ConversationClient.ConversationClientBuilder().context(this).build();
        } catch (ConversationClientException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ConversationClientApplication builder error!", Toast.LENGTH_LONG).show();
        }
    }

    public ConversationClient getConversationClient() {
        return this.conversationClient;
    }
}