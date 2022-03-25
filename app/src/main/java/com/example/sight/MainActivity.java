package com.example.sight;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

//Activity El VQA
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    FloatingActionButton fabButton;
    TextToSpeech tts;
    ImageView imageCaption;
    TextView txtCaption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts = TTS.getInstance(getApplicationContext());
        fabButton = findViewById(R.id.fab_mic);
        imageCaption = findViewById(R.id.img_caption);
        txtCaption = findViewById(R.id.txt_caption);
        String caption = getIntent().getExtras().getString("text");

        if (caption == null || caption.equals("")) {
            txtCaption.setText("No Data");
        } else {
            txtCaption.setText(caption);
        }

        tts.speak(caption,
                TextToSpeech.QUEUE_ADD,
                null,
                null);

        Bitmap bmp = getImageFromStorage("/sdcard/Pictures/SightPhotos/caption.png");
        imageCaption.setImageBitmap(bmp);

        tts.speak("You are in the Question page Please Use The Button in the Right Bottom to say the command",
                TextToSpeech.QUEUE_ADD,
                null,
                null);

        fabButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            tts.speak("Up Pressed", TextToSpeech.QUEUE_FLUSH, null, null);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            tts.speak("Down Pressed", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                tts.speak(Objects.requireNonNull(result).get(0), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }
}