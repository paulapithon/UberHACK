package com.uber.uberhack.view;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.uber.uberhack.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    final String TAG = "CAMERA";
    Camera camera;
    File picFile;
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = Camera.open();
        try {
            camera.setPreviewDisplay(new SurfaceView(getBaseContext()).getHolder());
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        startService(new Intent(this, com.uber.uberhack.SpeechRecognitionService.class));

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                capture();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("TIRARFOTO");
        registerReceiver(receiver, filter);

    }


    public void capture() {
        Camera.PictureCallback pictureCB = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera cam) {
                picFile  = getOutputMediaFile(0);
                if (picFile == null) {
                    Log.e(TAG, "Couldn't create media file; check storage permissions?");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(picFile);
                    fos.write(data);
                    fos.close();
                    Bitmap bitmap = BitmapFactory.decodeFile(picFile.getAbsolutePath());
                    ((ImageView) findViewById(R.id.foto)).setImageBitmap(bitmap);

                    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                    Uri file = Uri.fromFile(picFile);
                    StorageReference riversRef = mStorageRef.child("images/help.jpg");
                    riversRef.putFile(file)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    //send message
                                    SmsManager smsManager = SmsManager.getDefault();

                                    @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                                    double latti;
                                    double longi;
                                    if (location != null){
                                        latti = location.getLatitude();
                                        longi = location.getLongitude();

                                    } else {
                                        //default recife antigo (recife-pe) in case of null
                                        latti = -8.0627363;
                                        longi = -34.8681825;
                                    }

                                    smsManager.sendTextMessage("8199828858585", null, "Estou em perigo. Minha localização é: " + "https://maps.google.com/?ll="+latti+","+longi+" e isso é o que está acontecendo na minha câmera: " + downloadUrl.toString() + ". Fique alerta.", null, null);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {}
                            });

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found: " + e.getMessage());
                    e.getStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "I/O error writing file: " + e.getMessage());
                    e.getStackTrace();
                }
            }
        };
        camera.takePicture(null, null, pictureCB);

    }

    private File getOutputMediaFile(int type)
    {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getPackageName());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Failed to create storage directory.");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK).format(new Date());
        if (type == 0) {
            File file =  new File(dir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
            return  file;
        }
        else {
            return null;
        }
    }

}
