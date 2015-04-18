package com.spinningnoodle.interaction.samples;

import com.spinningnoodle.interaction.speechtotext.capture.AudioProvider;
import com.spinningnoodle.interaction.speechtotext.capture.MicAudioProvider;
import com.spinningnoodle.interaction.speechtotext.engine.SpeechToTextProvider;
import com.spinningnoodle.interaction.speechtotext.engine.TranslationResult;
import com.spinningnoodle.interaction.speechtotext.engine.cmusphinx.CmuSphinxSpeechToTextProvider;
import com.spinningnoodle.interaction.speechtotext.engine.google.GoogleSpeechToTextProvider;

import java.io.File;
import java.util.List;

/**
 * Created by Freddy on 4/18/2015.
 */
public class SimpleTranscriber {

    private final AudioProvider micAudioProvider;
    private final SpeechToTextProvider speechToTextProvider;

    public SimpleTranscriber() {
        micAudioProvider = new MicAudioProvider(new MicAudioProvider.Settings(MicAudioProvider.CaptureType.NOISE_THRESHOLD, null, 0.01, MicAudioProvider.DEFAULT_FORMAT, null));
        speechToTextProvider = new CmuSphinxSpeechToTextProvider(MicAudioProvider.DEFAULT_FORMAT);
        micAudioProvider.start();
        speechToTextProvider.start();

    }



    private void start() {
        boolean shouldProcess = true;
        while (shouldProcess) {
            File file = micAudioProvider.capture();
            List<TranslationResult> results = speechToTextProvider.transcribe(file);
            if (!results.isEmpty()) {
                for (TranslationResult result : results) {
                    System.out.println(result);
                    if (result.getText().toLowerCase().contains("stop")) shouldProcess = false;
                }
            }
        }
        stop();
    }

    private void stop() {
        micAudioProvider.stop();
        speechToTextProvider.stop();
    }

    public static void main(String[] args) {
        SimpleTranscriber transcriber = new SimpleTranscriber();
        transcriber.start();
    }

}
