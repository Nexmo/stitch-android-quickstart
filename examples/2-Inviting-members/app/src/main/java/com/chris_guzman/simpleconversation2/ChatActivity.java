package com.chris_guzman.simpleconversation2;

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
import com.nexmo.sdk.conversation.client.event.EventListener;
import com.nexmo.sdk.conversation.client.event.NexmoAPIError;
import com.nexmo.sdk.conversation.client.event.RequestHandler;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();

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

        conversationClient = ((ConversationClientApplication) getApplication()).getConversationClient();

        Intent intent = getIntent();
        String conversationId = intent.getStringExtra("CONVERSATION_ID");
        conversation = conversationClient.getConversation(conversationId);

        addListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conversation.removeEventListener(eventListener);
    }

    private void sendMessage() {
        conversation.sendText(msgEditTxt.getText().toString(), new RequestHandler<Event>() {
            @Override
            public void onSuccess(Event result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        msgEditTxt.setText(null);
                    }
                });
            }

            @Override
            public void onError(NexmoAPIError apiError) {
                logAndShow("Error sending message: " + apiError.getMessage());
            }
        });
    }

    private void addListener() {
        eventListener = new EventListener() {
            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onError(NexmoAPIError apiError) {
                logAndShow("onError: " + apiError.getMessage());

            }

            @Override
            public void onTextReceived(Text message) {
                showMessage(message);
            }

            @Override
            public void onTextDeleted(Text message, Member member) {

            }

            @Override
            public void onImageReceived(Image image) {

            }

            @Override
            public void onImageDeleted(Image message, Member member) {

            }

            @Override
            public void onImageDownloaded(Image image) {

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
                chatTxt.setText(prevText + "\n" + message.getMember().getName() + ": " + message.getText());
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