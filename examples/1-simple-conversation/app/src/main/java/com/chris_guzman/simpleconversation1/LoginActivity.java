package com.chris_guzman.simpleconversation1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationCreateListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.JoinListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LoginListener;

import java.util.Date;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = LoginActivity.this.getClass().getSimpleName();
    private String userJwt;

    private ConversationClient conversationClient;
    private TextView loginTxt;
    private Button loginBtn;
    private Button chatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ConversationClientApplication application = (ConversationClientApplication) getApplication();
        conversationClient = application.getConversationClient();

        loginTxt = (TextView) findViewById(R.id.login_text);
        loginBtn = (Button) findViewById(R.id.login);
        chatBtn = (Button) findViewById(R.id.chat);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createConversation();
            }
        });
    }

    private void login() {
        loginTxt.setText("Logging in...");
        conversationClient.login(userJwt, new LoginListener() {
            @Override
            public void onLogin(final User user) {
                showLoginSuccess(user);
            }

            @Override
            public void onUserAlreadyLoggedIn(User user) {
                showLoginSuccess(user);
            }

            @Override
            public void onTokenInvalid() {
                logAndShow("Token Invalid.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginTxt.setText("Token Invalid");
                    }
                });
            }

            @Override
            public void onTokenExpired() {
                logAndShow("Token Expired. Generate new token.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginTxt.setText("Token Expired. Generate new token.");
                    }
                });
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Login Error: " + errMessage);
            }
        });
    }

    private void showLoginSuccess(final User user) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginTxt.setText("Logged in as " + user.getName() + "\nStart a new conversation");
            }
        });
    }

    private void createConversation() {
        conversationClient.newConversation(new Date().toString(), new ConversationCreateListener() {
            @Override
            public void onConversationCreated(Conversation conversation) {
                logAndShow("Conversation created: " + conversation.getDisplayName());
                joinConversation(conversation);
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error creating conversation: " + errMessage);
            }
        });
    }

    private void joinConversation(final Conversation conversation) {
        conversation.join(new JoinListener() {
            @Override
            public void onConversationJoined(Member member) {
                goToChatActivity(conversation);
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error joining conversation: " + errMessage);
            }
        });
    }

    private void goToChatActivity(Conversation conversation) {
        Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
        intent.putExtra("CONVERSATION-ID", conversation.getConversationId());
        startActivity(intent);
    }

    private void logAndShow(final String message) {
        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}