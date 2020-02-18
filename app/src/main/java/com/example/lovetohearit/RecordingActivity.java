package com.example.lovetohearit;

import androidx.annotation.NonNull;
import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.internal.Constants;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.core.Amplify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class RecordingActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecord";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    private MediaRecorder recorder = null;
    private boolean startRecording = true;

    private MediaPlayer player = null;
    private boolean startPlaying = true;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));

        // Initialize the AWSMobileClient if not initialized
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(LOG_TAG, "AWSMobileClient initialized. User State is " + userStateDetails.getUserState());
            }

            @Override
            public void onError(Exception e) {
                Log.e(LOG_TAG, "Initialization error.", e);
            }
        });

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
    }

    //gets permission to be able to record audio on device
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    //method initiating recording functions when record button is clicked
    public void record(View v) {
        onRecord(startRecording);
        Button record = findViewById(R.id.button_record);
        //sets text according to what functionality the button will provide when clicked
        if (startRecording) {
            record.setText("Stop recording");
        } else {
            record.setText("Start recording");
        }
        startRecording = !startRecording;
    }

    //determines whether to start or stop recording based on booolean parameter
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    //commences recording process
    private void startRecording() {
        recorder = new MediaRecorder();

        //accesses user inputted name of recording
        EditText tv = findViewById(R.id.recordingName);
        String nameFile = String.valueOf(tv.getText());

        //makes sure there is a name for file
        if (nameFile == null || nameFile.equals("")) {
            //quick popup indicating there is no name
            Context context = getApplicationContext();
            CharSequence text = "Please enter a name for your recording";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            startRecording = false;
        } else {
            //completes file path
            fileName += "/" + nameFile + ".mp3";

            Log.i(LOG_TAG, "Recording begun");
            //sets up details of recording
            recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            recorder.setOutputFile(fileName);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            //prepares recorder to record
            try {
                recorder.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }

            //starts recording
            recorder.start();
        }
    }

    private void stopRecording() {
        //stops recording process
        recorder.stop();

        //quick popup indicating recording has been created
        Context context = getApplicationContext();
        CharSequence text = "Recording created, press Start Playing to hear it or Save to save it to your account";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
/*
    //helper class that refreshes authentification credentials
    private class RefreshAsyncTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... integers) {
            credentialsProvider.refresh();
            return integers[0];
        }

        @Override
        protected void onPostExecute(Integer action) {
            choose(action);
        }
    }

    //authentication proceedings, checks whether user is logged in
    private void setup(final int action) {
        Log.i(LOG_TAG, "We're in");
        final CognitoSettings cognitoSettings = new CognitoSettings(this);

        //identity pool credentials provider
        credentialsProvider = cognitoSettings.getCredentialsProvider();

        //get user - User Pool
        CognitoUser currentUser = cognitoSettings.getUserPool().getCurrentUser();

        //get token for logged in user - user pool
        currentUser.getSessionInBackground(new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                //if user is authenticated
                if (userSession.isValid()) {
                    //get id token from cognito user session
                    String idToken = userSession.getIdToken().getJWTToken();

                    if (idToken.length() > 0) {
                        //set up as a credentials provider
                        Map<String, String> logins = new HashMap<>();
                        logins.put("cognito-idp.us-east-1.amazonaws.com/us-east-1_vJK7E2TOa", idToken);
                        credentialsProvider.setLogins(logins);

                        //refresh provider off main thread
                        new RefreshAsyncTask().execute(action);
                    }

                    //either uploads or downloads based on desired action
                    choose(action);
                }
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                choose(action);
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {

            }

            @Override
            public void onFailure(Exception exception) {
                choose(action);
            }
        });
    }

    //determines whether to save or pull audio from account
    private void choose(int action) {
        switch (action) {
            case 1:
                upload();
                break;
            case 2:
                download();
        }
    }

    //kills credentials provider when app closes
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //clear the cached/saved credentials so we don't use them for guest user if not logged in
        credentialsProvider.clear();
    }

 */

    //uploads audio file to s3
    public void upload(View v) {
        TransferNetworkLossHandler.getInstance(this);

        //transfer utility object is what we utilize to upload and download objects in s3 bucket
        TransferUtility transferUtility = TransferUtility.builder()
                .context(getApplicationContext())
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                .build();

        //accesses user inputted name of recording
        EditText tv = findViewById(R.id.recordingName);
        String nameFile = String.valueOf(tv.getText());

        File file = new File(fileName);

        //transferObserver keeps track of upload
        TransferObserver uploadObserver = transferUtility.upload("lovetohearit162603-dev", "public/" + nameFile, file);

        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                //if completed, let user know recording has been saved
                if (TransferState.COMPLETED == state) {
                    Log.i("Completion", "Upload completed");
                    Context context = getApplicationContext();
                    CharSequence text = "The recording has been saved";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(LOG_TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");

            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });

    }

    //downloads audio file from s3
    public void download() {
        try {
            File outputDir = getExternalCacheDir();

            //accesses user inputted name of recording to play
            EditText tv = findViewById(R.id.playName);
            String nameFile = String.valueOf(tv.getText());

            File outputFile = new File(outputDir + "/" + nameFile + ".mp3");

            //initializes transferUtility, used to handle objects
            TransferUtility transferUtility = TransferUtility.builder().context(getApplicationContext())
                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                    .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                    .build();

            //observes transfers
            TransferObserver downloadObserver = transferUtility.download("lovetohearit162603-dev", "public/" + nameFile, outputFile);

            //Attach a listener to the observer to get state update and progress notifications
            downloadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        //Handle a completed download
                        Log.i(LOG_TAG, "Download completed");
                        //plays downloaded audio file
                        fileName = outputFile.getPath();
                        onPlay(startPlaying);
                        startPlaying = !startPlaying;
                    }
                }

                //keeps track of progress of download
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                    int percentDone = (int)percentDonef;

                    Log.d(LOG_TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");

                }

                @Override
                public void onError(int id, Exception ex) {
                    //if no file then just plays what was recently recorded
                    onPlay(startPlaying);
                    startPlaying = !startPlaying;
                }
            });
        } catch (Exception e) {
            Log.i(LOG_TAG, "File error");
        }
    }

    public void play(View v) {
        //enables access to button on screen
        Button play = findViewById(R.id.button_play);
        if (startPlaying) {
            //downloads file if there is one to play
            download();
            play.setText("Stop playing");
        } else {
            play.setText("Start playing");
            onPlay(startPlaying);
            startPlaying = !startPlaying;
        }
    }

    //determines whethere or stop or start playing based on boolean start
    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        //sets up mediaplayer to play the audio
        try {
            Log.i(LOG_TAG, "Playing Recording");
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    //terminates player
    private void stopPlaying() {
        Log.i(LOG_TAG, "Playing Stopped");
        if (player != null) {
            player.release();
            player = null;
        }
    }

    //terminates recorder and player when activity closes, no matter what
    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            Log.i(LOG_TAG, "Playing Stopped");
            player.release();
            player = null;
        }
    }
/*
    public void displayFiles(View v) {
        AmazonS3Client s3 = new AmazonS3Client(AWSMobileClient.getInstance());

        final ObjectListing objectListing = s3.listObjects(
                new ListObjectsRequest("lovetohearit162603-dev", "public/", null, null, null)
                        .withEncodingType(Constants.URL_ENCODING));
    }

 */

}