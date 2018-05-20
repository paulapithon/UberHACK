package com.uber.uberhack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    final String TAG = "CAMERA";
    Camera camera;

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


    public void capture() {
        Camera.PictureCallback pictureCB = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera cam) {
                File picFile = getOutputMediaFile(0);
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
