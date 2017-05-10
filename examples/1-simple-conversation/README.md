## Getting Started with the Nexmo Conversation Android SDK

The Nexmo Conversation API enables you to build conversation features where communication can take place across multiple mediums including IP Messaging, PSTN Voice, SMS and WebRTC Audio and Video. The context of the conversations is maintained though each communication event taking place within a conversation, no matter the medium.

In this getting started guide we'll demonstrate how to build a simple conversation app with IP messaging using the Nexmo Conversation Android SDK. In doing so we'll touch on concepts such as Nexmo Applications, JWTs, Users, Conversations and conversation Members.

### Before you being

* Ensure you have Node.JS and NPM installed (you'll need it for the CLI)
* Ensure you have Android Studio installed
* Create a free Nexmo account - [signup](https://dashboard.nexmo.com)

### Setup

Install the Nexmo CLI if you haven't already done so:

```bash
$ npm install -g nexmo-cli
```

Setup the CLI to use your Nexmo API Key and API Secret. You can get these from the [setting page](https://dashboard.nexmo.com/settings) in the Nexmo Dashboard.

```bash
$ nexmo setup api_key api_secret
```

Create an application within the Nexmo platform.

```bash
$ nexmo app:create "My Convo App" http://your-domain.com/answer http://your-domain.com/event --type=rtc --keyfile=private.key
```

Nexmo Applications contain configuration for the application that you are building. The output of the above command will be something like this:

```bash
Application created: 2c59f277-5a88-4fab-88c4-919ee28xxxxx
Private Key saved to: private.key
```

The first item is the Application Id and the second is a private key that is used generate JWTs that are used to authenticate your interactions with Nexmo.

Create a JWT using your Application Id.

```bash
$ APP_JWT="$(nexmo jwt:generate ./private.key application_id=YOUR_APP_ID)"
```

*Note: The above command saves the generated JWT to a `APP_JWT` variable.*

Create a user who will participate within the conversation.

```bash
curl -X POST https://api.nexmo.com/beta/users\
  -H 'Authorization: Bearer '$APP_JWT \
  -H 'Content-Type:application/json' \
  -d '{"name":"jamie"}'
```

Generate a JWT for the user and take a note of it.

```bash
nexmo jwt:generate ./private.key sub=jamie application_id=YOUR_APP_ID
```

### Create the Android App

Open Android Studio and start a new project. We'll name it "Android Quickstart 1". The minimum SDK will be set to API 19. We can start with an empty activity named "Main Activity".

In the `build.gradle` file we'll add the Nexmo Conversation Android SDK

```groovy
//app/build.gradle
dependencies {
...
  compile 'com.nexmo:conversation:0.2.0'
...
}
```

Then sync your project.

Before we change our activity, we're going to set up a custom application to share a reference to the `ConversationClient` across activities.

```java
// ConversationClientApplication.java
public class ConversationClientApplication extends Application {
    private ConversationClient conversationClient;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            this.conversationClient = new ConversationClient.ConversationClientBuilder().context(this).build();
        } catch (ConversationClientException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ConversationClientApplication builder error!", Toast.LENGTH_LONG).show();
        }
    }

    public ConversationClient getConversationClient() {
        return this.conversationClient;
    }
}
```

Make sure you also add `android:name=".ConversationClientApplication"` to the `application` tag in your `AndroidManifest.xml`

```xml
<!--AndroidManifest.xml-->
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.chris_guzman.androidquickstart1">

    <application
        ...
        android:name=".ConversationClientApplication">
    </application>

</manifest>
```

We're going to create a simple layout for the first activity in our app. There will be a button for the user to log in and then a button for the user to begin a new conversation.

```xml
<!--activity_main.xml-->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical">

    <Button
        android:id="@+id/user_login_btn"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Login"/>

    <Button
        android:id="@+id/create_convo_btn"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Create Conversation" />

    <Button
        android:id="@+id/join_convo_btn"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Join Conversation"/>

</LinearLayout>
```

Now we need to wire up the buttons in `MainActivity.java` Don't forget to replace `userJwt` with the JWT generated from the Nexmo CLI with this command `nexmo jwt:generate ./private.key sub=jamie application_id=YOUR_APP_ID`.

```java
//MainActivity.java
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CAPI-DEMO";
    String userJwt = USER_JWT_GENERATED_FROM_CLI;

    private Button loginBtn;
    private Button createConvoBtn;
    private Button joinConvoBtn;

    private ConversationClient conversationClient;
    private Conversation convo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConversationClientApplication application = (ConversationClientApplication) getApplication();
        conversationClient = application.getConversationClient();

        loginBtn = (Button) findViewById(R.id.user_login_btn);
        createConvoBtn = (Button) findViewById(R.id.create_convo_btn);
        joinConvoBtn = (Button) findViewById(R.id.join_convo_btn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(v);
            }
        });

        createConvoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createConversation(v.getContext());
            }
        });


        joinConvoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinConversation(v.getContext());
            }
        });
    }

    private void logAndShow(final Context context, final String message) {
        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
```

We're creating an instance of `ConversationClient` and saving it as a member variable in the activity.

First let's log a user in

```java
//MainActivity.java
private void loginUser(View v) {
        final Context context = v.getContext();
        conversationClient.login(userJwt, new LoginListener() {
            @Override
            public void onLogin(User user) {
                logAndShow(context, user.getName() + " logged in!");
            }

            @Override
            public void onUserAlreadyLoggedIn(User user) {
                logAndShow(context, "Silly " + user.getName() + " you're already logged in!");
            }

            @Override
            public void onTokenInvalid() {
                logAndShow(context, "Error token invalid. Generate new token");
            }

            @Override
            public void onTokenExpired() {
                logAndShow(context, "Error token expired. Generate new token");
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow(context, "On Login Error. Code" + errCode + "Message: " + errMessage);
            }
        });
}
```

To log in a user, you simply need to call `login` on the `conversationClient` passing in the user JWT and a `LoginListener` as arguments.

The `LoginListener` gives you multiple callbacks, once a user is logged in, or the ConversationClient knows that that user is already logged in. It also gives you three error callbacks: `onTokenInvalid` `onTokenExpired` and a general `onError` callback.

After the user logs in, they'll press the "Create Conversation" which will create a new conversation

```java
//MainActivity.java
private void createConversation(final Context context) {
    conversationClient.newConversation(new Date().toString(), new ConversationCreateListener() {
        @Override
        public void onConversationCreated(Conversation conversation) {
            logAndShow(context, "Conversation created: " + conversation.getName() + " / " + conversation.getConversationId());
            convo = conversation;
        }

        @Override
        public void onError(int errCode, String errMessage) {
            logAndShow(context, "onConversationCreated Error. Code " + errCode + " Message: " + errMessage);
        }
    });
}
```

Starting a new conversation is as easy as calling `newConversation()` on an instance of `ConversationClient`. `newConversation()` takes 2 arguments, the conversation name and a `ConversationCreateListener`. We're using `new Date().toString()` as the conversation name so that you'll have a new conversation every time you run the app.
`ConversationCreateListener` gives you a success callback of `onConversationCreated()` and an error callback of `onError()` if creating the conversation failed.

When the conversation is successfully created, we'll set our `Conversation` member variable of `convo` equal to the `conversation` object that comes back from the `onConversationCreated()` callback. Then we'll join the conversation and add the appropriate listeners.

```java
//MainActivity.java
private void joinConversation(final Context context) {
        convo.join(new JoinListener() {
            @Override
            public void onConversationJoined(Conversation conversation, Member member) {
                logAndShow(context, member.getName() + " has joined " + conversation.getConversationId());
                goToChatActivity(context);
            }

            @Override
            public void onError(int errCode, String errMessage) {
                logAndShow(context, "onConversationJoined Error. Code " + errCode + " Message: " + errMessage);
            }
        });
    }
```
Joining a conversation is as simple as calling `join()` on a `Conversation` and passing in a `JoinListener` as an argument. We'll get two callbacks, `onError` and `onConversationJoined`. For the sample app when a user successfully joins a conversation, we'll log it out and pop a toast. Then we'll start the `ChatActivity` so the user can begin chatting.

```java
//MainActivity.java
private void goToChatActivity(Context context) {
    //TODO: Make ChatActivity
    Intent intent = new Intent(context, ChatActivity.class);
    intent.putExtra("CONVERSATION-ID", convo.getConversationId());
    startActivity(intent);
}
```

When we construct the intent for `ChatActivity` we'll pass the conversation's id so that the new activity can look up which conversation to join.

# Sending messages in a conversation

We'll make a `ChatActivity` with this as the layout

```xml
<!--activity_chat.xml-->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <TextView
                android:id="@+id/chat_txt"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                tools:text="Here's a chat \nThere's a chat \nEverywhere's a chat-chat"/>

        </LinearLayout>

    </ScrollView>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_gravity="bottom">

        <EditText
            android:id="@+id/msg_edit_txt"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:imeOptions="actionSend"
            android:hint="Type a message"/>

        <Button
            android:id="@+id/send_msg_btn"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Send"/>

    </LinearLayout>

</LinearLayout>
```

Like last time we'll wire up the views in `ChatActivity.java` We also need to grab the `conversationId` from the incoming intent.

```java
//ChatActivity.java
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "CAPI-DEMO";

    private TextView chatTxt;
    private EditText msgEditTxt;
    private Button sendMsgBtn;

    private ConversationClient conversationClient;
    private Conversation convo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatTxt = (TextView) findViewById(R.id.chat_txt);
        msgEditTxt = (EditText) findViewById(R.id.msg_edit_txt);
        sendMsgBtn = (Button) findViewById(R.id.send_msg_btn);
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });

        ConversationClientApplication application = (ConversationClientApplication) getApplication();
        conversationClient = application.getConversationClient();

        Intent intent = getIntent();
        String conversationId = intent.getStringExtra("CONVERSATION-ID");
        convo = conversationClient.getConversation(conversationId);
        addListener();
    }

    private void logAndShow(final Context context, final String message) {
        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

```

To send a message we simply need to call `sendText` on our instance of `Conversation convo`. `sendText` takes two arguments, a `String message`, and an `EventSendListener`
In the `EventSendListener` we'll get two call backs: `onSent()` and `onError()`. If there's an error we'll just show an error in the logs and in a toast. We'll ignore the `onSent()` callback since we'll handle messages as they're received instead of as they're sent.

```java
//ChatActivity.java
private void sendMessage(final View v) {
    convo.sendText(msgEditTxt.getText().toString(), new EventSendListener() {
        @Override
        public void onSent(Conversation conversation, Message message) {
            //intentionally left blank
        }

        @Override
        public void onError(int errCode, String errMessage) {
            logAndShow(v.getContext(), "onMessageSent Error. Code " + errCode + " Message: " + errMessage);
        }
    });
}
```

We want to know when text messages are being received so we need to add a `TextListener` to the conversation. We can do this like so:

```java
//ChatActivity.java
private void addListener() {
    if (convo != null) {
        convo.addTextListener(new TextListener() {
            @Override
            public void onTextReceived(Conversation conversation, Text message) {
                showMessage(message);
            }

            @Override
            public void onTextDeleted(Conversation conversation, Text message, Member member) {
                //intentionally left blank
            }
        });
    } else {
        logAndShow(this, "Error adding TextListener: convo is null");
    }
}

private void showMessage(final Text message) {
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            msgEditTxt.setText(null);
            String prevText = chatTxt.getText().toString();
            chatTxt.setText(prevText + "\n" + message.getPayload());
        }
    });
}
```

Calling `addTextListener` on a Conversation allows us to add callbacks when a text is sent. The `addTextListener` takes a `TextListener()` as an argument.
The `TextListener` has a `onTextReceived` and a `onTextDeleted` callback. We'll just focus on the `onTextReceived` callback. When that is fired we'll call our `showMessage()` method.

We'll also check if `convo != null` so that we don't try to attach a `TextListener` to a `null` conversation. `convo` might be null if `conversationClient` fails to find the conversation.

`showMessage()` removes the text from the `msgEditTxt` and appends the text from the `message` to our `chatTxt` along with any previous messages.

# Trying it out

After this you should be able to run the app and send messages to a conversation like so:

![Hello world!](http://g.recordit.co/4eJj5hW5ZM.gif)
