package com.uber.uberhack;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by paula on 19/05/18.
 */

public class SpeechRecognitionService extends Service {

    private SpeechRecognizer sr;
    private static final String TAG = "MyStt3Activity";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "onReadyForSpeech");
            }

            public void onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech");
            }

            public void onRmsChanged(float rmsdB) {
                Log.d(TAG, "onRmsChanged");
            }

            public void onBufferReceived(byte[] buffer) {
                Log.d(TAG, "onBufferReceived");
            }

            public void onEndOfSpeech() {
                Log.d(TAG, "onEndofSpeech");
            }

            public void onError(int error) {
                Log.d(TAG, "error " + error);
                startRecognitionIntent();
            }

            public void onResults(Bundle results) {
                String str = new String();
                Log.d(TAG, "onResults " + results);
                ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                for (int i = 0; i < data.size(); i++) {
                    Log.d(TAG, "result " + data.get(i));
                    str += data.get(i);

                    if (!data.get(i).equals("")) {
                        sendBroadcast(new Intent("TIRARFOTO"));

                    }
                }
                startRecognitionIntent();

            }

            public void onPartialResults(Bundle partialResults) {
                Log.d(TAG, "onPartialResults");
            }

            public void onEvent(int eventType, Bundle params) {
                Log.d(TAG, "onEvent " + eventType);
            }
        });
        Log.d(TAG, "Service Started.");
        startRecognitionIntent();

    }

    private void startRecognitionIntent () {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        sr.startListening(intent);
    }
}

