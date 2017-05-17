package com.chris_guzman.simpleconversation1;

import android.app.Application;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.PushEnableListener;
import com.nexmo.sdk.conversation.client.event.ConversationClientException;
import com.nexmo.sdk.conversation.core.client.Router;

import java.lang.reflect.Field;

public class ConversationClientApplication extends Application {
    private ConversationClient conversationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            this.conversationClient = new ConversationClient.ConversationClientBuilder().context(this).build();
            debugIncomingData();
        } catch (ConversationClientException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ConversationClientApplication builder error!", Toast.LENGTH_LONG).show();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public ConversationClient getConversationClient() {
        return this.conversationClient;
    }

    public static void debugIncomingData() throws NoSuchFieldException, IllegalAccessException {
        Field debugField = Router.class.getDeclaredField("DEBUG_INCOMING_DATA");
        debugField.setAccessible(true);
        debugField.set(null, true);
    }
}