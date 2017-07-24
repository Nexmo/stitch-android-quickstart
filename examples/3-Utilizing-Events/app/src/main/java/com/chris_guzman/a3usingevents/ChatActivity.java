package com.chris_guzman.a3usingevents;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.Event;
import com.nexmo.sdk.conversation.client.Image;
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.SeenReceipt;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.ConversationListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.EventSendListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.MemberTypingListener;
import com.nexmo.sdk.conversation.client.event.CompletionListeners.TypingSendListener;
import com.nexmo.sdk.conversation.client.event.EventListener;
import com.nexmo.sdk.conversation.client.event.SeenReceiptListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private String TAG = ChatActivity.class.getSimpleName();

    private EditText chatBox;
    private ImageButton sendBtn;
    private TextView typingNotificationTxt;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Text> texts = new ArrayList<>();

    private ConversationClient conversationClient;
    private Conversation conversation;
    private EventListener eventListener;
    private MemberTypingListener memberTypingListener;
    private TypingSendListener typingSendListener;
    private SeenReceiptListener seenReceiptListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        conversationClient = ((ConversationClientApplication) getApplication()).getConversationClient();
        Intent intent = getIntent();
        String conversationId = intent.getStringExtra("CONVERSATION_ID");
        conversation = conversationClient.getConversation(conversationId);

        texts = conversation.getTexts();
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        chatAdapter = new ChatAdapter(texts, conversation.getSelf());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        chatBox = (EditText) findViewById(R.id.chat_box);
        sendBtn = (ImageButton) findViewById(R.id.send_btn);
        typingNotificationTxt = (TextView) findViewById(R.id.typing_notification);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachListeners();
        conversation.updateEvents(null, null, new ConversationListener() {
            @Override
            public void onConversationUpdated(final Conversation conversation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        texts.clear();
                        texts.addAll(conversation.getTexts());
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
                    }
                });
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("Error Updating Conversation: " + errMessage);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        conversation.removeEventListener(eventListener);
        conversation.removeTypingListener(memberTypingListener);
        conversation.removeSeenReceiptListener(seenReceiptListener);
    }

    private void attachListeners() {
        eventListener = new EventListener() {
            @Override
            public void onTextReceived(final Text message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        texts.add(message);
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

        conversation.addEventListener(eventListener);

        memberTypingListener = new MemberTypingListener() {
            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow("onTyping onError: " + errMessage);
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

        conversation.addTypingListener(memberTypingListener);

        typingSendListener = new TypingSendListener() {
            @Override
            public void onTypingSent(Member.TYPING_INDICATOR typingIndicator) {
            }

            @Override
            public void onError(int errCode, String errMessage) {
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
                    conversation.startTyping(typingSendListener);
                } else {
                    conversation.stopTyping(typingSendListener);
                }
            }
        });

        seenReceiptListener = new SeenReceiptListener() {
            @Override
            public void onTextSeen(Text text, Member member, SeenReceipt seenReceipt) {
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

        conversation.addSeenReceiptListener(seenReceiptListener);
    }

    private void sendMessage() {
        conversation.sendText(chatBox.getText().toString(), new EventSendListener() {
            @Override
            public void onSent(Event event) {
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
