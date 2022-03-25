package com.example.sight;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

// Activity EL Image Captioning
public class MainActivity2 extends AppCompatActivity {

    FloatingActionButton fabImage;
    TextToSpeech tts;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ImageCapture imageCapture;
    ImageView imageView;
    PreviewView previewView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        tts = TTS.getInstance(getApplicationContext());
        imageView = findViewById(R.id.testImage);
        fabImage = findViewById(R.id.fabImage);
        previewView = findViewById(R.id.cameraPreview);
        imageView = findViewById(R.id.testImage);
        progressBar = findViewById(R.id.progress_bar);
        fabImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        capturePhoto();
                    } else {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                    }
                }
            }
        });
        cameraProviderFuture = ProcessCameraProvider.getInstance(getApplicationContext());
        cameraProviderFuture.addListener(() -> {
            ProcessCameraProvider cameraProvider = null;
            try {
                cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));

        tts.speak("You are in the Image Caption Page Please Use The Button in the Right Bottom to capture the image",
                TextToSpeech.QUEUE_ADD,
                null,
                null);
    }

    private Bitmap getImageFromStorage(String Path) {
        File imgFile = new File(Path);
        Bitmap myBitmap = null;
        if (imgFile.exists()) {
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        } else {
            Log.d("Opening The Saved Photo", imgFile.getAbsolutePath());
            Toast.makeText(this, "Failed To open The Photo with Path : ", Toast.LENGTH_LONG).show();
        }
        return myBitmap;
    }

    private void capturePhoto() {
        progressBar.setVisibility(View.VISIBLE);
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);
                Toast.makeText(MainActivity2.this, "Done", Toast.LENGTH_SHORT).show();
                Bitmap bmp = getBitmap(image);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                try {
                    File filee = new File("/sdcard/Pictures/SightPhotos");
                    if (!filee.exists()) {
                        filee.mkdir();
                    }

                    File file = new File("/sdcard/Pictures/SightPhotos/caption.png");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream outputStream = new FileOutputStream(file);
                    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                } catch (IOException e) {
                    Log.e("Image Capture", e.getMessage());
                }
                //Caption generation()
                BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();

                try {
                    Bitmap bitmap = getImageFromStorage("/sdcard/Pictures/SightPhotos/caption.png");
                    String caption = ImageCaptionGeneration.generateCaption(bitmap, getApplicationContext());
                    Intent in1 = new Intent(getApplicationContext(), MainActivity.class);
                    in1.putExtra("text", caption);
                    startActivity(in1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //new CaptionGenerationTask().execute(new Parameters(bitmapDrawable.getBitmap() , getApplicationContext()));
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                Toast.makeText(MainActivity2.this, "Error Taking the Picture", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private Bitmap getBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    class Parameters {
        Bitmap img;
        Context context;

        public Parameters(Bitmap img, Context context) {
            this.img = img;
            this.context = context;
        }
    }

    class CaptionGenerationTask extends AsyncTask<Parameters, Void, String> {

        @Override
        protected String doInBackground(Parameters... parameters) {
            Parameters param = parameters[0];
            String caption = "";
            try {
                caption = ImageCaptionGeneration.generateCaption(param.img, param.context);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.e("ASYNC TASK", e.getMessage());
            }
            return caption;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.INVISIBLE);
            Intent in1 = new Intent(getApplicationContext(), MainActivity.class);
            in1.putExtra("text", s);
            startActivity(in1);
        }
    }

    @Override
    protected void onDestroy() {
        TTS.destroySpeech();
        super.onDestroy();
    }
}