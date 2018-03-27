package com.nexmo.simpleconversation1;

import android.content.Context;

import com.nexmo.sdk.conversation.client.ConversationClient;

class StitchClient {
    private static StitchClient instance = null;
    private ConversationClient conversationClient;

    private StitchClient() {}

    static StitchClient getInstance() {
        if (instance == null) {
            instance = new StitchClient();
        }
        return instance;
    }

    void init(Context applicationContext) {
        conversationClient = new ConversationClient.ConversationClientBuilder().context(applicationContext).build();
    }

    ConversationClient getConversationClient() {
        return conversationClient;
    }
}
