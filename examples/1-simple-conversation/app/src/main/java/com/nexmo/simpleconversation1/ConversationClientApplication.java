package com.nexmo.simpleconversation1;

import android.app.Application;

public class ConversationClientApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setUpStitchClient();
    }

    private void setUpStitchClient() {
        StitchClient.getInstance().init(getApplicationContext());
    }
}