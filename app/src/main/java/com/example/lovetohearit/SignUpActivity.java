package com.example.lovetohearit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private String TAG = "signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_actvity);

        registerUser();
    }

    public void registerUser() {
        //required attributes
        final EditText name = findViewById(R.id.name);
        final EditText email = findViewById(R.id.email);

        //signin stuff
        final EditText username = findViewById(R.id.usernameSignUp);
        final EditText password = findViewById(R.id.passwordSignUp);
/*
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(email.getText().toString(), name.getText().toString());
        AWSMobileClient.getInstance().signUp(username.getText().toString(), password.getText().toString(), attributes, null, new Callback<com.amazonaws.mobile.client.results.SignUpResult>() {
                    @Override
                    public void onResult(com.amazonaws.mobile.client.results.SignUpResult result) {
                        if (!result.getConfirmationState()) {
                            //signup was successful but not confirmed
                            Log.i("signup", "sign up success is confirmed" + result);
                            //quick popup
                            Context context = getApplicationContext();
                            CharSequence text = "Your account has been created.";
                            int duration = Toast.LENGTH_LONG;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                            //if successful then guide to verification activity
                            verify();
                        }

                    }

                    @Override
                    public void onError(Exception e) {
                        //signup failed
                        Log.i("signup", "sign up failure" + e.getLocalizedMessage());
                        TextView tv = findViewById(R.id.missing);
                        tv.setText("Missing fields and/or password is less than 6 characters");
                    }
                });
                */
        //Create a CognitoUserAttributes object and add user attributes
        final CognitoUserAttributes userAttributes = new CognitoUserAttributes();

        final SignUpHandler signupCallback = new SignUpHandler() {
            @Override
            public void onSuccess(CognitoUser user, SignUpResult signUpResult) {
                //signup was successful
                Log.i("signup", "sign up success is confirmed" + signUpResult);
                //quick popup
                Context context = getApplicationContext();
                CharSequence text = "Your account has been created.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                //if successful then guide to verification activity
                verify();
            }

            @Override
            public void onFailure(Exception exception) {
                //signup failed
                Log.i("signup", "sign up failure" + exception.getLocalizedMessage());
                TextView tv = findViewById(R.id.missing);
                tv.setText("Missing fields and/or password is less than 6 characters");
            }
        };

        Button signup = findViewById(R.id.createbutton);
        //determines what happens when button is clicked
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //adds user inputed attributes to userAttributes for initializing user info
                userAttributes.addAttribute("given_name", String.valueOf(name.getText()));
                userAttributes.addAttribute("email", String.valueOf(email.getText()));

                CognitoSettings cognitoSettings = new CognitoSettings(SignUpActivity.this);

                //accesses user pool and creates a new user
                cognitoSettings.getUserPool().signUpInBackground(String.valueOf(username.getText()),
                        String.valueOf(password.getText()), userAttributes,
                        null, signupCallback);
            }
        });
    }

    //directs to verification activity
    private void verify () {
        Intent verify = new Intent(this, VerificationActivity.class);
        startActivity(verify);
    }
}
