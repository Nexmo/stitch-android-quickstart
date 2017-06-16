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
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationCreateListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationListListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.JoinListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LoginListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LogoutListener;
import com.nexmo.sdk.conversation.client.event.ConversationInvitedListener;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private String tomToken;
    private String jerryToken;
    private Button loginBtn;
    private Button chatBtn;
    private ConversationClient conversationClient;
    private TextView loginTxt;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        conversationClient = ((ConversationClientApplication) getApplication()).getConversationClient();

        loginTxt = (TextView) findViewById(R.id.login_text);
        loginBtn = (Button) findViewById(R.id.login);
        chatBtn = (Button) findViewById(R.id.chat);
        logoutBtn = (Button) findViewById(R.id.logout);

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
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        conversationClient.logout(new LogoutListener() {
            @Override
            public void onLogout(User user) {
                logAndShow(user.getName() + " logged out");
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error logging out: " + errMessage);
            }
        });
    }

    private void login() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Which user are you logging in as?")
                .setItems(new String[]{"tom", "jerry"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            loginAsUser(tomToken);
                        } else {
                            loginAsUser(jerryToken);
                        }
                    }
                });

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
            public void onLogin(User user) {
                showLoginSuccessAndAddInvitationListener(user);
                retrieveConversations();
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

            @Override
            public void onError(int errCode, final String errMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginTxt.setText("Login Error: " + errMessage);
                    }
                });
                logAndShow("Login Error: " + errMessage);
            }
        });
    }

    private void retrieveConversations() {
        conversationClient.getConversations(new ConversationListListener() {
            @Override
            public void onConversationList(List<Conversation> conversationList) {
                if (conversationList.size() > 0) {
                    showConversationList(conversationList);
                } else {
                    showCreateConversationDialog();
                }
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error listing conversations: " + errMessage);
            }
        });
    }

    private void showConversationList(final List<Conversation> conversationList) {
        List<String> conversationNames = new ArrayList<>(conversationList.size());
        for (Conversation convo : conversationList) {
            conversationNames.add(convo.getDisplayName());
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Enter or create a conversation")
                .setItems(conversationNames.toArray(new CharSequence[conversationNames.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToConversation(conversationList.get(which));
                    }
                })
                .setPositiveButton("New conversation", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showCreateConversationDialog();
                    }
                })
                .setNegativeButton("Dismiss", null);
        ;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private void showCreateConversationDialog() {
        final EditText input = new EditText(LoginActivity.this);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Enter conversation name")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createConversation(input.getText().toString());
                    }
                })
                .setNegativeButton("Dismiss", null);

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

    private void createConversation(String name) {
        conversationClient.newConversation(name, new ConversationCreateListener() {
            @Override
            public void onConversationCreated(final Conversation conversation) {
                conversation.join(new JoinListener() {
                    @Override
                    public void onConversationJoined(Member member) {
                        logAndShow("Created and joined Conversation: " + conversation.getDisplayName());
                        goToConversation(conversation);
                    }

                    @Override
                    public void onError(int errCode, String errMessage) {
                        logAndShow("Error joining conversation: " + errMessage);
                    }
                });
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error creating conversation: " + errMessage);
            }
        });

    }

    private void goToConversation(final Conversation conversation) {
        Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
        intent.putExtra("CONVERSATION_ID", conversation.getConversationId());
        startActivity(intent);
    }

    private void showLoginSuccessAndAddInvitationListener(final User user) {
        conversationClient.addConversationInvitedListener(new ConversationInvitedListener() {
            @Override
            public void onConversationInvited(final Conversation conversation, Member invitedMember, String invitedByUsername) {
                logAndShow(invitedByUsername + " invited you to their chat");
                conversation.join(new JoinListener() {
                    @Override
                    public void onConversationJoined(Member member) {
                        goToConversation(conversation);
                    }

                    @Override
                    public void onError(int errCode, String errMessage) {
                        logAndShow("Error joining conversation: " + errMessage);
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
