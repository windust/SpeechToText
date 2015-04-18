package com.spinningnoodle.interaction.speechtotext.engine.cmusphinx;

import com.spinningnoodle.interaction.speechtotext.engine.SpeechToTextProvider;
import com.spinningnoodle.interaction.speechtotext.engine.TranslationResult;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Freddy on 4/15/2015.
 */
public class CmuSphinxSpeechToTextProvider implements SpeechToTextProvider {
    final Configuration configuration;
    private final StreamSpeechRecognizer recognizer;

    public CmuSphinxSpeechToTextProvider(Configuration configuration, AudioFormat format) {
        this.configuration = configuration;
        configuration.setSampleRate((int) format.getSampleRate());
        try {
            recognizer = new StreamSpeechRecognizer(configuration);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't initialize Stream Speech Recognizer",e);
        }
    }


    public CmuSphinxSpeechToTextProvider(AudioFormat format) {
        this(getDefaultConfiguration(), format);
    }

    private static Configuration getDefaultConfiguration() {
        Configuration configuration = new Configuration();
        // Set path to acoustic model.
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        // Set path to dictionary.
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        // Set language model.
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.dmp");
        return configuration;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public List<TranslationResult> transcribe(File file) {

        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            recognizer.startRecognition(ais);
            SpeechResult result;
            List<TranslationResult> results = new ArrayList<TranslationResult>();
            while ((result = recognizer.getResult()) != null) {
                results.add(new TranslationResult(result.getHypothesis(),1d));
            }

            recognizer.stopRecognition();
            return results;
        } catch (IOException  | UnsupportedAudioFileException e) {
            throw new RuntimeException("Couldn't recognize with CMU Sphinx");
        }
    }
}
