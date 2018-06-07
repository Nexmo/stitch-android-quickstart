package com.nexmo.simpleconversation1;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.Event;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.EventType;
import com.nexmo.sdk.conversation.client.event.ResultListener;
import com.nexmo.sdk.conversation.core.SubscriptionList;

class StitchListenerComponent implements LifecycleObserver {
    private final String TAG = this.getClass().getSimpleName();
    private Conversation conversation;
    private final EditText msgEditTxt;
    private final TextView chatTxt;
    private SubscriptionList subscriptions = new SubscriptionList();

    StitchListenerComponent(Conversation conversation, EditText msgEditTxt, TextView chatTxt) {
        this.conversation = conversation;
        this.msgEditTxt = msgEditTxt;
        this.chatTxt = chatTxt;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
        subscriptions.unsubscribeAll();
        Log.d(TAG, "onPause: Unsubscribe");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
        Log.d(TAG, "onResume: Subscribe to message events");
        conversation.messageEvent().add(new ResultListener<Event>() {
            @Override
            public void onSuccess(Event message) {
                showMessage(message);
            }
        }).addTo(subscriptions);
    }

    private void showMessage(final Event message) {
        if (message.getType().equals(EventType.TEXT)) {
            Text text = (Text) message;
            msgEditTxt.setText(null);
            final String prevText = chatTxt.getText().toString();
            chatTxt.setText(prevText + "\n" + text.getText());
        }
    }
}
