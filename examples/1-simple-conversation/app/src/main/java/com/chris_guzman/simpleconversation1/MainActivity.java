package com.chris_guzman.simpleconversation1;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationCreateListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.JoinListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LoginListener;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CAPI-DEMO";
    String userJwt = "";

    private Button loginBtn;
    private Button createConvoBtn;
    private Button joinConvoBtn;

    private ConversationClient conversationClient;
    private Conversation convo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConversationClientApplication application = (ConversationClientApplication) getApplication();
        conversationClient = application.getConversationClient();

        loginBtn = (Button) findViewById(R.id.user_login_btn);
        createConvoBtn = (Button) findViewById(R.id.create_convo_btn);
        joinConvoBtn = (Button) findViewById(R.id.join_convo_btn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(v);
            }
        });

        createConvoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createConversation(v.getContext());
            }
        });


        joinConvoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinConversation(v.getContext());
            }
        });
    }

    private void loginUser(View v) {
        final Context context = v.getContext();
        conversationClient.login(userJwt, new LoginListener() {
            @Override
            public void onLogin(User user) {
                logAndShow(context, user.getName() + " logged in!");
            }

            @Override
            public void onUserAlreadyLoggedIn(User user) {
                logAndShow(context, "Silly " + user.getName() + " you're already logged in!");
            }

            @Override
            public void onTokenInvalid() {
                logAndShow(context, "Error token invalid. Generate new token");
            }

            @Override
            public void onTokenExpired() {
                logAndShow(context, "Error token expired. Generate new token");
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow(context, "On Login Error. Code" + errCode + "Message: " + errMessage);
            }
        });
    }

    private void createConversation(final Context context) {
        conversationClient.newConversation(new Date().toString(), new ConversationCreateListener() {
            @Override
            public void onConversationCreated(Conversation conversation) {
                logAndShow(context, "Conversation created: " + conversation.getName() + " / " + conversation.getConversationId());
                convo = conversation;
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow(context, "onConversationCreated Error. Code " + errCode + " Message: " + errMessage);
            }
        });
    }

    private void joinConversation(final Context context) {
        convo.join(new JoinListener() {
            @Override
            public void onConversationJoined(Conversation conversation, Member member) {
                logAndShow(context, member.getName() + " has joined " + conversation.getConversationId());
                goToChatActivity(context);
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow(context, "onConversationJoined Error. Code " + errCode + " Message: " + errMessage);
            }
        });
    }

    private void goToChatActivity(Context context) {
        //TODO: Make ChatActivity
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("CONVERSATION-ID", convo.getConversationId());
        startActivity(intent);
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