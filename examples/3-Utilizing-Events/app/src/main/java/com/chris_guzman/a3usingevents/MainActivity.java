package com.chris_guzman.a3usingevents;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ConversationClient conversationClient;
    private Button loginBtn;
    private String chrisToken;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        conversationClient = ((ConversationApplication) getApplication()).getConversationClient();

        loginBtn = (Button) findViewById(R.id.login);
        logoutBtn = (Button) findViewById(R.id.logout);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
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
                logAndShow("onLogout: " + user.getName());
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("onLogout: error " + errMessage + " / " + errCode);
            }
        });
    }

    private void login() {
        conversationClient.login(chrisToken, new LoginListener() {
            @Override
            public void onLogin(User user) {
                logAndShow("onLogin: " + user.getName());
                createOrGoToConversation(user);
            }

            @Override
            public void onUserAlreadyLoggedIn(User user) {
                logAndShow("onUserAlreadyLoggedIn: " + user.getName());
                createOrGoToConversation(user);
            }

            @Override
            public void onTokenInvalid() {
                logAndShow( "onTokenInvalid: ");
            }

            @Override
            public void onTokenExpired() {
                logAndShow( "onTokenExpired: ");
            }

            @Override
            public void onError(int errCode, String errMessage) {
                Log.d(TAG, "login onError: " + errMessage + " / " + errCode);
            }
        });
    }

    private void createOrGoToConversation(User user) {
        conversationClient.getConversations(new ConversationListListener() {
            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("onConversationList onError: " + errMessage + " / " + errCode);
            }

            @Override
            public void onConversationList(final List<Conversation> conversationList) {
                final List<String> conversationNames = new ArrayList<>(conversationList.size());
                for (Conversation convo : conversationList) {
                    conversationNames.add(convo.getName());
                }

                final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Create or join a conversation")
                        .setNegativeButton("New conversation", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showCreateConvoDialog();
                            }
                        })
                        .setItems(conversationNames.toArray(new CharSequence[conversationNames.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goToChatActivity(conversationList.get(which).getConversationId());
                            }
                        });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.show();
                    }
                });
            }
        });

    }

    private void showCreateConvoDialog() {
        final EditText input = new EditText(MainActivity.this);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Enter conversation name")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createConversation(input.getText().toString());
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


    private void createConversation(String conversationName) {
            conversationClient.newConversation(conversationName, new ConversationCreateListener() {
                @Override
                public void onConversationCreated(Conversation conversation) {
                    logAndShow("onConversationCreated: " + conversation);
                    showJoinDialog(conversation);
                }

                @Override
                public void onError(int errCode, String errMessage) {
                    logAndShow("onConversationCreated error: " + errMessage + errCode);
                }
            });

    }

    private void showJoinDialog(final Conversation conversation) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle("Join " + conversation.getName() + " conversation?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        joinConversation(conversation);
                    }
                });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.show();
            }
        });
    }

    private void joinConversation(final Conversation conversation) {
        conversation.join(new JoinListener() {
            @Override
            public void onConversationJoined(Member member) {
                logAndShow( "onConversationJoined: " + member.getName());
                goToChatActivity(conversation.getConversationId());
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow( "onConversationJoined: error" + errMessage + errCode);
            }
        });
    }

    private void goToChatActivity(final String conversationId) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                .setTitle("Go to chat activity?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        intent.putExtra("CONVERSATION_ID", conversationId);
                        startActivity(intent);
                    }
                });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.show();
            }
        });
    }

    private void logAndShow(final String message) {
        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
