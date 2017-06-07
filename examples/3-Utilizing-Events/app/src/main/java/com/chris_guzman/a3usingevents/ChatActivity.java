package com.chris_guzman.a3usingevents;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.Message;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.EventSendListener;
import com.nexmo.sdk.conversation.client.event.MessageListener;
import com.nexmo.sdk.conversation.client.event.SynchronisationListener;

public class ChatActivity extends AppCompatActivity {

    private ConversationClient conversationClient;
    private RecyclerView recyclerView;
    private Conversation convo;
    private ChatAdapter chatAdapter;
    private EditText chatBox;
    private ImageButton sendBtn;
    private String TAG = ChatActivity.class.getSimpleName();
    private MessageListener msgListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        conversationClient = ((ConversationApplication) getApplication()).getConversationClient();
        Intent intent = getIntent();
        String conversationId = intent.getStringExtra("CONVERSATION_ID");
        convo = conversationClient.getConversation(conversationId);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        chatAdapter = new ChatAdapter(ChatActivity.this, convo);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        chatBox = (EditText) findViewById(R.id.chat_box);
        sendBtn = (ImageButton) findViewById(R.id.send_btn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        chatBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || event.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        convo.removeMessageListener(msgListener);
    }

    private void attachListener() {
        msgListener = new MessageListener() {
            @Override
            public void onTextReceived(final Text message) {
                logAndShow( "onTextReceived: " + message.getText());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.swapAdapter(chatAdapter, true);
                    }
                });

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

            @Override
            public void onError(int errCode, String errMessage) {

            }
        };

        convo.addMessageListener(msgListener);
    }

    private void sendMessage() {
        convo.sendText(chatBox.getText().toString(), new EventSendListener() {
            @Override
            public void onSent(Message message) {
                String textMsg = ((Text) message).getText();
                logAndShow( "onSent: " + textMsg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatBox.setText(null);
                    }
                });
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow( "onSent: error" + errMessage + " / " + errCode);
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
