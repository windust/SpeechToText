package com.spinningnoodle.interaction.speechtotext.capture;

import javax.sound.sampled.AudioFormat;
import java.io.File;

/**
 * Created by Freddy on 4/15/2015.
 */
public class IntegrationMic {
    public static void main(String[] args) {
        MicAudioProvider provider = new MicAudioProvider(new MicAudioProvider.Settings(MicAudioProvider.CaptureType.NOISE_THRESHOLD, null,.05, new AudioFormat(8000f, 16, 1, true, false), new File("test.wav")));
        provider.start();
        File testFile = provider.capture();
        provider.stop();
    }
}
