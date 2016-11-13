package eu.jakubtudruj.networking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import static java.lang.System.in;

public class MainActivity extends AppCompatActivity {

    ImageView mainImageView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mainImageView = (ImageView) findViewById(R.id.img);
        new DownloadImageTask().execute("http://image.shutterstock.com/z/stock-photo-example-stamp-123670585.jpg");
    }

    private InputStream openHttpConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        InputStream inputStream = null;
        int responseCode = -1;
        URLConnection connection = url.openConnection();

        if (!(connection instanceof HttpURLConnection)) {
            throw new IOException("Not a http connection");
        }

        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            httpURLConnection.setAllowUserInteraction(false);
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            responseCode = httpURLConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
            }
        } catch (ProtocolException e) {
            Log.e("Networking", e.getLocalizedMessage());
            throw new IOException("Error connection");
        } catch (IOException e) {
            Log.e("Networking", e.getLocalizedMessage());
            throw new IOException("Error connection");
        }

        return inputStream;
    }

    private Bitmap downloadImage(String urlString) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = this.openHttpConnection(urlString);
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            Log.e("Downloading Bitmap", e.getLocalizedMessage());
        }
        return bitmap;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            return MainActivity.this.downloadImage(urls[0]);
        }

        protected void onPostExecute(Bitmap result) {
            MainActivity.this.mainImageView.setImageBitmap(result);
        }
    }
}