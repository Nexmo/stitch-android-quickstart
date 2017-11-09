package com.nexmo.a3usingevents;

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
    private String USER_JWT;
    private String SECOND_USER_JWT;

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
        return username.toLowerCase().equals("jamie") ? USER_JWT : SECOND_USER_JWT;
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
            public void onError(NexmoAPIError apiError) {
                logAndShow("Error listing conversations: " + apiError.getMessage());
            }

            @Override
            public void onSuccess(List<Conversation> conversationList) {
                if (conversationList.size() > 0) {
                    showConversationList(conversationList);
                } else {
                    logAndShow("You are not a member of any conversations");
                }
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
        conversation.updateEvents(null, null, new RequestHandler<Conversation>() {
            @Override
            public void onError(NexmoAPIError apiError) {
                logAndShow("Error Updating Conversation: " + apiError.getMessage());
            }

            @Override
            public void onSuccess(final Conversation result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                        intent.putExtra("CONVERSATION_ID", conversation.getConversationId());
                        startActivity(intent);
                    }
                });
            }
        });
    }

    private void showLoginSuccessAndAddInvitationListener(final User user) {
        conversationClient.invitedEvent().add(new ResultListener<Invitation>() {
            @Override
            public void onSuccess(final Invitation invitation) {
                logAndShow(invitation.getInvitedBy() + " invited you to their chat");
                invitation.getConversation().join(new RequestHandler<Member>() {
                    @Override
                    public void onError(NexmoAPIError apiError) {
                        logAndShow("Error joining conversation: " + apiError.getMessage());
                    }

                    @Override
                    public void onSuccess(Member member) {
                        goToConversation(invitation.getConversation());
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
