package com.nexmo.simpleconversation1;


import com.nexmo.sdk.conversation.client.Event;

interface ReceiveMessageHandler {
    void showMessage(Event message);
}
