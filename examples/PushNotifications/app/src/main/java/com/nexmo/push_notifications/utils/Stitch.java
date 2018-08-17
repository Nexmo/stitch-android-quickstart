package com.nexmo.push_notifications.utils;

import android.content.Context;
import android.util.Log;

import com.nexmo.sdk.conversation.client.ConversationClient;

public class Stitch {

    private static Stitch instance = null;
    private ConversationClient client;

    private Stitch(Context context) {
        client = new ConversationClient.ConversationClientBuilder()
                .context(context)
                .onMainThread(true)
                .logLevel(Log.VERBOSE)
                .build();
    }

    public static Stitch getInstance(Context context) {
        if (instance == null) {
            instance = new Stitch(context);
        }
        return instance;
    }

    public ConversationClient getClient() {
        return client;
    }
}
