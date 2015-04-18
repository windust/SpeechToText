package com.spinningnoodle.interaction.speechtotext.engine.google;

import com.spinningnoodle.interaction.speechtotext.engine.SpeechToTextProvider;
import com.spinningnoodle.interaction.speechtotext.engine.TranslationResult;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.xml.ws.http.HTTPException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Provider for using Google API Service
 */


public class GoogleSpeechToTextProvider implements SpeechToTextProvider {
    private String googleSpeechURL = "https://www.google.com/speech-api/v2/recognize";
    private final String apiKey;
    private String language="auto";

    public GoogleSpeechToTextProvider(String apiKey) {
        this.apiKey = apiKey;
    }


    @Override
    public void start() {
        // nothing
    }

    @Override
    public void stop() {
        // nothing
    }

    public List<TranslationResult> transcribe(File file) {
        try {
            StringBuilder sb = new StringBuilder(googleSpeechURL);
            sb.append("?output=json");
            sb.append("&client=chromium");
            sb.append("&lang=").append(language);
            sb.append("&key=").append(apiKey);

            HttpsURLConnection httpConn = null;

            URL url = new URL(sb.toString());
            URLConnection urlConn = url.openConnection();
            if (!(urlConn instanceof HttpsURLConnection)) {
                throw new IOException("URL must be HTTPS");
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = stream.getFormat();

            httpConn = (HttpsURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setChunkedStreamingMode(0); //TransferType: chunked
            httpConn.setRequestProperty("Content-Type", "audio/l"+format.getSampleSizeInBits()+"; rate=" + (int) format.getSampleRate());
            // this opens a connection, then sends POST & headers.
            OutputStream out = httpConn.getOutputStream();

            IOUtils.copy(stream, out);
            out.close();


            int resCode = httpConn.getResponseCode();
            if (resCode >= HttpURLConnection.HTTP_UNAUTHORIZED) {//Stops here if Google doesn't like us/
                throw new HTTPException(HttpURLConnection.HTTP_UNAUTHORIZED);//Throws
            }

            StringWriter writer = new StringWriter();
            IOUtils.copy(httpConn.getInputStream(), writer);
            return parseResponse(writer.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error when getting Google Result",e);
        }
    }

    private List<TranslationResult> parseResponse(String json) throws JSONException {
        List<TranslationResult> results = new ArrayList<TranslationResult>();

        StringTokenizer tokenizer = new StringTokenizer(json,"\n");
        while (tokenizer.hasMoreElements()) {
            String element = tokenizer.nextElement().toString();
            JSONArray result = new JSONObject(element).getJSONArray("result");
            if (result.length() == 0) continue;
            JSONArray alternative = result.getJSONObject(0).getJSONArray("alternative");
            int length = alternative.length();
            double confidence = 1d;
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = alternative.getJSONObject(i);
                String transcript = jsonObject.getString("transcript");
                if (jsonObject.has("confidence")) {
                    confidence = jsonObject.getDouble("confidence");
                }
                results.add(new TranslationResult(transcript, confidence));
            }
        }
        return results;
    }

    public String getGoogleSpeechURL() {
        return googleSpeechURL;
    }

    public void setGoogleSpeechURL(String googleSpeechURL) {
        this.googleSpeechURL = googleSpeechURL;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
