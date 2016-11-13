package eu.jakubtudruj.networking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static java.lang.System.in;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private InputStream openHttpConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        InputStream inputStream = null;
        int responseConde = -1;
        URLConnection connection = url.openConnection();

        if(!(connection instanceof  HttpURLConnection)) {
            throw new IOException("Not a http connection");

            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
                httpURLConnection.setAllowUserInteraction(false);
                httpURLConnection.setInstanceFollowRedirects(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                responseCode = httpURLConnection.getResponseCode();

                if(responseConde == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
            } catch (Exception e) {
                Log.e("Networking", e.getLocalizedMessage());
                throw IOException("Error connection");
            }
        }

        return inputStream
}
