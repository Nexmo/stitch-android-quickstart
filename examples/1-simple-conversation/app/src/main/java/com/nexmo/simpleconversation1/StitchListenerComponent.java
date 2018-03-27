package com.nexmo.simpleconversation1;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.util.Log;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Event;
import com.nexmo.sdk.conversation.client.event.ResultListener;
import com.nexmo.sdk.conversation.core.SubscriptionList;

class StitchListenerComponent implements LifecycleObserver {
    private final String TAG = this.getClass().getSimpleName();
    private Conversation conversation;
    private SubscriptionList subscriptions = new SubscriptionList();
    private ChatActivity chatActivityClass;

    StitchListenerComponent(Conversation conversation, ChatActivity chatActivityClass) {
        this.conversation = conversation;
        this.chatActivityClass = chatActivityClass;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
        subscriptions.unsubscribeAll();
        Log.d(TAG, "onPause: Unsubscribe");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onCreate() {
        Log.d(TAG, "onResume: Subscribe to message events");
        conversation.messageEvent().add(new ResultListener<Event>() {
            @Override
            public void onSuccess(Event message) {
                chatActivityClass.showMessage(message);
            }
        }).addTo(subscriptions);
    }
}
