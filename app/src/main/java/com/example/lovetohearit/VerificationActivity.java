package com.example.lovetohearit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;

public class VerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        //user inputted aspects
        final EditText username = findViewById(R.id.usernameVerify);
        final EditText code = findViewById(R.id.passwordVerify);

        Button verify = findViewById(R.id.verifybutton);
        //determines function when button is clicked
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ConfirmTask().execute(String.valueOf(code.getText()),
                        String.valueOf(username.getText()));
            }
        });
    }

    //ensures the verification code is right
    private class ConfirmTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            final String[] result = new String[1];

            //Callback handler for confirmSignUp API
            final GenericHandler confirmationCallback = new GenericHandler() {
                @Override
                public void onSuccess() {
                    Looper.prepare();
                    //User was successfully confirmed
                    result[0] = "Succeeded";
                    //quick popup if success
                    Context context = getApplicationContext();
                    CharSequence text = "Your account has been verified!";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    //goes back to login screen if code is right and account is confirmed
                    login();
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.i("verify","Failed: " + exception.getMessage());
                    //if wrong code, then prints error
                    TextView tv = findViewById(R.id.incorrect);
                    tv.setText("Incorrect verification code and/or username");
                }
            };

            CognitoSettings cognitoSettings = new CognitoSettings(VerificationActivity.this);

            CognitoUser user = cognitoSettings.getUserPool().getUser(strings[1]);
            //this will cause confirmation to fail if same username has been used for different user in same pool
            user.confirmSignUp(strings[0], false, confirmationCallback);

            return result[0];
        }

    }

    //directs to login page
    private void login() {
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
    }
}
