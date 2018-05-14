package com.nexmo.callingusers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nexmo.sdk.conversation.client.Call;
import com.nexmo.sdk.conversation.client.CallEvent;
import com.nexmo.sdk.conversation.client.ConversationClient;
import com.nexmo.sdk.conversation.client.event.NexmoAPIError;
import com.nexmo.sdk.conversation.client.event.RequestHandler;
import com.nexmo.sdk.conversation.client.event.ResultListener;

import java.util.Collections;

import static android.Manifest.permission.RECORD_AUDIO;

//CallActivity.java
public class CallActivity extends AppCompatActivity {
    private String TAG = ChatActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_AUDIO = 0;

    private ConversationClient conversationClient;
    private Call currentCall;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        conversationClient = ((ConversationClientApplication) getApplication()).getConversationClient();
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        TextView usernameTxt = findViewById(R.id.username);
        usernameTxt.setText("In call with "+ username);

        if (checkAudioPermissions()) {
            callUser(username);
        } else {
            logAndShow("Check audio permissions");
        }

        Button hangUpBtn = findViewById(R.id.hangup);
        hangUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hangup();
            }
        });
    }


    private void callUser(String username) {
        conversationClient.call(Collections.singletonList(username), new RequestHandler<Call>() {
            @Override
            public void onError(NexmoAPIError apiError) {
                logAndShow(apiError.getMessage());
            }

            @Override
            public void onSuccess(Call result) {
                currentCall = result;
                attachCallListeners(currentCall);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callUser(username);
                    break;
                } else {
                    logAndShow("Enable audio permissions to continue");
                    break;
                }
            }
            default: {
                logAndShow("Issue with onRequestPermissionsResult");
                break;
            }
        }
    }

    private void attachCallListeners(Call incomingCall) {
        //Listen for incoming member events in a call
        ResultListener<CallEvent> callEventListener = new ResultListener<CallEvent>() {
            @Override
            public void onSuccess(CallEvent message) {
                Log.d(TAG, "callEvent : state: " + message.getState() + " .content:" + message.toString());
            }
        };
        incomingCall.event().add(callEventListener);
    }

    private boolean checkAudioPermissions() {
        if (ContextCompat.checkSelfPermission(CallActivity.this, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, RECORD_AUDIO)) {
                logAndShow("Need permissions granted for Audio to work");
            } else {
                ActivityCompat.requestPermissions(CallActivity.this, new String[]{RECORD_AUDIO}, PERMISSION_REQUEST_AUDIO);
            }
        }
        return false;
    }

    private void hangup() {
        if (currentCall != null) {
            currentCall.hangup(new RequestHandler<Void>() {
                @Override
                public void onError(NexmoAPIError apiError) {
                    logAndShow("Cannot hangup: " + apiError.toString());
                }

                @Override
                public void onSuccess(Void result) {
                    logAndShow("Call completed.");
                    finish();
                }
            });

        }
    }

    private void logAndShow(final String message) {
        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CallActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
