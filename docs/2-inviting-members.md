## Inviting Members the Nexmo Conversation Android SDK

In this getting started guide we'll demonstrate how to login a user, create a conversation, invite another user, and send and receive messages between users. In doing so we'll touch on concepts such as Nexmo Applications, JWTs, Users, Conversations, Members, and Event Listeners.

### Before you begin

* Ensure you have Node.JS and NPM installed (you'll need it for the CLI)
* Ensure you have Android Studio installed
* Create a free Nexmo account - [signup](https://dashboard.nexmo.com)
* Make sure you have two Android devices to complete this example. They can be two emulators, one emulator and one physical device, or two physical devices.

### Setup

We're going to create a new Nexmo app and new users for this demo. This demo assumes that you've completed the Before you begin steps in [Quickstart 1](https://github.com/Nexmo/conversation-android-quickstart/tree/master/examples/1-simple-conversation)

Create a new application within the Nexmo platform.

```bash
$ nexmo app:create "Conversation Android Quickstart 2" http://example.com/answer http://example.com/event --type=rtc --keyfile=private.key
```

Nexmo Applications contain configuration for the application that you are building. The output of the above command will be something like this:

```bash
Application created: 2c59f277-5a88-4fab-88c4-919ee28xxxxx
Private Key saved to: private.key
```

The first item is the Application ID and the second is a private key that is used generate JWTs that are used to authenticate your interactions with Nexmo.

Create a JWT using your Application ID.

```bash
$ APP_JWT="$(nexmo jwt:generate ./private.key application_id=2c59f277-5a88-4fab-88c4-919ee28xxxxx)"
```

*Note: The above command saves the generated JWT to a `APP_JWT` variable.*

Next we're going to create two users who will participate within the conversation.

```bash
curl -X POST https://api.nexmo.com/beta/users\
  -H 'Authorization: Bearer '$APP_JWT \
  -H 'Content-Type:application/json' \
  -d '{"name":"tom"}'
```

```bash
curl -X POST https://api.nexmo.com/beta/users\
  -H 'Authorization: Bearer '$APP_JWT \
  -H 'Content-Type:application/json' \
  -d '{"name":"jerry"}'
```

Now we'll need to generate a JWT for each user and take a note of each.

```bash
nexmo jwt:generate ./private.key sub=tom acl='{"paths": {"/v1/sessions/**": {}, "/v1/users/**": {}, "/v1/conversations/**": {}}}' application_id=2c59f277-5a88-4fab-88c4-919ee28xxxxx
```

```bash
nexmo jwt:generate ./private.key sub=jerry acl='{"paths": {"/v1/sessions/**": {}, "/v1/users/**": {}, "/v1/conversations/**": {}}}' application_id=2c59f277-5a88-4fab-88c4-919ee28xxxxx
```

### Create the Android App

You can build off of our project in Quickstart 1 or you can open Android Studio and start a new project. We'll name it "Conversation Android Quickstart 2". The minimum SDK will be set to API 19. We can start with an empty activity named "Login Activity".

In the `build.gradle` file we'll add the Nexmo Conversation Android SDK.

```groovy
//app/build.gradle
dependencies {
...
  compile 'com.nexmo:conversation:0.6.2'
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

Now we need to wire up the buttons in `LoginActivity.java` Don't forget to replace `tomToken` and `jerryToken` with the JWT generated from the Nexmo CLI:

```bash
$ nexmo jwt:generate ./private.key sub=tom acl='{"paths": {"/v1/sessions/**": {}, "/v1/users/**": {}, "/v1/conversations/**": {}}}' application_id=2c59f277-5a88-4fab-88c4-919ee28xxxxx
```

```bash
$ nexmo jwt:generate ./private.key sub=jerry acl='{"paths": {"/v1/sessions/**": {}, "/v1/users/**": {}, "/v1/conversations/**": {}}}' application_id=2c59f277-5a88-4fab-88c4-919ee28xxxxx
```

```java
//LoginActivity.java
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private String tomToken;
    private String jerryToken;
    private Button loginBtn;
    private Button chatBtn;
    private ConversationClient conversationClient;
    private TextView loginTxt;
    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        conversationClient = ((ConversationClientApplication) getApplication()).getConversationClient();

        loginTxt = (TextView) findViewById(R.id.login_text);
        loginBtn = (Button) findViewById(R.id.login);
        logoutBtn = (Button) findViewById(R.id.logout);
        chatBtn = (Button) findViewById(R.id.chat);

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
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveConversations();
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

First let's log one of our users in:

```java
//LoginActivity.java
private void login() {
    final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this)
            .setTitle("Which user are you logging in as?")
            .setItems(new String[]{"tom", "jerry"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        loginAsUser(tomToken);
                    } else {
                        loginAsUser(jerryToken);
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
    conversationClient.login(token, new LoginListener() {
        @Override
        public void onLogin(User user) {
            showLoginSuccessAndAddInvitationListener(user);
            retrieveConversations();
        }

        @Override
        public void onUserAlreadyLoggedIn(User user) {
            showLoginSuccessAndAddInvitationListener(user);
            retrieveConversations();
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
```

We'll be running this device on two different devices (on an emulator or physical devices), so we'll first ask which user you're logging in as. When you select a user we'll login with their JWT. We'll also add an invitation listener and retrieve any conversations that user is already a part of. Let's cover what the `showLoginSuccessAndAddInvitationListener(user)` method will do.

```java
//LoginActivity.java
private void showLoginSuccessAndAddInvitationListener(final User user) {
    conversationClient.addConversationInvitedListener(new ConversationInvitedListener() {
        @Override
        public void onConversationInvited(final Conversation conversation, Member invitedMember, String invitedByUsername) {
            logAndShow(invitedByUsername + " invited you to their chat");
            conversation.join(new JoinListener() {
                @Override
                public void onConversationJoined(Member member) {
                    goToConversation(conversation);
                }

                @Override
                public void onError(int errCode, String errMessage) {
                    logAndShow("Error joining conversation: " + errMessage);
                }
            });


        }
    });
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            loginTxt.setText("Logged in as " + user.getName() + "\nStart chatting!");
        }
    });
}
```

Calling `addConversationInvitedListener` on an instance of `ConversationClient` let's you respond to events where a user is invited to a conversation. In this example we're going to show that the user was invited, join the conversation, and then navigate to our `ChatActivity` to participate in that conversation. `addConversationInvitedListener` takes a `ConversationInvitedListener` object as a parameter. The `ConversationInvitedListener` has two callbacks: `onConversationInvited`, which means the user successfully received the invite, and an error callback.

Now let's go back to our `retrieveConversations()` method:


```java
//LoginActivity.java
private void retrieveConversations() {
    conversationClient.getConversations(new ConversationListListener() {
        @Override
        public void onConversationList(List<Conversation> conversationList) {
            if (conversationList.size() > 0) {
                showConversationList(conversationList);
            } else {
                showCreateConversationDialog();
            }
        }

        @Override
        public void onError(int errCode, String errMessage) {
            logAndShow("Error listing conversations: " + errMessage);
        }
    });
}
```

There are two ways to see what conversations a member is a part of. `conversationClient.getConversations()` retrieves the full list of conversations the logged in user is a Member of asynchronously. If you want retreive the list of conversations a user is a part of in a synchronous manner, you can call `conversationClient.getConversationList()`

If there wasn't an error retrieving the list of conversations, we'll check if the user has more than one conversation that they are a part of. If there is more than one conversation, then we'll show a dialog allowing the user to select a conversation to enter. If the user doesn't have any conversations then we'll show a dialog where the user can create and join a new conversation.

For now, let's show the list of conversations.

```java
//LoginActivity.java
private void showConversationList(final List<Conversation> conversationList) {
    List<String> conversationNames = new ArrayList<>(conversationList.size());
    for (Conversation convo : conversationList) {
        conversationNames.add(convo.getDisplayName());
    }

    final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this)
            .setTitle("Enter or create a conversation")
            .setItems(conversationNames.toArray(new CharSequence[conversationNames.size()]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    goToConversation(conversationList.get(which));
                }
            })
            .setPositiveButton("New conversation", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showCreateConversationDialog();
                }
            })
            .setNegativeButton("Dismiss", null);

    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            dialog.show();
        }
    });
}
```

First we'll loop through the conversations the user is a member of and then show that list in a AlertDialog. If the user selects on of the already created conversations, we'll go to the `ChatActivity` with that conversation. If the user selects "New conversation", then we'll show a new dialog allowing them to create a new conversation.

```java
//LoginActivity.java
private void showCreateConversationDialog() {
    final EditText input = new EditText(LoginActivity.this);
    final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this)
            .setTitle("Enter conversation name")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    createConversation(input.getText().toString());
                }
            })
            .setNegativeButton("Dismiss", null);

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

private void createConversation(String name) {
    conversationClient.newConversation(name, new ConversationCreateListener() {
        @Override
        public void onConversationCreated(final Conversation conversation) {
            conversation.join(new JoinListener() {
                @Override
                public void onConversationJoined(Member member) {
                    logAndShow("Created and joined Conversation: " + conversation.getDisplayName());
                    goToConversation(conversation);
                }

                @Override
                public void onError(int errCode, String errMessage) {
                    logAndShow("Error joining conversation: " + errMessage);
                }
            });
        }

        @Override
        public void onError(int errCode, String errMessage) {
            logAndShow("Error creating conversation: " + errMessage);
        }
    });

}

private void goToConversation(final Conversation conversation) {
    Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
    intent.putExtra("CONVERSATION_ID", conversation.getConversationId());
    startActivity(intent);
}
```

In the first quickstart, we created a new conversation and didn't allow the user to name the conversation. Here we're showing an AlertDialog with an EditText so the user can name the conversation in `showCreateConversationDialog()`. Once we have the conversation name we can create and join the conversation in `createConversation()`. After the conversation is succesfully created and joined, then the user will directed to the `ChatActivty` passing along the conversation ID as an extra in `goToConversation()`

# Inviting a user and sending messages in Chat Activity

Let's make or update the layout for `ChatActivity`like so:

```xml
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
    private static final String TAG = ChatActivity.class.getSimpleName();

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

        conversationClient = ((ConversationClientApplication) getApplication()).getConversationClient();

        Intent intent = getIntent();
        String conversationId = intent.getStringExtra("CONVERSATION_ID");
        convo = conversationClient.getConversation(conversationId);

        addListener();
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

For this quickstart, we've added the ability for the user to invite the other user. To do so we need to create an options menu.

```java
//ChatActivity.java
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
```

In res/menu/chat_menu.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <item android:id="@+id/invite"
        android:title="Invite"
        app:showAsAction="always"
        android:icon="@drawable/ic_person_add_black_24dp"/>
</menu>
```
I'm using [Vector Asset Studio](https://developer.android.com/studio/write/vector-asset-studio.html#running) to import the ic_person_add_black_24dp drawable.

Now when the user clicks the invite user button we'll call the following method:

```java
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
```

`convo.invite` will invites another user. All you need to do is pass in the user's name and an `InviteSendListener`. The user's name comes from the users we created in the [Setup](#setup) steps.

The steps to send and receive messages are the same as they are from quickstart 1. Feel free to navigate to that quickstart to revisit the steps.

# Trying it out

Once you've completed this quickstart, you can run the sample app on two different devices. You'll be able to login as a user, join an exisitng conversation or create a new one, invite the other user, and chat with that user. Here's a gif of the demo in action.

![Awesome Chat](http://g.recordit.co/lyoKa93BiQ.gif)
