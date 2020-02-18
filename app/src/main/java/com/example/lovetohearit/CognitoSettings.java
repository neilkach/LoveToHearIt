package com.example.lovetohearit;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class CognitoSettings {

    private String userPoolId = "us-east-1_vJK7E2TOa";
    private String clientId = "6iabarhtihnst6qkt9p0dtb271";
    private String clientSecret = "1rvqqmgmakb4au3v0u1c63cceac953i5413rce8l9mu4036kglca";
    private Regions cognitoRegion = Regions.US_EAST_1;

    private String identityPoolId = "us-east-1:ae8bf1fd-6c34-4a5d-9c8c-12d804b90002";

    private Context context;

    //constructor initializing context instance variable
    public CognitoSettings (Context ctx) {
        context = ctx;
    }

    //getters for all the instance variables
    public String getUserPoolId() {
        return userPoolId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Regions getCognitoRegion() {
        return cognitoRegion;
    }

    //entry point for all interactions with AWS user pool
    public CognitoUserPool getUserPool() {
        return new CognitoUserPool(context, userPoolId, clientId, clientSecret, cognitoRegion);
    }

    public CognitoCachingCredentialsProvider getCredentialsProvider() {
        return new CognitoCachingCredentialsProvider(context.getApplicationContext(), identityPoolId, cognitoRegion);
    }

}
