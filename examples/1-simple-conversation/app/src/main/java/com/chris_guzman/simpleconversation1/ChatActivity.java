package com.chris_guzman.simpleconversation1;

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
import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.EventSendListener;
import com.nexmo.sdk.conversation.client.event.EventListener;
import com.nexmo.sdk.conversation.client.event.EventType;

public class ChatActivity extends AppCompatActivity {
    private final String TAG = ChatActivity.this.getClass().getSimpleName();

    private TextView chatTxt;
    private EditText msgEditTxt;
    private Button sendMsgBtn;

    private ConversationClient conversationClient;
    private Conversation conversation;
    private EventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatTxt = (TextView) findViewById(R.id.chat_txt);
        msgEditTxt = (EditText) findViewById(R.id.msg_edit_txt);
        sendMsgBtn = (Button) findViewById(R.id.send_msg_btn);
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        ConversationClientApplication application = (ConversationClientApplication) getApplication();
        conversationClient = application.getConversationClient();

        Intent intent = getIntent();
        String conversationId = intent.getStringExtra("CONVERSATION-ID");
        conversation = conversationClient.getConversation(conversationId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conversation.removeEventListener(eventListener);
    }

    private void sendMessage() {
        conversation.sendText(msgEditTxt.getText().toString(), new EventSendListener() {
            @Override
            public void onSent(Event event) {
                if (event.getType().equals(EventType.TEXT)) {
                    Log.d(TAG, "onSent: " + ((Text) event).getText());
                }
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error sending message: " + errMessage);
            }
        });
    }

    private void addListener() {
        eventListener = new EventListener() {
            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error adding EventListener: " + errMessage);
            }

            @Override
            public void onImageDownloaded(Image image) {
                //intentionally left blank
            }

            @Override
            public void onTextReceived(Text message) {
                showMessage(message);
            }

            @Override
            public void onTextDeleted(Text message, Member member) {
                //intentionally left blank
            }

            @Override
            public void onImageReceived(Image image) {
                //intentionally left blank
            }

            @Override
            public void onImageDeleted(Image message, Member member) {
                //intentionally left blank
            }
        };
        conversation.addEventListener(eventListener);
    }

    private void showMessage(final Text message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgEditTxt.setText(null);
                String prevText = chatTxt.getText().toString();
                chatTxt.setText(prevText + "\n" + message.getText());
            }
        });
    }

    private void logAndShow(final String message) {
        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
