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
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.core.Amplify;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "login";
    private CognitoUser user;

    //guides user to sign up activity screen upon clicking the sign up button
    public void signUp(View view) {
        Intent signUpIntent = new Intent(this, SignUpActivity.class);
        startActivity(signUpIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
 //tried and failed code
/*
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, userStateDetails.getUserState().toString());
                switch (userStateDetails.getUserState()){
                    case SIGNED_IN:
                        Intent i = new Intent(LoginActivity.this, MainScreenActivity.class);
                        startActivity(i);
                        break;
                    case SIGNED_OUT:
                        showSignIn();
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                        showSignIn();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    private void showSignIn() {
        try {
            AWSMobileClient.getInstance().showSignIn(this,
                    SignInUIOptions.builder().nextActivity(MainScreenActivity.class).build());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
    */
//accesses user inputted logins
    EditText username = findViewById(R.id.usernameLogin);
    EditText password = findViewById(R.id.passwordLogin);

        //uses authentication handler to check if credentials are valid or not
        final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                Log.i("login", "Success");
                //quick popup indicating successful login
                Context context = getApplicationContext();
                CharSequence text = "You're logged in! Enjoy!";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                //if success then goes to main screen
                main();
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {

                //resolves error where code would break if no username was entered
                if (userId == null) {
                    TextView tv = findViewById(R.id.error);
                    tv.setText("No username found");
                }
                else {
                    //need to get the userId and password to continue
                    AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId,
                            String.valueOf(password.getText()), null);

                    Log.i("Login", "Password checked");
                    //pass the user sign-in credentials to the continuation
                    authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                    //allow the sign-in to continue
                    authenticationContinuation.continueTask();
                }
            }

            //not used, they just come with the handler
            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {

            }
            //if failed, produces an error message
            @Override
            public void onFailure(Exception exception) {
                TextView tv = findViewById(R.id.error);
                tv.setText("Incorrect Username or Password");
            }
        };

        Button login = findViewById(R.id.loginbutton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CognitoSettings cognitoSettings = new CognitoSettings(LoginActivity.this);

                user = cognitoSettings.getUserPool()
                        .getUser(String.valueOf(username.getText()));

                user.getSessionInBackground(authenticationHandler);
            }
        });
    }

    //directs to main screen
    private void main () {
        Intent main = new Intent(this, MainScreenActivity.class);
        startActivity(main);
    }
    //gets user
    public CognitoUser getUser() {
        return user;
    }
}
