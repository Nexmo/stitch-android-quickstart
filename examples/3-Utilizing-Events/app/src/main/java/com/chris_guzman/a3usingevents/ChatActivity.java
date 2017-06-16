package com.chris_guzman.a3usingevents;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.Message;
import com.nexmo.sdk.conversation.client.SeenReceipt;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.EventSendListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.InviteSendListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.MarkedAsSeenListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.MemberTypingListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.TypingSendListener;
import com.nexmo.sdk.conversation.client.event.MessageListener;
import com.nexmo.sdk.conversation.client.event.SeenReceiptListener;

public class ChatActivity extends AppCompatActivity {

    private String TAG = ChatActivity.class.getSimpleName();
    private ConversationClient conversationClient;
    private RecyclerView recyclerView;
    private Conversation convo;
    private ChatAdapter chatAdapter;
    private EditText chatBox;
    private ImageButton sendBtn;
    private MessageListener msgListener;
    private SeenReceiptListener seenReceiptListener;
    private MemberTypingListener memberTypingListener;
    private TextView typingNotificationTxt;
    private TypingSendListener typingSendListener;
    private MarkedAsSeenListener markedAsSeenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        conversationClient = ((ConversationClientApplication) getApplication()).getConversationClient();
        Intent intent = getIntent();
        String conversationId = intent.getStringExtra("CONVERSATION_ID");
        convo = conversationClient.getConversation(conversationId);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        chatAdapter = new ChatAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        convo.updateEvents(null, null, new ConversationListener() {
            @Override
            public void onConversationUpdated(final Conversation conversation) {
                Log.d(TAG, "onConversationUpdated: ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatAdapter.setMessages(conversation.getMessages());
                    }
                });
            }

            @Override
            public void onError(int errCode, String errMessage) {
                Log.d(TAG, "Error Updating Conversation: " + errMessage);
            }
        });

        chatBox = (EditText) findViewById(R.id.chat_box);
        sendBtn = (ImageButton) findViewById(R.id.send_btn);
        typingNotificationTxt = (TextView) findViewById(R.id.typing_notification);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        chatBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || event.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        convo.removeMessageListener(msgListener);
        convo.removeSeenReceiptListener(seenReceiptListener);
        convo.removeTypingListener(memberTypingListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite:
                inviteUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void inviteUser() {
        final String otherUser = (conversationClient.getLoggedInUser().getName().equals("tom") ? "jerry" : "tom");
        convo.invite(otherUser, new InviteSendListener() {
            @Override
            public void onInviteSent(Member invitedMember) {
                logAndShow("Invite sent to: " + invitedMember.getName());
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error sending invite: " + errMessage);
            }
        });
    }

    private void attachListener() {
        markedAsSeenListener = new MarkedAsSeenListener() {
            @Override
            public void onMarkedAsSeen() {
                Log.d(TAG, "onMarkedAsSeen: ");
            }

            @Override
            public void onError(int errCode, String errMessage) {
                Log.d(TAG, "onError onMarkedAsSeen: " + errMessage + " / " + errCode);
            }
        };

        msgListener = new MessageListener() {
            @Override
            public void onTextReceived(final Text message) {
                message.markAsSeen(markedAsSeenListener);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
                    }
                });
            }

            @Override
            public void onTextDeleted(Text message, Member member) {
                //intentionally left blank
            }

            @Override
            public void onImageReceived(Image image) {
                //intentionally left blank
            }

            @Override
            public void onImageDeleted(Image message, Member member) {
                //intentionally left blank
            }

            @Override
            public void onImageDownloaded(Image image) {
                //intentionally left blank
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error receiving message: " + errMessage);
            }
        };

        convo.addMessageListener(msgListener);

        seenReceiptListener = new SeenReceiptListener() {
            @Override
            public void onTextSeen(Text text, Member member, SeenReceipt seenReceipt) {
                Log.d(TAG, "onTextSeen: " + member.getName() + " " + text.getText() + " ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onImageSeen(Image image, Member member, SeenReceipt seenReceipt) {
                //intentionally left blank
            }
        };

        convo.addSeenReceiptListener(seenReceiptListener);

        memberTypingListener = new MemberTypingListener() {
            @Override
            public void onError(int errCode, String errMessage) {
                Log.d(TAG, "onTyping onError: " + errMessage);
            }

            @Override
            public void onTyping(final Member member, final Member.TYPING_INDICATOR typingIndicator) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String typingMsg = typingIndicator.equals(Member.TYPING_INDICATOR.ON) ? member.getName() + " is typing" : null;
                        typingNotificationTxt.setText(typingMsg);
                    }
                });
            }
        };

        convo.addTypingListener(memberTypingListener);

        typingSendListener = new TypingSendListener() {
            @Override
            public void onTypingSent(Member.TYPING_INDICATOR typingIndicator) {
                Log.d(TAG, "onTypingSent: " + typingIndicator);
            }

            @Override
            public void onError(int errCode, String errMessage) {
                Log.d(TAG, "onError: onTypingSent " + errMessage);
            }
        };

        chatBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //intentionally left blank
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    convo.startTyping(typingSendListener);
                } else {
                    convo.stopTyping(typingSendListener);
                }
            }
        });
    }

    private void sendMessage() {
        convo.sendText(chatBox.getText().toString(), new EventSendListener() {
            @Override
            public void onSent(Message message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatBox.setText(null);
                    }
                });
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error sending message: " + errMessage);
            }
        });
    }

    private void logAndShow(final String message) {
        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
