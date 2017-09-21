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
import com.nexmo.sdk.conversation.client.Member;
import com.nexmo.sdk.conversation.client.SeenReceipt;
import com.nexmo.sdk.conversation.client.Subscription;
import com.nexmo.sdk.conversation.client.Text;
import com.nexmo.sdk.conversation.client.event.NexmoAPIError;
import com.nexmo.sdk.conversation.client.event.RequestHandler;
import com.nexmo.sdk.conversation.client.event.ResultListener;
import com.nexmo.sdk.conversation.client.event.container.Receipt;
import com.nexmo.sdk.conversation.core.SubscriptionList;

import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends AppCompatActivity {
    private String TAG = ChatActivity.class.getSimpleName();

    private EditText chatBox;
    private TextView typingNotificationTxt;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Text> texts = new ArrayList<>();

    private Conversation conversation;
    private RequestHandler<Member.TYPING_INDICATOR> typingSendListener;
    private SubscriptionList subscriptions = new SubscriptionList();
    private Subscription<Event> eventSubscription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ConversationClient conversationClient = ((ConversationClientApplication) getApplication()).getConversationClient();
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
        ImageButton sendBtn = (ImageButton) findViewById(R.id.send_btn);
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
        conversation.updateEvents(null, null, new RequestHandler<Conversation>() {
            @Override
            public void onSuccess(Conversation result) {
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
            public void onError(NexmoAPIError apiError) {
                logAndShow("Error Updating Conversation: " + apiError.getMessage());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.unsubscribeAll();
        eventSubscription.unsubscribe();
    }

    private void attachListeners() {
        ResultListener<Event> eventListener = new ResultListener<Event>() {
            @Override
            public void onSuccess(final Event message) {
                logAndShow("onNewMessage of type " + message.getType());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        texts.add(((Text) message));
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
                    }
                });
            }
        };

        eventSubscription = conversation.messageEvent().add(eventListener);

//        conversation.messageEvent().add(eventListener).addTo(subscriptions);

        conversation.typingEvent().add(new ResultListener<Member>() {
            @Override
            public void onSuccess(final Member member) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String typingMsg = member.getTypingIndicator().equals(Member.TYPING_INDICATOR.ON) ? member.getName() + " is typing" : null;
                        typingNotificationTxt.setText(typingMsg);
                    }
                });
            }
        }).addTo(subscriptions);

        typingSendListener = new RequestHandler<Member.TYPING_INDICATOR>() {
            @Override
            public void onSuccess(Member.TYPING_INDICATOR result) {

            }

            @Override
            public void onError(NexmoAPIError apiError) {

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

        conversation.seenEvent().add(new ResultListener<Receipt<SeenReceipt>>() {
            @Override
            public void onSuccess(Receipt<SeenReceipt> result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).addTo(subscriptions);


    }

    private void sendMessage() {
        conversation.sendText(chatBox.getText().toString(), new RequestHandler<Event>() {
            @Override
            public void onSuccess(Event result) {
                logAndShow("Sending " + ((Text) result).getText());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatBox.setText(null);
                    }
                });
            }

            @Override
            public void onError(NexmoAPIError apiError) {
                logAndShow("Error sending message: " + apiError.getMessage());
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
