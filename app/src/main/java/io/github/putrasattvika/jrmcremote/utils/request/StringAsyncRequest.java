package io.github.putrasattvika.jrmcremote.utils.request;

import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sattvika on 14-Jan-17.
 */

public class StringAsyncRequest extends AsyncTask<URL, Void, List<String>> {
    @Override
    protected List<String> doInBackground(URL... urls) {
        List<String> strings = new ArrayList<>();

        for (int i = 0; i < urls.length; i++) {
            InputStream in = null;
            HttpURLConnection urlConnection;

            try{
                urlConnection = (HttpURLConnection) urls[i].openConnection();

                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            StringWriter stringWriter = new StringWriter();
            try {
                IOUtils.copy(in, stringWriter, "UTF-8");
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignore) {}

            strings.add(stringWriter.toString());
        }

        return strings;
    }
}
