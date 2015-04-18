package com.spinningnoodle.interaction.speechtotext.engine;

/**
 * Created by Freddy on 4/15/2015.
 */
public class TranslationResult {
    private final String text;
    private final double confidence;

    public TranslationResult(String text, double confidence) {
        this.text = text;
        this.confidence = confidence;
    }

    public String getText() {
        return text;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "TranslationResult{" +
                "text='" + text + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
