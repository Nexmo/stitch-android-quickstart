package com.chris_guzman.a3usingevents;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.client.event.LoginListener;
import com.nexmo.sdk.conversation.client.event.NexmoAPIError;
import com.nexmo.sdk.conversation.client.event.RequestHandler;
import com.nexmo.sdk.conversation.client.event.ResultListener;
import com.nexmo.sdk.conversation.client.event.container.Invitation;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private String USER_JWT="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE1MDU0MDY3NTEsImp0aSI6IjRlNTBmMTAwLTk5NmEtMTFlNy04YTc4LThkM2UxYjA5NzJiNiIsInN1YiI6ImphbWllIiwiZXhwIjoiMTUwNTQ5MzE1MSIsImFjbCI6eyJwYXRocyI6eyIvdjEvc2Vzc2lvbnMvKioiOnt9LCIvdjEvdXNlcnMvKioiOnt9LCIvdjEvY29udmVyc2F0aW9ucy8qKiI6e319fSwiYXBwbGljYXRpb25faWQiOiI0ZmE2YjhjMi03M2JlLTQ3ODktYTU4ZC0wZWM3MTA5YWNkOGEifQ.VyPtIuz7NDOwOJomtlsZH2Ve9afWE-R-QN0XryeR-6aCKU4WbNjQ0G71I5Jkjghh0IwY-QsN7VAcViET2zugsWKcJmk0mVan_BIIVG_n_ZgOtxOkxPWFtWMHdu2DCEF7AIqtozOpF9_idnVEJx8OE9zGjj16SuqXNfae4RvCZJWVmTSZQxdgA9JQF8YZeq0CCnWiPVYL0v9HlR_425YjNgN7HgmWd8dQXxp7NjZ794vR4FuBGYBOkiXG07BdpnezMIzsG5ToNZwg7sxztw-XPMnc4WyrlWA6YHsXrs6IhOD43B2gYpLeRfAwHH-B4ddFzYHmxXW55od4ExeN0kkhDQ";
    private String SECOND_USER_JWT="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE1MDU0MTE5OTUsImp0aSI6IjgzZmQwZjgwLTk5NzYtMTFlNy04NWJjLWJmYTM1NWJkMDAyZSIsInN1YiI6ImFsaWNlIiwiZXhwIjoiMTUwNTQ5ODM5NSIsImFjbCI6eyJwYXRocyI6eyIvdjEvc2Vzc2lvbnMvKioiOnt9LCIvdjEvdXNlcnMvKioiOnt9LCIvdjEvY29udmVyc2F0aW9ucy8qKiI6e319fSwiYXBwbGljYXRpb25faWQiOiI0ZmE2YjhjMi03M2JlLTQ3ODktYTU4ZC0wZWM3MTA5YWNkOGEifQ.qta1qRDM3tUwpShDwCXL-KfMaOuUKr1HgeWJ0S-kqdqFJoLlneAM9Zgvn_g8qvqf9BJeQC6_5osqLpmvdz3gLPR-d11ZQ-Yj3mkdgKOonaIeyUlGUHLCtgCCGds4WT0MeprOvDC_nyzVkZWS_11SPzrk1lp68CNP5UsWZxg5N_9h1-rdpJEO_I3RhM3Tfhn7Bc6uv-JUbVPOmmTtgAGVlOXDPPQLkjK6y8m3Ev9ylYBw6d1lixHiWzlPTJIuJBPoHv3uwYfBNGr5cOxRGkTTdW3Z1jOIJqXzEsf4oLRRm9b984v3uX6QVdhpPhsFTD-uCfqJ2iW6THgxAvT-dChXUA";

    private Button loginBtn;
    private Button chatBtn;
    private ConversationClient conversationClient;
    private TextView loginTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        conversationClient = ((ConversationClientApplication) getApplication()).getConversationClient();

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
                retrieveConversations();
            }
        });
    }

    private String authenticate(String username) {
        return username.toLowerCase().contains("j") ? USER_JWT : SECOND_USER_JWT;
//        return username.toLowerCase().equals("jamie") ? USER_JWT : SECOND_USER_JWT;
    }

    private void login() {
        final EditText input = new EditText(LoginActivity.this);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Enter your username")
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userToken = authenticate(input.getText().toString());
                        loginAsUser(userToken);
                    }
                });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        dialog.setView(input);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private void loginAsUser(String token) {
        loginTxt.setText("Logging in...");
        conversationClient.login(token, new LoginListener() {
            @Override
            public void onSuccess(User user) {
                showLoginSuccessAndAddInvitationListener(user);
                retrieveConversations();
            }

            @Override
            public void onError(final NexmoAPIError apiError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginTxt.setText("Login Error: " + apiError.getMessage());
                    }
                });
                logAndShow("Login Error: " + apiError.getMessage());
            }

            @Override
            public void onUserAlreadyLoggedIn(User user) {
                showLoginSuccessAndAddInvitationListener(user);
                retrieveConversations();
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
        });
    }

    private void retrieveConversations() {
        conversationClient.getConversations(new RequestHandler<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversationList) {
                if (conversationList.size() > 0) {
                    showConversationList(conversationList);
                } else {
                    logAndShow("You are not a member of any conversations");
                }
            }

            @Override
            public void onError(NexmoAPIError apiError) {
                logAndShow("Error listing conversations: " + apiError.getMessage());
            }
        });
    }

    private void showConversationList(final List<Conversation> conversationList) {
        List<String> conversationNames = new ArrayList<>(conversationList.size());
        for (Conversation convo : conversationList) {
            conversationNames.add(convo.getDisplayName());
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Choose a conversation")
                .setItems(conversationNames.toArray(new CharSequence[conversationNames.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToConversation(conversationList.get(which));
                    }
                });
        ;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private void goToConversation(final Conversation conversation) {
        Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
        intent.putExtra("CONVERSATION_ID", conversation.getConversationId());
        startActivity(intent);
    }

    private void showLoginSuccessAndAddInvitationListener(final User user) {
        conversationClient.invitedEvent().add(new ResultListener<Invitation>() {
            @Override
            public void onSuccess(Invitation result) {
                logAndShow(result.getInvitedBy() + " invited you to their chat");
                result.getConversation().join(new RequestHandler<Member>() {
                    @Override
                    public void onSuccess(Member result) {
                        goToConversation(result.getConversation());
                    }

                    @Override
                    public void onError(NexmoAPIError apiError) {
                        logAndShow("Error joining conversation: " + apiError.getMessage());
                    }
                });
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginTxt.setText("Logged in as " + user.getName() + "\nStart chatting!");
            }
        });
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
