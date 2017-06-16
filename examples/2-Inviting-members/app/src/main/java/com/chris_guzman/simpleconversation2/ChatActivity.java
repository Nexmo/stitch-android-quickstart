package com.chris_guzman.simpleconversation2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.nexmo.sdk.conversation.client.event.CompletionListeners.InviteSendListener;
import com.nexmo.sdk.conversation.client.event.MessageListener;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();

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

        conversationClient = ((ConversationClientApplication) getApplication()).getConversationClient();

        Intent intent = getIntent();
        String conversationId = intent.getStringExtra("CONVERSATION_ID");
        convo = conversationClient.getConversation(conversationId);

        addListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        convo.removeMessageListener(messageListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite:
                inviteUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void inviteUser() {
        final String otherUser = (conversationClient.getLoggedInUser().getName().equals("tom") ? "jerry" : "tom");
        convo.invite(otherUser, new InviteSendListener() {
            @Override
            public void onInviteSent(Member invitedMember) {
                logAndShow("Invite sent to: " + invitedMember.getName());
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error sending invite: " + errMessage);
            }
        });
    }

    private void sendMessage() {
        convo.sendText(msgEditTxt.getText().toString(), new EventSendListener() {
            @Override
            public void onSent(Message message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        msgEditTxt.setText(null);
                    }
                });
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error sending message: " + errMessage);
            }
        });
    }

    private void addListener() {
        messageListener = new MessageListener() {
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

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("onError: " + errMessage + errCode);
            }
        };

        convo.addMessageListener(messageListener);
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