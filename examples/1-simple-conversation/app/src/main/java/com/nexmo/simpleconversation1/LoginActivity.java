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
    private String CONVERSATION_ID="CON-fbf19d35-6184-4a26-8142-cb7b1a3828c2";
    private String USER_JWT="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE1MDYwMjcwMDMsImp0aSI6IjcxNWUxODQwLTlmMGUtMTFlNy05ZTNkLThmZTUyMTA1MmQ3MCIsInN1YiI6ImphbWllIiwiZXhwIjoiMTUwNjExMzQwMyIsImFjbCI6eyJwYXRocyI6eyIvdjEvc2Vzc2lvbnMvKioiOnt9LCIvdjEvdXNlcnMvKioiOnt9LCIvdjEvY29udmVyc2F0aW9ucy8qKiI6e319fSwiYXBwbGljYXRpb25faWQiOiIyZTZmNDVjZC0wZDc3LTQ5MzItOTg0MC03NGFkZDRkNjJiNjYifQ.AIe907xYz6jhZKKJVRkT4qYeeedHzMgZH5EjA49P59of9JS9SGaxnmhQVAc-ZYAdlDAZ1x_UMgQkj_LCuCzdWV92fiMMtXkPv_BzLbQJbr754sAYVtEVITmgaOxZW4SJ1wmwAmcVgXID5fpqmSvOLnln_bS4mx34Lc03B9B7LSWxkbhCM4YjXlh1j4sn7KeEmZ1S2jVjsvnYEkCTuTIW83FOHGtcsL2IswqtWstMGRhLmvBnuHrp43JN9-somZDL6rZvmoxvjd6xPQNgTaURKccB6gm29OhU7sYkj0rdmqs1WCohVU2tC-YLI3snWeCZItr_0n8XOpLzwVIQN46Mkw";

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