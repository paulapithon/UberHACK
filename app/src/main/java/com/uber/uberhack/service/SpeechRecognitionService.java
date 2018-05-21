package com.uber.uberhack.service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.SurfaceView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uber.uberhack.R;
import com.uber.uberhack.UberHACKApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by paula on 19/05/18.
 */

public class SpeechRecognitionService extends Service {

    private SpeechRecognizer speechRecognizer;

    private static final String TAG = "SpeechRecognition";
    private boolean processing;

    private Camera camera;

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
        // Criar serviço de reconhecimento e definir callback
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "onReadyForSpeech");
            }

            public void onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech");
            }

            public void onRmsChanged(float rmsdB) { }

            public void onBufferReceived(byte[] buffer) {
                Log.d(TAG, "onBufferReceived");
            }

            public void onEndOfSpeech() {
                Log.d(TAG, "onEndofSpeech");
            }

            public void onError(int error) {
                startRecognitionIntent();
            }

            public void onResults(Bundle results) {
                Log.d(TAG, "onResults " + results);
                ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                // Iterar em todos os resultados encontrados
                for (int i = 0; i < data.size(); i++) {
                    Log.d(TAG, "result " + data.get(i));
                    // Caso não esteja contida, é a primeira vez que o usuário configura o serviço
                    if (UberHACKApplication.getSafeWord().equals("")) {
                        UberHACKApplication.setSafeWord(data.get(i).toString());
                        sendBroadcast(new Intent("TIRARFOTO"));
                    }
                    // Caso a safe word esteja contida em algum das frases idetificadas
                    // configurar mensagem de ajuda
                    else {
                        if (data.get(i).toString().toLowerCase().contains(UberHACKApplication.getSafeWord().toLowerCase())) {
                            // Caso não esteja processando uma requisição
                            if (!processing) {
                                getCameraFile();
                                processing = true;
                            }

                        }
                    }
                    startRecognitionIntent();
                }
            }

            public void onPartialResults(Bundle partialResults) {
                Log.d(TAG, "onPartialResults");
            }

            public void onEvent(int eventType, Bundle params) { Log.d(TAG, "onEvent " + eventType); }
        });
        Log.d(TAG, "Service Started.");
        startRecognitionIntent();

    }

    /**
     * Ativar intent de identificação de voz
     */

    private void startRecognitionIntent () {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        speechRecognizer.startListening(intent);
    }

    /**
     * Tirar foto
     */

    private void getCameraFile() {
        // Abrir câmera
        try {
            if (camera == null) {
                camera = Camera.open();
                Camera.Parameters params = camera.getParameters();
                List<Camera.Size> sizes = params.getSupportedPictureSizes();
                params.setPictureSize(sizes.get(sizes.size() - 1).width, sizes.get(sizes.size() - 1).height);
                camera.setParameters(params);
                camera.setPreviewDisplay(new SurfaceView(getBaseContext()).getHolder());
                camera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
            getAudioFile("");
        }
        // Definir callback para quando uma imagem for encontrada
        Camera.PictureCallback pictureCB = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera cam) {
                File picFile  = getOutputMediaFile(true);
                if (picFile == null) {
                    Log.e(TAG, "Couldn't create media file; check storage permissions?");
                    getAudioFile("");
                    return;
                }
                try {
                    // Salvar imagem em um arquivo
                    FileOutputStream fos = new FileOutputStream(picFile);
                    fos.write(data);
                    fos.close();
                    Log.d(TAG, "Imagem salva na memória interna!");
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found: " + e.getMessage());
                    e.getStackTrace();
                    getAudioFile("");
                } catch (IOException e) {
                    Log.e(TAG, "I/O error writing file: " + e.getMessage());
                    e.getStackTrace();
                    getAudioFile("");
                }
                camera.release();

                // Salvar imagem no firebase
                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                Uri file = Uri.fromFile(picFile);
                StorageReference riversRef = mStorageRef.child("images/" + picFile.getName());
                riversRef.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "Imagem upada com sucesso!");
                                // Pegar link para o arquivo
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                // Mandar gravar áudio
                                    getAudioFile(downloadUrl.toString());
                                }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                exception.printStackTrace();
                                processing = false;
                                getAudioFile("");
                            }
                        });

            }
        };
        // Tentar tirar foto
        try {
            camera.takePicture(null, null, pictureCB);
        } catch (RuntimeException e) {
            e.printStackTrace();
            getAudioFile("");
        }
    }

    /**
     * Gravar audio por 10 segundos
     */

    private void getAudioFile (final String imageUrl) {
        final File outputFile = getOutputMediaFile(false);
        // Iniciar recorder
        final MediaRecorder myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile.getPath());
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
            // Depois de 10 segundos, parar gravação
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    myAudioRecorder.stop();
                    myAudioRecorder.release();

                    // Salvar áudio no firebase
                    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                    Uri file = Uri.fromFile(outputFile);
                    StorageReference riversRef = mStorageRef.child("audio/" + outputFile.getName());
                    riversRef.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.d(TAG, "Audio upado com sucesso!");
                                    // Pegar link para o arquivo e enviar mensagem
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    sendMessage(imageUrl, downloadUrl.toString());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    exception.printStackTrace();
                                    processing = false;
                                    sendMessage(imageUrl, "");
                                }
                            });
                }
            }, 10000);
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            processing = false;
            sendMessage(imageUrl, "");
        }
    }

    /**
     * Ajustar diretório que irá salvar a imagem
     * @return arquivo da imagem
     */

    private File getOutputMediaFile(boolean isImage) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getPackageName());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Failed to create storage directory.");
                processing = false;
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK).format(new Date());
        String extension = isImage ? "IMG_"+ timeStamp + ".jpg" : "AUD_"+ timeStamp + ".3gp";
        return new File(dir.getPath() + File.separator + extension);

    }

    /**
     * Enviar mensagem com informações do usuário
     * @param imageUrl url da imagem
     * @param audioUrl url do áudio
     */

    private void sendMessage (String imageUrl, String audioUrl) {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Pegar localização atual
        @SuppressLint("MissingPermission")
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        String result = "Estou em perigo, fique em alerta. \n\nLocalização: " + "https://maps.google.com/?ll=";
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
           result += latitude + "," + longitude;
        }
        if (!imageUrl.equals("")) {
            result += "\nCâmera: " + imageUrl;
        }
        if (!audioUrl.equals("")) {
            result += "\nÁudio: " + audioUrl;
        }
        // Enviar mensagem sms contendo informações
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(UberHACKApplication.getSafePhone(), null, result, null, null);
        //Enviar notificação
        sendNotification();

        processing = false;
    }

    /**
     * Enviar notificação para informar que o serviço rodou
     */

    private void sendNotification () {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Alerta de perigo enviado!")
                    .setContentText("Pensando em sua segurança, enviamos uma mensagem para seu contato de emergência já que sua palavra de segurança foi dita. Em breve ajuda entrará em contato.")
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle());
        Intent resultIntent = new Intent(Intent.ACTION_MAIN);
        resultIntent.setType("vnd.android-dir/mms-sms");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(222, mBuilder.build());
    }
}

