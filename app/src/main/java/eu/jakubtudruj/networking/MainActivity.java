package eu.jakubtudruj.networking;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static java.lang.System.in;

public class MainActivity extends AppCompatActivity {

    ImageView mainImageView;
    TextView myTextView;
    EditText wordEditText;
    ProgressBar progressBar;
    final static int BUFFER_SIZE = 2048;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mainImageView = (ImageView) findViewById(R.id.img);
        this.myTextView = (TextView) findViewById(R.id.myTextView);
        this.wordEditText = (EditText) findViewById(R.id.wordEditText);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.progressBar.setVisibility(View.INVISIBLE);

        new DownloadImageTask().execute("http://st.depositphotos.com/1023799/2906/v/950/depositphotos_29066941-stock-illustration-grunge-example-rubber-stamp-vector.jpg");

        this.myTextView.setText("Tutaj pojawią się definicje wyszukiwanego słowa.\nWpisz wyszukiwane słowo w piniższe pole i naciśnij przycisk SZUKAJ");
    }

    void findDefinitionButtonOnClick(View view) {
        this.showProgressIndicator();
        this.hideKeyboard();

        String word = this.wordEditText.getText().toString();
        new DownloadDefinitionTask().execute(word);
    }

    void showProgressIndicator() {
        this.progressBar.setVisibility(View.VISIBLE);
    }

    void dismissProgressIndicator() {
        this.progressBar.setVisibility(View.INVISIBLE);
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.wordEditText.getWindowToken(), 0);
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

    private String downloadText(String urlString) {
        String text = "";
        InputStream inputStream = null;
        try {
            inputStream = this.openHttpConnection(urlString);
            InputStreamReader isr = new InputStreamReader(inputStream);
            char[] inputBuffer = new char[BUFFER_SIZE];
            int charRead;
            while ((charRead = isr.read(inputBuffer))>0) {
                //convert chars to a String
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                text += readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
            inputStream.close();
        } catch (Exception e) {
            Log.e("Downloading text", e.getLocalizedMessage());
        }
        return text;
    }

    private class DownloadTextTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return MainActivity.this.downloadText(urls[0]);
        }

        protected void onPostExecute(String result) {
            MainActivity.this.myTextView.setText(result);
        }
    }

    /*
    * Word definitions
    */

    private String wordDefinition(String word) {
        InputStream inputStream = null;
        String definition = "";

        try {
            inputStream = openHttpConnection("http://services.aonaware.com/DictService/DictService.asmx/Define?word=" + word);
            Document document = null;
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder;
            try {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
                document = documentBuilder.parse(inputStream);
            } catch (Throwable t) {
                Log.e("document builder", t.getLocalizedMessage());

            }
            NodeList definitionElements = document.getElementsByTagName("Definition");
            for(int i=0; i< definitionElements.getLength(); i++) {
                Node item = definitionElements.item(i);
                if(item.getNodeType() == Node.ELEMENT_NODE) {
                    Element definitionElement = (Element) item;
                    NodeList wordDefinitionElements = definitionElement.getElementsByTagName("WordDefinition");

                    definition = "";
                    for (int j=0; j<wordDefinitionElements.getLength(); j++) {
                        Element wordDefElement = (Element) wordDefinitionElements.item(j);
                        NodeList list = ((Node) wordDefElement).getChildNodes();
                        definition += ((Node) list.item(0)).getNodeValue() + "\n\n";
                    }
                }
            }
            document.getDocumentElement().normalize();
        } catch (Throwable t) {
            Log.e("document problem", t.getLocalizedMessage());
        }
        return definition;
    }

    private class DownloadDefinitionTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return MainActivity.this.wordDefinition(urls[0]);
        }

        protected void onPostExecute(String result) {
            MainActivity.this.myTextView.setText(result);
            MainActivity.this.dismissProgressIndicator();
        }
    }
}