package com.uber.uberhack;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    Bitmap photo;
    String phonenumber;
    static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    double latti;
    double longi;


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
        getLocation();


        startService(new Intent(this, SpeechRecognitionService.class));

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

//    void getLocation() {
//        if( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//
//        } else {
//            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//            if (location != null){
//                double latti = location.getLatitude();
//                double longi = location.getLongitude();
//
//            } else {
//                //default recife antigo (recife-pe) in case of null
//                double latti = -8.0627363;
//                double longi = -34.8681825;
//            }
//        }
//
//    }

    void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null){
                latti = location.getLatitude();
                longi = location.getLongitude();

            } else {
                //default recife antigo (recife-pe) in case of null
                latti = -8.0627363;
                longi = -34.8681825;
            }
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
                                    smsManager.sendTextMessage("8199828858585", null, "Estou em perigo. Minha localização é : " + "maps.google.com/?ll="+latti+","+longi+" e isso é o que está acontecendo na minha câmera: " + downloadUrl.toString(), null, null);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage("8199828858585", null, "Estou em perigo. Minha localização é", null, null);
                                }
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

    private void sendSms(String phonenumber,String message, boolean isBinary) {
        SmsManager manager = SmsManager.getDefault();
        PendingIntent piSend = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
        PendingIntent piDelivered = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
        if(isBinary) {
            byte[] data = new byte[message.length()];
            for(int index=0; index<message.length() && index < 160; ++index) {
                data[index] = (byte)message.charAt(index);
            }
            manager.sendDataMessage(phonenumber, null, (short) 8888, data,piSend, piDelivered);
        } else {
            int length = message.length();
            if(length > 160) {
                ArrayList<String> messagelist = manager.divideMessage(message);
                manager.sendMultipartTextMessage(phonenumber, null, messagelist, null, null);
            }
            else {
                manager.sendTextMessage(phonenumber, null, message, piSend, piDelivered);
            }
        }
    }


}
