package com.example.go4lunch.data;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class DownloadUrl {

    public String readUrl(String urlToRead) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlToRead);
            urlConnection = (HttpURLConnection) url.openConnection();   // Creating http connection to communicate with url
            urlConnection.connect();                                    // Connecting to url
            iStream = urlConnection.getInputStream();                   // Reading data from url

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null)
                sb.append(line);

            data = sb.toString();
            Log.d("URL", "readUrl: " + data);
            br.close();
        }
        catch (Exception e) {
            Log.d("Exception", "readUrl: " + e.getMessage());
        }
        finally {
            Objects.requireNonNull(iStream).close();
            urlConnection.disconnect();
        }

        return data;
    }
}
