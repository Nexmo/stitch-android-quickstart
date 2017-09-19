## Getting Started with the Nexmo Conversation Android SDK

In this getting started guide we'll demonstrate how to build a simple conversation app with IP messaging using the Nexmo Conversation Android SDK.

## Concepts

This guide will introduce you to the following concepts.

* **Nexmo Applications** - contain configuration for the application that you are building
* **JWTs** ([JSON Web Tokens](https://jwt.io/)) - the Conversation API uses JWTs for authentication. JWTs contain all the information the Nexmo platform needs to authenticate requests. JWTs also contain information such as the associated Applications, Users and permissions.
* **Users** - users who are associated with the Nexmo Application. It's expected that Users will have a one-to-one mapping with your own authentication system.
* **Conversations** - A thread of conversation between two or more Users.
* **Members** - Users that are part of a conversation.

### Before you begin

* Ensure you have Node.JS and NPM installed (you'll need it for the CLI)
* Ensure you have Android Studio installed
* Create a free Nexmo account - [signup](https://dashboard.nexmo.com)
* Install the Nexmo CLI:

    ```bash
    $ npm install -g nexmo-cli@beta
    ```

    Setup the CLI to use your Nexmo API Key and API Secret. You can get these from the [setting page](https://dashboard.nexmo.com/settings) in the Nexmo Dashboard.

    ```bash
    $ nexmo setup api_key api_secret
    ```

## 1 - Setup

_Note: The steps within this section can all be done dynamically via server-side logic. But in order to get the client-side functionality we're going to manually run through setup._

### 1.1 - Create a Nexmo 

Create an application within the Nexmo platform.

```bash
$ nexmo app:create "Conversation Android App" http://example.com/answer http://example.com/event --type=rtc --keyfile=private.key
```

Nexmo Applications contain configuration for the application that you are building. The output of the above command will be something like this:

```bash
Application created: aaaaaaaa-bbbb-cccc-dddd-0123456789ab
Private Key saved to: private.key
```

The first item is the Application ID and the second is a private key that is used generate JWTs that are used to authenticate your interactions with Nexmo. You should take a note of it. We'll refer to this as `YOUR_APP_ID` later. The second value is a private key location. The private key is used to generate JWTs that are used to authenticate your interactions with Nexmo.


### 1.2 - Generate an Application JWT

Generate a JWT using your Application ID (`YOUR_APP_ID`).

```bash
$ APP_JWT="$(nexmo jwt:generate ./private.key exp=$(($(date +%s)+86400)) application_id=YOUR_APP_ID)"
```

*Note: The above command saves the generated JWT to a `APP_JWT` variable. It also sets the expiry of the JWT (`exp`) to one day from now.*

### 1.3 - Create a Conversation

Create a conversation within the application:

```bash
$ curl -X POST https://api.nexmo.com/beta/conversations\
 -H 'Authorization: Bearer '$APP_JWT -H 'Content-Type:application/json' -d '{"name":"nexmo-chat", "display_name": "Nexmo Chat"}'
```

This will result in a JSON response that looks something like the following. Take a note of the `id` attribute as this is the unique identifier for the conversation that has been created. We'll refer to this as `CONVERSATION_ID` later.

```json
{"id":"CON-8cda4c2d-9a7d-42ff-b695-ec4124dfcc38","href":"http://conversation.local/v1/conversations/CON-8cda4c2d-9a7d-42ff-b695-ec4124dfcc38"}
```

### 1.4 - Create a User

Create a user who will participate within the conversation.

```bash
curl -X POST https://api.nexmo.com/beta/users\
  -H 'Authorization: Bearer '$APP_JWT \
  -H 'Content-Type:application/json' \
  -d '{"name":"jamie"}'
```

The output will look as follows:

```json
{"id":"USR-9a88ad39-31e0-4881-b3ba-3b253e457603","href":"http://conversation.local/v1/users/USR-9a88ad39-31e0-4881-b3ba-3b253e457603"}
```

Take a note of the `id` attribute as this is the unique identifier for the user that has been created. We'll refer to this as `USER_ID` later.

### 1.5 - Add the User to the Conversation

Finally, let's add the user to the conversation that we created. Remember to replace `CONVERSATION_ID` and `USER_ID` values.

```bash
$ curl -X POST https://api.nexmo.com/beta/conversations/CONVERSATION_ID/members\
 -H 'Authorization: Bearer '$APP_JWT -H 'Content-Type:application/json' -d '{"action":"join", "user_id":"USER_ID", "channel":{"type":"app"}}'
```

The response to this request will confirm that the user has `JOINED` the "Nexmo Chat" conversation.

```json
{"id":"MEM-fe168bd2-de89-4056-ae9c-ca3d19f9184d","user_id":"USR-f4a27041-744d-46e0-a75d-186ad6cfcfae","state":"JOINED","timestamp":{"joined":"2017-06-17T22:23:41.072Z"},"channel":{"type":"app"},"href":"http://conversation.local/v1/conversations/CON-8cda4c2d-9a7d-42ff-b695-ec4124dfcc38/members/MEM-fe168bd2-de89-4056-ae9c-ca3d19f9184d"}
```

You can also check this by running the following request, replacing `CONVERSATION_ID`:

```bash
$ curl https://api.nexmo.com/beta/conversations/CONVERSATION_ID/members\
 -H 'Authorization: Bearer '$APP_JWT
```

Where you should see a response similar to the following:

```json
[{"user_id":"USR-f4a27041-744d-46e0-a75d-186ad6cfcfae","name":"MEM-fe168bd2-de89-4056-ae9c-ca3d19f9184d","user_name":"jamie","state":"JOINED"}]
```

### 1.6 - Generate a User JWT

Generate a JWT for the user and take a note of it. Remember to change the `YOUR_APP_ID` value in the command.

```bash
$ USER_JWT="$(nexmo jwt:generate ./private.key sub=jamie exp=$(($(date +%s)+86400)) acl='{"paths": {"/v1/sessions/**": {}, "/v1/users/**": {}, "/v1/conversations/**": {}}}' application_id=YOUR_APP_ID)"
```

*Note: The above command saves the generated JWT to a `USER_JWT` variable. It also sets the expiry of the JWT to one day from now.*

You can see the JWT for the user by running the following:

```bash
$ echo $USER_JWT
```

## 2 - Create the Android App

With the basic setup in place we can now focus on the client-side application

### 2.1 Start a new project and add the Nexmo Conversation SDK

Open Android Studio and start a new project. We'll name it "Conversation Android Quickstart 1". The minimum SDK will be set to API 19. We can start with an empty activity named "Login Activity".

In the `build.gradle` file we'll add the Nexmo Conversation Android SDK.

```groovy
//app/build.gradle
dependencies {
...
  compile 'com.nexmo:conversation:0.7.1'
  compile 'com.android.support:appcompat-v7:25.3.1'
...
}
```

Then sync your project.

### 2.2 Add ConversationClient to your app

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

### 2.3 Creating the login layout

We're going to create a simple layout for the first activity in our app. There will be buttons for the user to log in and start chatting.

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
### 2.3 - Create the LoginActivity

We're creating an instance of `ConversationClient` and saving it as a member variable in the activity.

We also need to wire up the buttons in `LoginActivity.java` Don't forget to replace `USER_JWT` with the JWT generated from the Nexmo CLI in [step 1.6](#16---generate-a-user-jwt) and `CONVERSATION_ID` with the id generated in [step 1.3](#13---create-a-conversation)

```java
//LoginActivity.java
public class LoginActivity extends AppCompatActivity {
    private final String TAG = LoginActivity.this.getClass().getSimpleName();
    private String CONVERSATION_ID = YOUR_CONVERSATION_ID;
    private String USER_JWT = YOUR_USER_JWT;

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
                goToChatActivity();
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

### 2.4 Stubbed Out Login

Next, let's stub out the login workflow.

Create an authenticate function that takes a username. For now, stub it out to always return the `USER_JWT` value. Also create a login function that takes a userToken (a JWT).

```java
//LoginActivity.java
private String authenticate() {
    return USER_JWT;
}

private void login() {
    loginTxt.setText("Logging in...");

    String userToken = authenticate();
    conversationClient.login(userToken, new LoginListener() {
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

After the user logs in, they'll press the "Chat" button which will take them to the ChatActivity and let them begin chatting in the conversation we've already created.

### 2.5 Navigate to ChatActivity

When we construct the intent for `ChatActivity` we'll pass the conversation's ID so that the new activity can look up which conversation to join. Remember that `CONVERSATION_ID` comes from the id generated in [step 1.3](#13---create-a-conversation).

```java
//LoginActivity.java
private void goToChatActivity() {
    Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
    intent.putExtra("CONVERSATION-ID", CONVERSATION_ID);
    startActivity(intent);
}
```

### 2.6 Create the Chat layout

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

### 2.7 Create the ChatActivity

Like last time we'll wire up the views in `ChatActivity.java` We also need to grab the `conversationId` from the incoming intent so we can look up the appropriate conversation.

```java
//ChatActivity.java
public class ChatActivity extends AppCompatActivity {
  private final String TAG = ChatActivity.this.getClass().getSimpleName();

  private TextView chatTxt;
  private EditText msgEditTxt;
  private Button sendMsgBtn;

  private ConversationClient conversationClient;
  private Conversation conversation;
  private EventListener eventListener;

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
      conversation = conversationClient.getConversation(conversationId);
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

### 2.8 - Sending `text` Events

To send a message we simply need to call `sendText` on our instance of `Conversation conversation`. `sendText` takes two arguments, a `String message`, and an `EventSendListener`
In the `EventSendListener` we'll get two call backs: `onSent()` and `onError()`. If there's an error we'll just show an error in the logs and in a toast. We'll just log out the message in the `onSent()` callback since we'll handle messages as they're received instead of as they're sent. You might notice that I'm checking the type of the message before I log it out. That's because a `Message` can be `Text` or an `Image`. For now we'll just worry about `Text`.

```java
//ChatActivity.java
private void sendMessage() {
    conversation.sendText(msgEditTxt.getText().toString(), new EventSendListener() {
        @Override
        public void onSent(Event event) {
            if (event.getType().equals(EventType.TEXT)) {
                Log.d(TAG, "onSent: " + ((Text) event).getText());
            }
        }

        @Override
        public void onError(int errCode, String errMessage) {
            logAndShow("Error sending message: " + errMessage);
        }
    });
}
```

### 2.9 - Receiving `text` Events

We want to know when text messages are being received so we need to add a `EventListener` to the conversation. We can do this like so:

```java
//ChatActivity.java
private void addListener() {
    eventListener = new EventListener() {
        @Override
        public void onError(int errCode, String errMessage) {
            logAndShow("Error adding EventListener: " + errMessage);
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
    conversation.addEventListener(eventListener);
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

Calling `addEventListener` on a Conversation allows us to add callbacks when a message is received. The `addEventListener` method takes a `new EventListener()` as an argument.
`EventListener` has a few callbacks, but we'll just focus about the `onTextReceived` and `onError` methods. When that is `onTextReceived` is fired we'll call our `showMessage()` method. If an error occurs, we'll log it out.

`showMessage()` removes the text from the `msgEditTxt` and appends the text from the `message` to our `chatTxt` along with any previous messages.

### 2.10 - Adding and removing listeners

Finally, we need to add the `EventListener` to the `Conversation` in order to send and receive messages. We should also remove the `EventListener` when our Activity is winding down.

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
    conversation.removeEventListener(eventListener);
}
```

## 3.0 - Trying it out

After this you should be able to run the app and send messages to a conversation like so:

![Hello world!](http://g.recordit.co/uqdFsAOTFE.gif)

## Where next?

Try out [Quickstart 2](https://github.com/Nexmo/conversation-android-quickstart/blob/master/docs/2-inviting-members.md)
