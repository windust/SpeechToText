package com.spinningnoodle.interaction.speechtotext.capture;

import com.sun.media.sound.WaveFileReader;

import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Created by Freddy on 4/15/2015.
 * Interface that provides audio
 */

public interface AudioProvider {
    void start();
    void stop();
    File capture();
}
