package com.nexmo.simpleconversation1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.client.event.LoginListener;
import com.nexmo.sdk.conversation.client.event.NexmoAPIError;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = LoginActivity.class.getSimpleName();
    private String CONVERSATION_ID;
    private String USER_JWT;

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
                goToChatActivity();
            }
        });
    }

    private String authenticate() {
        return USER_JWT;
    }

    private void login() {
        loginTxt.setText("Logging in...");

        String userToken = authenticate();
        conversationClient.login(userToken, new LoginListener() {
            @Override
            public void onSuccess(User user) {
                showLoginSuccess(user);
            }

            @Override
            public void onError(NexmoAPIError apiError) {
                logAndShow("Login Error: " + apiError.getMessage());
            }

            @Override
            public void onUserAlreadyLoggedIn(User user) {
                showLoginSuccess(user);
            }

            @Override
            public void onTokenInvalid() {
                logAndShow("Token Invalid.");
                loginTxt.setText("Token Invalid");
            }

            @Override
            public void onTokenExpired() {
                logAndShow("Token Expired. Generate new token.");
                loginTxt.setText("Token Expired. Generate new token.");
            }
        });
    }

    private void showLoginSuccess(final User user) {
        loginTxt.setText("Logged in as " + user.getName() + "\nGo to a conversation!");
    }

    private void goToChatActivity() {
        Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
        intent.putExtra("CONVERSATION-ID", CONVERSATION_ID);
        startActivity(intent);
    }

    private void logAndShow(final String message) {
        Log.d(TAG, message);
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}