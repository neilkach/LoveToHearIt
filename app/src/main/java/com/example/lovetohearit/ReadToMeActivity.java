package com.example.lovetohearit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import okio.Utf8;

import static java.lang.System.in;
import static java.nio.charset.Charset.forName;

public class ReadToMeActivity extends AppCompatActivity {
    private TextToSpeech tts;
    private EditText et;
    private SeekBar pitch;
    private SeekBar speed;
    private Button speak;
    private String LOG_TAG = "Read";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        speak = findViewById(R.id.buttonSpeak);

        //initializes text to speech engine
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.getDefault());

                    //ensures language is valid
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        speak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        //initializes intance variables
        et = findViewById(R.id.editText);
        pitch = findViewById(R.id.seekBarPitch);
        speed = findViewById(R.id.seekBarSpeed);

        //sets on click listener for button
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = et.getText().toString();
                speak(text);
            }
        });

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
    }

    //method detailing phone to speak
    private void speak(String text) {

        //ensures pitch has a minimum of 0.1
        float pitchValue = (float) pitch.getProgress() / 50;
        if (pitchValue < 0.1) pitchValue = 0.1f;

        //ensures speed is at least 0.1
        float speedValue = (float) speed.getProgress() / 50;
        if (speedValue < 0.1) speedValue = 0.1f;

        //sets up text to speech engine with inputted pitch and speed values
        tts.setPitch(pitchValue);
        tts.setSpeechRate(speedValue);

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);

    }

    //stops and shuts down engine when app stops
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    //stops engine upon click of button
    public void stop(View v) {
        tts.stop();
    }

    //leads to lates new story google search
    public void web(View v)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=latest+news+stories&rlz=1C1SQJL_enUS829US829&oq=latest+news+stories&aqs=chrome.0.0l8.3073j0j7&sourceid=chrome&ie=UTF-8"));
        startActivity(intent);
    }

    //uploads text file to s3
    public void save(View v) throws IOException {
        TransferNetworkLossHandler.getInstance(this);

        //transfer utility object is what we utilize to upload and download objects in s3 bucket
        TransferUtility transferUtility = TransferUtility.builder()
                .context(getApplicationContext())
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                .build();

        //accesses user inputted name of text file
        EditText tv = findViewById(R.id.editNameFile);
        String nameFile = String.valueOf(tv.getText());

        //user inputted text
        String text = et.getText().toString();

        File file = new File(getExternalCacheDir() + nameFile + ".txt");

        //writes text to file
        FileUtils.writeStringToFile(file, text, forName("UTF-8"));

        //transferObserver keeps track of upload
        TransferObserver uploadObserver = transferUtility.upload("lovetohearit162603-dev", "public/" + nameFile, file);

        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                //if completed, let user know recording has been saved
                if (TransferState.COMPLETED == state) {
                    Log.i("Completion", "Upload completed");
                    Context context = getApplicationContext();
                    CharSequence text = "The text has been saved";
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

    //downloads text file from s3
    public void read(View v) {
        try {
            File outputDir = getExternalCacheDir();

            //accesses user inputted name of text to read
            EditText tv = findViewById(R.id.editNameFile);
            String nameFile = String.valueOf(tv.getText());

            File outputFile = new File(outputDir + "/" + nameFile + ".txt");

            //initializes transferUtility to handle transfers of files
            TransferUtility transferUtility = TransferUtility.builder().context(getApplicationContext())
                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                    .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                    .build();

            TransferObserver downloadObserver = transferUtility.download("lovetohearit162603-dev", "public/" + nameFile, outputFile);

            //Attach a listener to the observer to get state update and progress notifications
            downloadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        //Handle a completed download
                        Log.i(LOG_TAG, "Download completed");
                        //reads downloaded text file
                        BufferedReader br = null;
                        try {
                            br = new BufferedReader(new FileReader(outputFile));
                            String st;
                            String read = "";
                            while ((st = br.readLine()) != null)
                                read += st;
                            //reads text
                            speak(read);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                //updates log based on progress
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                    int percentDone = (int)percentDonef;

                    Log.d(LOG_TAG, "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");

                }

                //notifies user in case of error
                @Override
                public void onError(int id, Exception ex) {
                    Context context = getApplicationContext();
                    CharSequence text = "No such file exists";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            });
        } catch (Exception e) {
            Log.i(LOG_TAG, "File error");
        }
    }

    /*
    //reads link inputted by user
    public void readLink(View v) {
        URL url;

        //accesses user inputted link
        EditText site = findViewById(R.id.editLink);
        String link = site.getText().toString();
        try {
            url = new URL(link);
            //creates buffered reader from url so it can be read
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            String inputLine;
            String read = "";

            //adds lines of buffered reader to input for reading
            while ((inputLine = reader.readLine()) != null)
                read += inputLine;

            //closes reader
            reader.close();

            //ensures pitch has a minimum of 0.1
            float pitchValue = (float) pitch.getProgress() / 50;
            if (pitchValue < 0.1) pitchValue = 0.1f;

            //ensures speed is at least 0.1
            float speedValue = (float) speed.getProgress() / 50;
            if (speedValue < 0.1) speedValue = 0.1f;

            //sets up text to speech engine with inputted pitch and speed values
            tts.setPitch(pitchValue);
            tts.setSpeechRate(speedValue);

            //engine reads content of url
            tts.speak(read, TextToSpeech.QUEUE_FLUSH, null);
        } catch (IOException murl) {
            Log.e("URL", "Invalid URL");
        }
    }

     */
}
