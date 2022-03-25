package com.example.sight;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TTS {
    private static TextToSpeech textToSpeech;

    private TTS() {
    }

    private static void setupTextToSpeech(Context context) {
        textToSpeech = new TextToSpeech(context, i -> {
            if (i == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.UK);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language Missing");
                }
            } else {
                Log.e("TTS", "Not Init");
            }
        });
        //textToSpeech.setSpeechRate(0.3f);
    }

    public static TextToSpeech getInstance(Context context) {
        if (textToSpeech == null) {
            setupTextToSpeech(context);
        }
        return textToSpeech;
    }

    public static void destroySpeech() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}