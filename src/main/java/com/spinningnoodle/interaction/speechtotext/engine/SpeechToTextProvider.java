package com.spinningnoodle.interaction.speechtotext.engine;

import java.io.File;
import java.util.List;

/**
 * Created by Freddy on 4/15/2015.
 * Interface that implements providers
 * Each provider takes a File and returns a collection of Results
 */

public interface SpeechToTextProvider {
    void start();
    void stop();
    List<TranslationResult> transcribe(File file);

}
