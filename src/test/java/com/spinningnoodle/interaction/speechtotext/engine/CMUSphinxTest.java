package com.spinningnoodle.interaction.speechtotext.engine;

import com.spinningnoodle.interaction.speechtotext.engine.cmusphinx.CmuSphinxSpeechToTextProvider;
import org.junit.Assert;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.util.List;

/**
 * Created by Freddy on 4/15/2015.
 */
public class CMUSphinxTest {

    @Test
    public void testCMUSphinxTranslation() {
        SpeechToTextProvider speechToTextProvider = new CmuSphinxSpeechToTextProvider(new AudioFormat(8000,16,1,true,false));
        File file = new File("./src/test/java/com/spinningnoodle/interaction/speechtotext/engine/test.wav");

        List<TranslationResult> results = speechToTextProvider.transcribe(file);
        Assert.assertEquals("hello", results.iterator().next().getText());
    }
}
