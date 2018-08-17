package com.nexmo.push_notifications.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.User;
import com.nexmo.sdk.conversation.client.event.NexmoAPIError;
import com.nexmo.sdk.conversation.client.event.RequestHandler;
import com.nexmo.push_notifications.R;
import com.nexmo.push_notifications.utils.Stitch;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends BaseActivity {
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

        conversationClient = Stitch.getInstance(this.getApplicationContext()).getClient();

        loginTxt = findViewById(R.id.login_text);
        loginBtn = findViewById(R.id.login);
        chatBtn = findViewById(R.id.chat);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userToken = authenticate();
                login(userToken);
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChatDialog();
            }
        });
    }

    private void showChatDialog() {
        new AlertDialog.Builder(this)
                .setTitle("New or existing conversation?")
                .setPositiveButton("Existing Conversation", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        retrieveConversations();
                    }
                })
                .setNegativeButton("Create Conversation", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createConversation();
                    }
                }).show();
    }


    private String authenticate() {
        //stubbing out login flow
        //in a production application you'd make a network request to your server for a short lived JWT
        return USER_JWT;
    }

    private void login(String token) {
        loginTxt.setText("Logging in...");
        conversationClient.login(token, new RequestHandler<User>() {
            @Override
            public void onError(final NexmoAPIError apiError) {
                loginTxt.setText("Login Error: " + apiError.getMessage());
                logAndShow("Login Error: " + apiError.getMessage());
            }

            @Override
            public void onSuccess(User user) {
                loginTxt.setText("Logged in as " + user.getName() + "\nStart chatting!");
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

    private void createConversation() {
        final EditText input = new EditText(LoginActivity.this);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this)
                .setTitle("Enter your conversation name")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        conversationClient.newConversation(true, input.getText().toString(), new RequestHandler<Conversation>() {
                            @Override
                            public void onError(NexmoAPIError apiError) {
                                logAndShow(apiError.getMessage());
                            }

                            @Override
                            public void onSuccess(Conversation conversation) {
                                goToConversation(conversation);
                            }
                        });
                    }
                });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        dialog.setView(input);
        dialog.show();
    }


}