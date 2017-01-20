package io.github.putrasattvika.jrmcremote.utils.request;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Sattvika on 17-Jan-17.
 */

public class ResourceRequestUtil {
    public static Bitmap requestBitmap(URL url) throws ExecutionException, InterruptedException {
        List<ByteArrayInputStream> bitmapInputStreams = null;
        Bitmap bitmap = null;

        try {
            ByteArrayAsyncRequest asyncRequest = new ByteArrayAsyncRequest();
            bitmapInputStreams = asyncRequest.execute(url).get(2000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ignore) {}

        try {
            bitmap = BitmapFactory.decodeStream(bitmapInputStreams.get(0));
        } catch (Exception ignore) {}

        return bitmap;
    }

    public static Document requestDocument(URL url) throws ExecutionException, InterruptedException {
        try {
            StringAsyncRequest asyncRequest = new StringAsyncRequest();
            List<String> documentStrings = asyncRequest.execute(url).get();

            Document doc = null;

            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                doc = dBuilder.parse(new InputSource(new StringReader(documentStrings.get(0))));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return doc;
        } catch (Exception e) {
            return null;
        }
    }

    public static String requestString(URL url) throws ExecutionException, InterruptedException {
        try {
            StringAsyncRequest asyncRequest = new StringAsyncRequest();
            List<String> strings = asyncRequest.execute(url).get();

            return strings.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public static void request(URL url) throws ExecutionException, InterruptedException {
        try {
            StringAsyncRequest asyncRequest = new StringAsyncRequest();
            asyncRequest.execute(url);
        } catch (Exception ignore) {}
    }
}
