package com.chris_guzman.simpleconversation2;

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
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationCreateListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationListListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.JoinListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.LoginListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private String jamieToken;
    private String adamToken;
    private Button loginBtn;
    private Button createConversationBtn;
    private ConversationClient client;
    private Button joinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = ((ConversationClientApplication) getApplication()).getConversationClient();

        loginBtn = (Button) findViewById(R.id.login);
        createConversationBtn = (Button) findViewById(R.id.create_conversation);
        joinBtn = (Button) findViewById(R.id.join);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });

        createConversationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateConvoDialog();
            }
        });

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveConversations();
            }
        });
    }

    private void logIn() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Which user are you logging in as?")
                .setItems(new String[]{"jamie", "adam"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            loginAsUser(jamieToken);
                        } else {
                            loginAsUser(adamToken);
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
        client.login(token, new LoginListener() {
            @Override
            public void onLogin(User user) {
                Log.d(TAG, "onLogin: ");
            }

            @Override
            public void onUserAlreadyLoggedIn(User user) {
                Log.d(TAG, "onUserAlreadyLoggedIn: ");
            }

            @Override
            public void onTokenInvalid() {
                Log.d(TAG, "onTokenInvalid: ");
            }

            @Override
            public void onTokenExpired() {
                Log.d(TAG, "onTokenExpired: ");
            }

            @Override
            public void onError(int errCode, String errMessage) {
                Log.d(TAG, "onLogin onError: " + errMessage + " / " + errCode);
            }
        });
    }

    private void retrieveConversations() {
        client.getConversations(new ConversationListListener() {
            @Override
            public void onConversationList(List<Conversation> conversationList) {
                Log.d(TAG, "onConversationList: list count = " + conversationList.size());
                if (conversationList.size() > 0) {
                    showConversationList(conversationList);
                } else {
                    showNoConversations();
                }
            }

            @Override
            public void onError(int errCode, String errMessage) {
                Log.d(TAG, "onConversationList onError: " + errMessage + " / " + errCode);
            }
        });
    }

    private void showConversationList(final List<Conversation> conversationList) {
        List<String> conversationNames = new ArrayList<>(conversationList.size());
        for (Conversation convo : conversationList) {
            conversationNames.add(convo.getName());
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Choose conversation to join")
                .setItems(conversationNames.toArray(new CharSequence[conversationNames.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        joinConversation(conversationList.get(which));
                    }
                });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private void showNoConversations() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "No conversations found, create a new conversation", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCreateConvoDialog() {
        final EditText input = new EditText(MainActivity.this);
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
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
        dialog.show();

    }

    private void createConversation(String name) {
        client.newConversation(name, new ConversationCreateListener() {
            @Override
            public void onConversationCreated(Conversation conversation) {
                showJoinDialog(conversation);
            }

            @Override
            public void onError(int errCode, String errMessage) {
                Log.d(TAG, "onConversationCreated onError: ");
            }
        });

    }

    private void showJoinDialog(final Conversation conversation) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this).setTitle("Conversation created! Press ok to join")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        joinConversation(conversation);
                    }
                });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private void joinConversation(Conversation conversation) {
        conversation.join(new JoinListener() {
            @Override
            public void onConversationJoined(Conversation conversation, Member member) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("CONVERSATION_ID", conversation.getConversationId());
                startActivity(intent);
            }

            @Override
            public void onError(int errCode, String errMessage) {
                Log.d(TAG, "onConversationJoined onError: " + errMessage + " / " + errCode);
            }
        });
    }

}
