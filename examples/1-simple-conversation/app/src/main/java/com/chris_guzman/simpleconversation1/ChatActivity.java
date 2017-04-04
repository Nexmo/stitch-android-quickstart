package com.chris_guzman.simpleconversation1;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.Message;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.EventSendListener;
import com.nexmo.sdk.conversation.client.event.TextListener;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "CAPI-DEMO";

    private TextView chatTxt;
    private EditText msgEditTxt;
    private Button sendMsgBtn;

    private ConversationClient conversationClient;
    private Conversation convo;

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
                sendMessage(v);
            }
        });

        ConversationClientApplication application = (ConversationClientApplication) getApplication();
        conversationClient = application.getConversationClient();

        Intent intent = getIntent();
        String conversationId = intent.getStringExtra("CONVERSATION-ID");
        convo = conversationClient.getConversation(conversationId);
        addListener();
    }

    private void sendMessage(final View v) {
        convo.sendText(msgEditTxt.getText().toString(), new EventSendListener() {
            @Override
            public void onSent(Conversation conversation, Message message) {
                //intentionally left blank
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow(v.getContext(), "onMessageSent Error. Code " + errCode + " Message: " + errMessage);
            }
        });
    }

    private void addListener() {
        if (convo != null) {
            convo.addTextListener(new TextListener() {
                @Override
                public void onTextReceived(Conversation conversation, Text message) {
                    showMessage(message);
                }

                @Override
                public void onTextDeleted(Conversation conversation, Text message, Member member) {
                    //intentionally left blank
                }
            });
        } else {
            logAndShow(this, "Error adding TextListener: convo is null");
        }
    }

    private void showMessage(final Text message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgEditTxt.setText(null);
                String prevText = chatTxt.getText().toString();
                chatTxt.setText(prevText + "\n" + message.getPayload());
            }
        });
    }

    private void logAndShow(final Context context, final String message) {
        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
