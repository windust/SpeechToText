package com.spinningnoodle.interaction.speechtotext.capture;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Freddy on 4/15/2015.
 * Provides with audio capture
 * Using Mic
 */

public class MicAudioProvider implements AudioProvider {
    public static final AudioFormat DEFAULT_FORMAT = new AudioFormat(8000f, 16, 1, true, false);
    private final Settings settings;
    int secondsToCapture = 5;
    boolean started = false;
    TargetDataLine line;

    public MicAudioProvider(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void start() {
        // set line
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                settings.format); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) {
            // Handle the error ...
            throw new IllegalArgumentException("Audio Line "+settings.format+" is not supported");
        }

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(settings.format);
        } catch (LineUnavailableException ex) {
            throw new RuntimeException("Audio Line "+settings.format+" is not available",ex);
        }

        started = true;
    }

    @Override
    public void stop() {
        started = false;
        if (line.isRunning()) line.stop();
        if (line.isOpen()) line.close();
    }

    @Override
    public File capture() {
        switch (settings.type) {
            case FIXED_TIME:
                return captureAtFixedTime(settings.secondsToCapture);
            case NOISE_THRESHOLD:
                return captureAtNoiseThreshold();
        }
        return null;
    }

    private enum State {AWATING,LISTENING}

    private File captureAtNoiseThreshold() {
        line.start();

        int bufferSize = (int) settings.format.getSampleRate() *
                settings.format.getFrameSize() / 10;
        byte buffer[] = new byte[bufferSize];
        boolean shouldListen = true;
        State state = State.AWATING;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize * 10); // at least 10 seconds.
        System.out.println("Awaiting");
        int quietSampleCount = 0;
        LinkedList<byte[]> prebufferList = new LinkedList<>();
        double threshold = ((double) Short.MAX_VALUE) * settings.threshold;
        while (shouldListen) {
            long averageVolume = 0;
            int sampleSize=0;

            int count = line.read(buffer, 0, buffer.length);
            if (count > 0) {
                ByteBuffer wrap = ByteBuffer.wrap(buffer);
                wrap.order(settings.format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
                ShortBuffer shortBuffer = wrap.asShortBuffer();
                for (int i =0;i < buffer.length/2;i++) {
                    short newSample = shortBuffer.get(i);
                    averageVolume += Math.abs(newSample);
                    sampleSize++;
                }
                averageVolume /= sampleSize;
//                System.out.println("Vol:" + averageVolume + " on " + sampleSize + " threshold " + threshold);
                switch (state) {
                    case AWATING:
                        prebufferList.add(buffer.clone());
                        if (prebufferList.size() > 10) prebufferList.remove(0);
                        if (averageVolume > threshold) {
//                            System.out.println("Listening...");
                            state = State.LISTENING;
                            for (byte[] prebuffer : prebufferList) {
                                baos.write(prebuffer,0,prebuffer.length);
                            }
                        }
                        break;
                    case LISTENING:
                        if (averageVolume < threshold) {
                            quietSampleCount+=buffer.length/2;
                        } else {
                            quietSampleCount = 0;
                        }
                        if (quietSampleCount > settings.format.getSampleRate()) {
                            shouldListen = false;
                        }
                        baos.write(buffer,0,count);
                }
            }
        }
        line.stop();
        byte[] buf = baos.toByteArray();
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(buf), settings.format, buf.length*8/settings.format.getSampleSizeInBits());
        File file = getFile();
        try {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't process Wave File",e);
        }
        return file;
    }

    private File getFile() {
        File outFile = settings.captureFileName;
        if (outFile == null) {

            try {
                outFile = File.createTempFile("MicAudioProvider", ".wav");
            } catch (IOException e) {
                throw new RuntimeException("Couldn't create temp file ", e);
            }
        }
        return outFile;
    }

    private File captureAtFixedTime(double secondsToCapture) {
        File outFile = getFile() ;
        Timer timer = new Timer("CaptureTimer",true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                line.stop();
            }
        }, (long) (secondsToCapture * 1000));

        line.start();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            AudioSystem.write(new AudioInputStream(line),
                    AudioFileFormat.Type.WAVE, outFile);
        } catch (IOException e) {
            throw new RuntimeException("Exception while recording",e);
        }
        return outFile;
    }

    public static class Settings {
        final CaptureType type;
        final Double secondsToCapture;
        final Double threshold;
        final AudioFormat format;
        final File captureFileName;

        public Settings(CaptureType type, Double secondsToCapture, Double threshold, AudioFormat format, File captureFileName) {
            this.type = type;
            this.secondsToCapture = secondsToCapture;
            this.format = format;
            this.captureFileName = captureFileName;
            this.threshold = threshold;
        }
    }

    public enum CaptureType {
        FIXED_TIME, NOISE_THRESHOLD
    }

}
