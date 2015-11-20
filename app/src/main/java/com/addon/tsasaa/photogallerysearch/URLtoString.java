package com.addon.tsasaa.photogallerysearch;

import android.net.Uri;
import android.util.Log;
import android.widget.Gallery;
// key: 31a593638a4d34109a4e532eb371d42f secret: 47ca4d7f970a0a3c
// https://api.flickr.com/services/?method=flickr.photos.getRecent&api_key=xxx&format=json&nojsoncallback=1
// https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=31a593638a4d34109a4e532eb371d42f&format=json&nojsoncallback=1
// https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=31a593638a4d34109a4e532eb371d42f&format=json&nojsoncallback=1&extras=url_s

// Search
// https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=xxx&format=json&nojsoncallback=1&text=cat
// https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=31a593638a4d34109a4e532eb371d42f&format=json&nojsoncallback=1&text=cat

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class URLtoString {

    private static final String TAG = "URLFetchr";

    private static final String API_KEY = "31a593638a4d34109a4e532eb371d42f";
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final String urlLarge = "url_s";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)                   // appendQP escape query string for us
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", urlLarge)                    // small version of the picture
            //.appendQueryParameter("text", "sexy asian")
            .build();

    public byte[] getUrlBytes(String urlArg) throws IOException { // fetches raw data from a URL -> array of bytes
        URL url = new URL(urlArg);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlArg);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();

        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String url) throws IOException{ // converts the result from getUrlBytes to a String
        //String str = new String(getUrlBytes(url));
        return new String(getUrlBytes(url));
    }

    public List<GalleryItem> fetchRecentPhotos() {
        String url = buildUrl(FETCH_RECENT_METHOD, null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query) {
        String url = buildUrl(SEARCH_METHOD, query);
        //Log.e(TAG, url);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();

        try {
            String jsonString = getUrlString(url);                              // Converting URL to String
            //Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);                   // parses the JSON string -> object hierarchy
            // JSONObj(photos) -> JSONObj(photo) -> JSONArray(index)
            parseItem(items, jsonBody);                                 // parse to list<GalleryItem>
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }

    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }

    private void parseItem(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject detailObj = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(detailObj.getString("id"));
            item.setCaption(detailObj.getString("title"));

            if (!detailObj.has(urlLarge)) {
                continue;                           // doesn't always return a url_s for each image
            }

            item.setUrl(detailObj.getString(urlLarge));
            items.add(item);
        }
    }
}