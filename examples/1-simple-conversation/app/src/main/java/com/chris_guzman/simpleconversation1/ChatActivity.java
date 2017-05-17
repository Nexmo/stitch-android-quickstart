package com.chris_guzman.simpleconversation1;

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
import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.Message;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.EventSendListener;
import com.nexmo.sdk.conversation.client.event.MessageListener;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "CAPI-DEMO";

    private TextView chatTxt;
    private EditText msgEditTxt;
    private Button sendMsgBtn;

    private ConversationClient conversationClient;
    private Conversation convo;
    private MessageListener messageListener;

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
        convo = conversationClient.getConversation(conversationId);
        addListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        convo.removeMessageListener(messageListener);
    }

    private void sendMessage() {
        if (convo != null) {
            convo.sendText(msgEditTxt.getText().toString(), new EventSendListener() {
                @Override
                public void onSent(Message message) {
                    //intentionally left blank
                }

                @Override
                public void onError(int errCode, String errMessage) {
                    logAndShow("onMessageSent Error. Code " + errCode + " Message: " + errMessage);
                }
            });
        } else {
            logAndShow("Error sendText: convo is null");
        }
    }

    private void addListener() {
        if (convo != null) {
            messageListener = new MessageListener() {
                @Override
                public void onError(int errCode, String errMessage) {
                    logAndShow("MessageListener error " + errCode + " / " + errMessage);
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
            convo.addMessageListener(messageListener);
        } else {
            logAndShow("Error adding TextListener: convo is null");
        }

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
