## Getting Started with the Nexmo Conversation Android SDK

The Nexmo Conversation API enables you to build conversation features where communication can take place across multiple mediums including IP Messaging, PSTN Voice, SMS and WebRTC Audio and Video. The context of the conversations is maintained though each communication event taking place within a conversation, no matter the medium.

In this getting started guide we'll demonstrate how to build a simple conversation app with IP messaging using the Nexmo Conversation Android SDK. In doing so we'll touch on concepts such as Nexmo Applications, JWTs, Users, Conversations and conversation Members.

### Before you begin

* Ensure you have Node.JS and NPM installed (you'll need it for the CLI)
* Ensure you have Android Studio installed
* Create a free Nexmo account - [signup](https://dashboard.nexmo.com)

### Setup

Install the beta version of the Nexmo CLI if you haven't already done so:

```bash
$ npm install -g nexmo-cli@beta
```

Setup the CLI to use your Nexmo API Key and API Secret. You can get these from the [setting page](https://dashboard.nexmo.com/settings) in the Nexmo Dashboard.

```bash
$ nexmo setup api_key api_secret
```

Create an application within the Nexmo platform.

```bash
$ nexmo app:create "Conversation Android App" http://example.com/answer http://example.com/event --type=rtc --keyfile=private.key
```

Nexmo Applications contain configuration for the application that you are building. The output of the above command will be something like this:

```bash
Application created: 2c59f277-5a88-4fab-88c4-919ee28xxxxx
Private Key saved to: private.key
```

The first item is the Application ID and the second is a private key that is used generate JWTs that are used to authenticate your interactions with Nexmo.

Create a JWT using your Application ID.

```bash
$ APP_JWT="$(nexmo jwt:generate ./private.key application_id=YOUR_APP_ID)"
```

*Note: The above command saves the generated JWT to a `APP_JWT` variable.*

Create a user who will participate within the conversation.

```bash
curl -X POST https://api.nexmo.com/beta/users\
  -H 'Authorization: Bearer '$APP_JWT \
  -H 'Content-Type:application/json' \
  -d '{"name":"adam"}'
```

Generate a JWT for the user and take a note of it.

```bash
nexmo jwt:generate ./private.key sub=adam acl='{"paths": { "/**": {  } } }' application_id=YOUR_APP_ID
```

### Create the Android App

Open Android Studio and start a new project. We'll name it "Conversation Android Quickstart 1". The minimum SDK will be set to API 19. We can start with an empty activity named "Login Activity".

In the `build.gradle` file we'll add the Nexmo Conversation Android SDK.

```groovy
//app/build.gradle
dependencies {
...
  compile 'com.nexmo:conversation:0.6.1'
  compile 'com.android.support:appcompat-v7:25.3.1'
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
        this.conversationClient = new ConversationClient.ConversationClientBuilder().context(this).build();
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

We're going to create a simple layout for the first activity in our app. There will be buttons for the user to log in and begin a new conversation.

```xml
<!--activity_login.xml-->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <TextView
        android:id="@+id/login_text"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:gravity="center"
        android:text="Welcome to Awesome Chat. Login to continue" />

    <LinearLayout
        android:id="@+id/login_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:orientation="horizontal">

        <Button
            android:id="@+id/login"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Login"/>

        <View
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"/>


        <Button
            android:id="@+id/chat"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Chat"/>

    </LinearLayout>

</LinearLayout>
```

Now we need to wire up the buttons in `LoginActivity.java` Don't forget to replace `userJwt` with the JWT generated from the Nexmo CLI with this command `nexmo jwt:generate ./private.key sub=adam acl='{"paths": { "/**": {  } } }' application_id=YOUR_APP_ID`.

```java
//LoginActivity.java
public class LoginActivity extends AppCompatActivity {
    private final String TAG = LoginActivity.this.getClass().getSimpleName();
    String userJwt = USER_JWT_GENERATED_FROM_CLI;

    private ConversationClient conversationClient;
    private TextView loginTxt;
    private Button loginBtn;
    private Button chatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ConversationClientApplication application = (ConversationClientApplication) getApplication();
        conversationClient = application.getConversationClient();

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
                createConversation();
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
```

We're creating an instance of `ConversationClient` and saving it as a member variable in the activity.

First let's log a user in

```java
//LoginActivity.java
private void login() {
    loginTxt.setText("Logging in...");
    conversationClient.login(userJwt, new LoginListener() {
        @Override
        public void onLogin(final User user) {
            showLoginSuccess(user);
        }

        @Override
        public void onUserAlreadyLoggedIn(User user) {
            showLoginSuccess(user);
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
        public void onError(int errCode, String errMessage) {
            logAndShow("Login Error: " + errMessage);
        }
    });
}

private void showLoginSuccess(final User user) {
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            loginTxt.setText("Logged in as " + user.getName() + "\nStart a new conversation");
        }
    });
}
```

To log in a user, you simply need to call `login` on the `conversationClient` passing in the user JWT and a `LoginListener` as arguments.

The `LoginListener` gives you multiple callbacks, once a user is logged in, or the ConversationClient knows that that user is already logged in. It also gives you three error callbacks: `onTokenInvalid` `onTokenExpired` and a general `onError` callback.

After the user logs in, they'll press the "Chat" button which will create a new conversation and have the user join that conversation.

```java
//LoginActivity.java
private void createConversation() {
    conversationClient.newConversation(new Date().toString(), new ConversationCreateListener() {
        @Override
        public void onConversationCreated(Conversation conversation) {
            logAndShow("Conversation created: " + conversation.getDisplayName());
            joinConversation(conversation);
        }

        @Override
        public void onError(int errCode, String errMessage) {
            logAndShow("Error creating conversation: " + errMessage);
        }
    });
}
```

Starting a new conversation is as easy as calling `newConversation()` on an instance of `ConversationClient`. `newConversation()` takes 2 arguments, the conversation name and a `ConversationCreateListener`. We're using `new Date().toString()` as the conversation name so that you'll have a new conversation every time you click the "Chat" button.
`ConversationCreateListener` gives you a success callback of `onConversationCreated()` and an error callback of `onError()` if creating the conversation failed.

After the conversation is successfully created, we'll join the conversation we just created.

```java
//LoginActivity.java
private void joinConversation(final Conversation conversation) {
    conversation.join(new JoinListener() {
        @Override
        public void onConversationJoined(Member member) {
            goToChatActivity(conversation);
        }

        @Override
        public void onError(int errCode, String errMessage) {
            logAndShow("Error joining conversation: " + errMessage);
        }
    });
}
```
Joining a conversation is as simple as calling `join()` on a `Conversation` and passing in a `JoinListener` as an argument. We'll get two callbacks, `onError` and `onConversationJoined`. For the sample app when a user successfully joins a conversation, we'll log it out and pop a toast. Then we'll start the `ChatActivity` so the user can begin chatting.

```java
//LoginActivity.java
private void goToChatActivity(Conversation conversation) {
    //TODO create ChatActivity
    Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
    intent.putExtra("CONVERSATION-ID", conversation.getConversationId());
    startActivity(intent);
}
```

When we construct the intent for `ChatActivity` we'll pass the conversation's id so that the new activity can look up which conversation to join.

### Sending messages in a conversation

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

Like last time we'll wire up the views in `ChatActivity.java` We also need to grab the `conversationId` from the incoming intent so we can look up the appropriate conversation.

```java
//ChatActivity.java
public class ChatActivity extends AppCompatActivity {
  private final String TAG = ChatActivity.this.getClass().getSimpleName();

  private TextView chatTxt;
  private EditText msgEditTxt;
  private Button sendMsgBtn;

  private ConversationClient conversationClient;
  private Conversation convo;
  private MessageListener messageListener;

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
              sendMessage();
          }
      });

      ConversationClientApplication application = (ConversationClientApplication) getApplication();
      conversationClient = application.getConversationClient();

      Intent intent = getIntent();
      String conversationId = intent.getStringExtra("CONVERSATION-ID");
      convo = conversationClient.getConversation(conversationId);
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

```

To send a message we simply need to call `sendText` on our instance of `Conversation convo`. `sendText` takes two arguments, a `String message`, and an `EventSendListener`
In the `EventSendListener` we'll get two call backs: `onSent()` and `onError()`. If there's an error we'll just show an error in the logs and in a toast. We'll just log out the message in the `onSent()` callback since we'll handle messages as they're received instead of as they're sent. You might notice that I'm checking the type of the message before I log it out. That's because a `Message` can be `Text` or an `Image`. For now we'll just worry about `Text`.

```java
//ChatActivity.java
private void sendMessage() {
    convo.sendText(msgEditTxt.getText().toString(), new EventSendListener() {
        @Override
        public void onSent(Message message) {
            if (message.getType().equals(EventType.TEXT)) {
                Log.d(TAG, "onSent: " + ((Text) message).getText());
            }
        }

        @Override
        public void onError(int errCode, String errMessage) {
            logAndShow("Error sending message: " + errMessage);
        }
    });
}
```

We want to know when text messages are being received so we need to add a `TextListener` to the conversation. We can do this like so:

```java
//ChatActivity.java
private void addListener() {
    messageListener = new MessageListener() {
        @Override
        public void onError(int errCode, String errMessage) {
            logAndShow("Error adding MessageListener: " + errMessage);
        }

        @Override
        public void onImageDownloaded(Image image) {
            //intentionally left blank
        }

        @Override
        public void onTextReceived(Text message) {
            showMessage(message);
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
    };
    convo.addMessageListener(messageListener);
}

private void showMessage(final Text message) {
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            msgEditTxt.setText(null);
            String prevText = chatTxt.getText().toString();
            chatTxt.setText(prevText + "\n" + message.getText());
        }
    });
}
```

Calling `addMessageListener` on a Conversation allows us to add callbacks when a message is sent. The `addMessageListener` method takes a `new MessageListener()` as an argument.
`MessageListener` has a few callbacks, but we'll just focus about the `onTextReceived` and `onError` methods. When that is `onTextReceived` is fired we'll call our `showMessage()` method. If an error occurs, we'll log it out.

`showMessage()` removes the text from the `msgEditTxt` and appends the text from the `message` to our `chatTxt` along with any previous messages.

### Adding and removing listeners

Finally, we need to add the `MessageListener` to the `Conversation` in order to send and receive messages. We should also remove the `MessageListener` when our Activity is winding down.

```java
//ChatActivity.java
@Override
protected void onResume() {
    super.onResume();
    addListener();
}

@Override
protected void onDestroy() {
    super.onDestroy();
    convo.removeMessageListener(messageListener);
}
```

# Trying it out

After this you should be able to run the app and send messages to a conversation like so:

![Hello world!](http://g.recordit.co/uqdFsAOTFE.gif)
