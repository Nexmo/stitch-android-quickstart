package com.nexmo.simpleconversation1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Event;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.EventType;
import com.nexmo.sdk.conversation.client.event.NexmoAPIError;
import com.nexmo.sdk.conversation.client.event.RequestHandler;

public class ChatActivity extends AppCompatActivity implements ReceiveMessageHandler {
    private final String TAG = ChatActivity.this.getClass().getSimpleName();

    private TextView chatTxt;
    private EditText msgEditTxt;
    private Button sendMsgBtn;

    private ConversationClient conversationClient;
    private Conversation conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatTxt = findViewById(R.id.chat_txt);
        msgEditTxt = findViewById(R.id.msg_edit_txt);
        sendMsgBtn = findViewById(R.id.send_msg_btn);
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        conversationClient = StitchClient.getInstance().getConversationClient();

        Intent intent = getIntent();
        String conversationId = intent.getStringExtra("CONVERSATION-ID");
        conversation = conversationClient.getConversation(conversationId);

        getLifecycle().addObserver(new StitchListenerComponent(conversation, this));
    }

    private void sendMessage() {
        conversation.sendText(msgEditTxt.getText().toString(), new RequestHandler<Event>() {
            @Override
            public void onSuccess(Event event) {
                if (event.getType().equals(EventType.TEXT)) {
                    Log.d(TAG, "onSent: " + ((Text) event).getText());
                }
            }

            @Override
            public void onError(NexmoAPIError apiError) {
                logAndShow("Error sending message: " + apiError.getMessage());
            }
        });
    }

    @Override
    public void showMessage(final Event message) {
        if (message.getType().equals(EventType.TEXT)) {
            Text text = (Text) message;
            msgEditTxt.setText(null);
            final String prevText = chatTxt.getText().toString();
            chatTxt.setText(prevText + "\n" + text.getText());
        }
    }

    private void logAndShow(final String message) {
        Log.d(TAG, message);
        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
