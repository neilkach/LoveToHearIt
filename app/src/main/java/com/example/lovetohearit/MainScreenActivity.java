package com.example.lovetohearit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;

public class MainScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
    }

    //guides user to recording activity screen upon clicking the record audio button
    public void record(View view) {
        Intent recordIntent = new Intent(this, RecordingActivity.class);
        startActivity(recordIntent);
    }

    //guides user to ReadToMe activity screen upon clicking the read to me button
    public void play(View view) {
        Intent readIntent = new Intent(this, ReadToMeActivity.class);
        startActivity(readIntent);
    }

    //guides user to playing activity screen upon clicking the play audio button
    public void signOut(View view) {
        //accesses user from Login Activity and signs out of account
        LoginActivity la = new LoginActivity();
        CognitoUser user = la.getUser();

        //ensures a user is in session
        if (user != null)
            user.signOut();

        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
