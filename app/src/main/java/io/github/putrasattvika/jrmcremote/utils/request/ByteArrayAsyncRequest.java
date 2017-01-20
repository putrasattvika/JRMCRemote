package io.github.putrasattvika.jrmcremote.utils.request;

import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sattvika on 14-Jan-17.
 */

public class ByteArrayAsyncRequest extends AsyncTask<URL, Void, List<ByteArrayInputStream>> {
    @Override
    protected List<ByteArrayInputStream> doInBackground(URL... urls) {
        List<ByteArrayInputStream> byteArrs = new ArrayList<>();

        for (int i = 0; i < urls.length; i++) {
            InputStream in = null;
            HttpURLConnection urlConnection;

            try{
                urlConnection = (HttpURLConnection) urls[i].openConnection();

                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (IOException ignore) {}

            try {
                byteArrs.add(new ByteArrayInputStream(IOUtils.toByteArray(in)));
            } catch (NullPointerException | IOException e) {
                byteArrs.add(null);
            }
        }

        return byteArrs;
    }
}
