package com.example.sight;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CacheActivity extends AppCompatActivity {

    public TextToSpeech tts;
    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache);
        tts = TTS.getInstance(getApplicationContext());
        tts.speak("Welcome", TextToSpeech.QUEUE_ADD, null, null);

        if (!CheckPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 101);
        } else {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }
    }

    private boolean CheckPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(this, SplashActivity.class));
                finish();
            }
        }
    }
}