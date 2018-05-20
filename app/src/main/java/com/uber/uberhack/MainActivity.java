package com.uber.uberhack;

import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mText;
    private SpeechRecognizer sr;
    private static final String TAG = "MyStt3Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button speakButton = (Button) findViewById(R.id.btn_speak);
        mText = (TextView) findViewById(R.id.textView1);
        speakButton.setOnClickListener(this);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
        }

        class listener implements RecognitionListener
        {
            public void onReadyForSpeech(Bundle params)
            {
                Log.d(TAG, "onReadyForSpeech");
            }
            public void onBeginningOfSpeech()
            {
                Log.d(TAG, "onBeginningOfSpeech");
            }
            public void onRmsChanged(float rmsdB)
            {
                Log.d(TAG, "onRmsChanged");
            }
            public void onBufferReceived(byte[] buffer)
            {
                Log.d(TAG, "onBufferReceived");
            }
            public void onEndOfSpeech()
            {
                Log.d(TAG, "onEndofSpeech");
            }
            public void onError(int error)
            {
                Log.d(TAG,  "error " +  error);
                mText.setText("error " + error);
            }
            public void onResults(Bundle results)
            {
                String str = new String();
                Log.d(TAG, "onResults " + results);
                ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                for (int i = 0; i < data.size(); i++)
                {
                    Log.d(TAG, "result " + data.get(i));

                    str += data.get(i);
                }

                mText.setText("results: "+ data.get(data.size() - 1));

            }
            public void onPartialResults(Bundle partialResults)
            {
                Log.d(TAG, "onPartialResults");
            }
            public void onEvent(int eventType, Bundle params)
            {
                Log.d(TAG, "onEvent " + eventType);
            }
        }
    public void onClick(View v) {
        if (v.getId() == R.id.btn_speak)
        {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");

            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
            sr.startListening(intent);
            Log.i("111111","11111111");
        }
    }
}
