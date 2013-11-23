package com.projet.awssdk;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {
    public static InputStream download(String url, FoundCallback<String> callback) {
        try {
            URL myUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);

            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode >= 400 && responseCode < 600) {
                return new ByteArrayInputStream((responseCode + " error").getBytes());
            }

            InputStream inputStream = connection.getInputStream();

            if (callback != null && connection.getHeaderField("Content-Type").startsWith("text/html")) {
                processHtml(download(url, null), callback);
            }

            return inputStream;
        } catch (MalformedURLException e) {
            return new ByteArrayInputStream(("Malformed URL " + url).getBytes());
        } catch (Exception e) {
            return new ByteArrayInputStream(("Exception " + url).getBytes());
        }
    }

    private static void processHtml(InputStream inputStream, FoundCallback<String> callback) throws IOException {
        String regex = "\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(regex);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                callback.found(matcher.group());
            }
        }
    }

    public static interface FoundCallback<F> {
        void found(F element);
    }
}