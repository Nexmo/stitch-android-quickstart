## Using more Event Listeners with the Nexmo Conversation Android SDK

In this getting started guide we'll demonstrate how to show previous history of a Conversation we created in the [simple conversation](1-simple-conversation.md) getting started guide. From there we'll cover how to show when a member is typing and mark text as being seen.

## Concepts

This guide will introduce you to **Conversation Events**. We'll be attaching the `MarkedAsSeenListener` and `SeenReceiptListener` listeners to a Conversation, after you are a Member.


### Before you begin


* Ensure you have run through the [the first](1-simple-conversation.md) [and second](2-inviting-members.md) quickstarts.
* Make sure you have two Android devices to complete this example. They can be two emulators, one emulator and one physical device, or two physical devices.

## 1 - Setup

For this quickstart you won't need to emulate any server side events with curl. You'll just need to be able to login as both users created in quickstarts 1 and 2.

If you're continuing on from the previous guide you may already have a `APP_JWT`. If not, generate a JWT using your Application ID (`YOUR_APP_ID`).

```bash
$ APP_JWT="$(nexmo jwt:generate ./private.key application_id=YOUR_APP_ID exp=$(($(date +%s)+86400)))"
```

You may also need to regenerate the users JWTs. See quickstarts 1 and 2 for how to do so.

## 2 Update the Android App

We will use the application we already created for quickstarts 1 and 2. With the basic setup in place we can now focus on updating the client-side application. We can leave the LoginActivity as is. For this demo, we'll solely focus on the ChatActivity.

### 2.1 Updating the ChatActivity layout

We're going to be adding some new elements to our chat app so let's update our layout to reflect them. The updated layout should look like so:

```xml
<!--activity_chat.xml-->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context="com.chris_guzman.a3usingevents.ChatActivity">

    <android.support.v7.widget.RecyclerView
        android:id="@+id/recycler"
        android:layout_marginLeft="8dp"
        android:layout_marginRight="8dp"
        android:layout_marginBottom="16dp"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <TextView
        android:id="@+id/typing_notification"
        android:layout_gravity="center"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        tools:text="Someone is typing"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <EditText
            android:id="@+id/chat_box"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:inputType="textAutoComplete"
            tools:text="This is a sample" />

        <ImageButton
            android:id="@+id/send_btn"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:background="@null"
            android:layout_gravity="center"
            android:src="@drawable/ic_send_black_24dp"/>

    </LinearLayout>

</LinearLayout>
```

Notice that we've added the RecyclerView as well as a TextView with the id `typing_notification`. We'll load the messages in the RecyclerView and show a message in the `typing_notification` TextView when a user is typing.


### 2.2 Adding the new UI to the ChatActivity

In the previous examples we showed messages by adding to a TextView. For this example we'll show you how to use the Conversation SDK in a RecyclerView. Let's add our new UI elements to the ChatActivity:

```java
//ChatActivity.java
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
        chatAdapter = new ChatAdapter(texts);
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

}
```

### 2.3 Creating the ChatAdapter and ViewHolder

Our RecyclerView will need a Adapter and ViewHolder. We can use this:

```java
//ChatAdapter.java
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final String TAG = "ChatAdapter";
    private List<Text> messages;

    public ChatAdapter(List<Text> texts) {
        messages = texts;
        this.self = self;
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.chat_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChatAdapter.ViewHolder holder, int position) {
        Text textMessage = messages.get(position);
        if (textMessage.getType().equals(EventType.TEXT)) {
            holder.text.setText(textMessage.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView text;
        private final ImageView seenIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_chat_txt);
            seenIcon = (ImageView) itemView.findViewById(R.id.item_chat_seen_img);
        }
    }
}
```

We'll also need to create a layout for the ViewHolder. Our layout will have a textview to hold the message text. The layout will also have a check mark image that we can make visible or set the visibility to `gone` depending on if the other users of the chat have seen the message or not. The layout will look like so:

```xml
<!-- layout/chat_item.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:orientation="horizontal"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">

    <TextView
        android:id="@+id/item_chat_txt"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        tools:text="Hello World!"/>

    <View
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_weight="1"
        />

    <ImageView
        android:id="@+id/item_chat_seen_img"
        android:layout_gravity="center"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/ic_done_all_black_24dp"
        android:visibility="gone"/>

</LinearLayout>
```

### 2.4 - Show chat history

When `onResume` fires we'll get the messages in the conversation and update the adapter with those messages. We'll do this in `onResume` instead of `onCreate` so we can fetch the history and any missing messages if we leave the app and come back.

```java
// ChatActivity.java
@Override
    protected void onResume() {
        super.onResume();
        attachListener();
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
```

We'll also need to attach the EventListener. We'll do so in `attachListener()`

```java
//ChatActivity.java
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
}
```

And since we're attaching the listeners we'll need to remove them as well. Let's do that in the `onPause` part of the lifecycle.

```java
//ChatActivity.java
@Override
protected void onPause() {
    super.onPause();
    conversation.removeEventListener(eventListener);
}
```

### 2.5 - Adding Typing and Seen Listeners

We can add other listeners just like we added our EventListener. The TypingSendListener is used to indicate when a user is currently typing or not. The MemberTypingListener is used to listen to typing events sent by the TypingSendListener. Finally, the SeenReceiptListener will be used to mark our messages as read. We'll add theses listeners to our `attachListeners()` method, remembering to detach them in `onPause()`. We'll make the listeners member variables in the Activity so their easy to manage:

```java
//ChatActivity.java
public class ChatActivity extends AppCompatActivity {
    ...
    private ConversationClient conversationClient;
    private Conversation conversation;
    private EventListener eventListener;
    private MemberTypingListener memberTypingListener;
    private TypingSendListener typingSendListener;
    private SeenReceiptListener seenReceiptListener;

    ...

    private void attachListeners() {

    ...

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

    @Override
    protected void onPause() {
        super.onPause();
        conversation.removeEventListener(eventListener);
        conversation.removeTypingListener(memberTypingListener);
        conversation.removeSeenReceiptListener(seenReceiptListener);
    }

}
```

For MemberTypingListener, when `onTyping` fires, we're receiving a typing event. Typing events can either be on or off. If the typing is on then we want to show the member name of who is typing. If the event tells us that the typing indicator is off, then we'll set the typingNotificationTxt to null. We can tell the Conversation SDK when a member is typing using Android's `addTextChangedListener`. We'll attach TextWatcher to the `chatBox`. In the `afterTextChanged` callback we'll look at the length of the text in the EditText. If the text is greater than 0, we know that the user is still typing. The length of the string in the edit text will be 0 when we call `chatBox.setText(null);` in the `sendMessage()` method. Finally we'll add the `SeenReceiptListener` so the SDK knows to mark messages as read as we send them.


### 2.6 - Marking Text messages as seen

We'll only want to mark our messages as read when the other user has seen the message. If the user has the app in the background, we'll want to wait until they bring the app to the foreground and they have seen the text message in the RecyclerView in the `ChatActivity`. To do so, we'll need to mark messages as seen in the `ChatAdapter.` Let's make the following changes to the `ChatAdapter`

```java
// ChatAdapter.java
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final String TAG = "ChatAdapter";
    private List<Text> messages;
    private Member self;
    private MarkedAsSeenListener markedAsSeenListener = new MarkedAsSeenListener() {
        @Override
        public void onMarkedAsSeen() {
        }

        @Override
        public void onError(int errCode, String errMessage) {
            Log.d(TAG, "Error onMarkedAsSeen: " + errMessage);
        }
    };

    public ChatAdapter(List<Text> texts, Member self) {
        messages = texts;
        this.self = self;
    }

    ...

    @Override
    public void onBindViewHolder(ChatAdapter.ViewHolder holder, int position) {
        Text textMessage = messages.get(position);
        if (!textMessage.getMember().equals(self)) {
            textMessage.markAsSeen(markedAsSeenListener);
        }
        if (textMessage.getType().equals(EventType.TEXT)) {
            holder.text.setText(textMessage.getText());
            if (!textMessage.getSeenReceipts().isEmpty()) {
                holder.seenIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    ...
}
```

As you can see we've added two new member variables to our ChatAdapter `Member self` & `MarkedAsSeenListener markedAsSeenListener`. We've also added `Member self` to our constructor.

We've also made some changes to the `onBindViewHolder` method. The first is that we only want to mark a message as read if the sender of the message is not our `self`. That's why `!textMessage.getMember().equals(self)` is there. Then, we only want to show the `seenIcon` if the message has been marked as read. That's what `!textMessage.getSeenReceipts().isEmpty()` is for.

We'll need to update our references to the `ChatAdapter` in `ChatActivity` so let's do so.

```java
//ChatActivity.java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ...
    chatAdapter = new ChatAdapter(texts, conversation.getSelf());
    ...
}
```

# Trying it out

Run the apps on both of your emulators. On one of them, login with the username "jamie". On the other emulator login with the username "alice"
Once you've completed this quickstart, you can run the sample app on two different devices. You'll be able to login as a user, join an existing conversation, chat with users, show a typing indicator, and mark messages as read. Here's a gif of our quickstart in action.

![Awesome Chat](http://g.recordit.co/hfTUzwQYNH.gif)
