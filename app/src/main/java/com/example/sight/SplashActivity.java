package com.example.sight;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deleteStatusBarActionBar();
        setContentView(R.layout.activity_splash);
        tts = TTS.getInstance(getApplicationContext());
        tts.speak("Welcome", TextToSpeech.QUEUE_ADD, null, null);

        Handler handleTransition = new Handler();
        handleTransition.postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity2.class));
            finish();
        }, 1500);

    }

    private void deleteStatusBarActionBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.hide();
    }
}