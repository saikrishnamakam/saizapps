package com.sai.app.photogallery;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FlickrFetchr {

	int page = 0;

	private static final String TAG = "FlickrFetchr";

	private static final String API_KEY = "23c0dd84db1c280fe3f0666f60d40caa";
	private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
	private static final String SEARCH_METHOD = "flickr.photos.search";
	private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
			.buildUpon()
			.appendQueryParameter("api_key", API_KEY)
			.appendQueryParameter("format", "json")
			.appendQueryParameter("nojsoncallback", "1")
			.appendQueryParameter("extras", "url_s")
			.build();


	public byte[] getUrlBytes(String urlSpec) throws IOException {

		URL url = new URL(urlSpec);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();

			if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {

				throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
			}

			int bytesRead = 0;
			byte[] buffer = new byte[1024];

			while((bytesRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			return out.toByteArray();
		} finally {
			connection.disconnect();
		}
	}

	public String getUrlString(String urlSpec) throws IOException {

		return new String(getUrlBytes(urlSpec));
	}

	public List<GalleryItem> fetchRecentPhotos(int page) {

		String url = buildUrl(page, FETCH_RECENTS_METHOD, null);
		return downloadGalleryItems(url);
	}

	public List<GalleryItem> searchPhotos(int page, String query) {

		String url = buildUrl(page, SEARCH_METHOD, query);
		return downloadGalleryItems(url);
	}

	private List<GalleryItem> downloadGalleryItems(String url) {

		List<GalleryItem> items = new ArrayList<>();

		try {

			this.page = page;
			String jsonString = getUrlString(url);
			Log.e(TAG, "Received JSON: " + jsonString);

			Gson gson = new Gson();
			GalleryPage.getGalleryPage();
			GalleryPage.sGalleryPage = gson.fromJson(jsonString, GalleryPage.class);
			items = GalleryPage.sGalleryPage.getGalleryItemList();
			Log.i(TAG, "Size: " + items.size());
		} catch(IOException ioe) {
			Log.e(TAG, "Failed to fetch Items", ioe);
		}

		return items;
	}

	private String buildUrl(int page, String method, String query) {

		Uri.Builder uriBuilder = ENDPOINT.buildUpon().appendQueryParameter("method", method)
				.appendQueryParameter("page", Integer.toString(page));

		if(method.equals(SEARCH_METHOD)) {

			uriBuilder.appendQueryParameter("text", query);
		}

		return uriBuilder.build().toString();
	}
/*
	private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {

		JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
		JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

		for(int i = 0; i < photoJsonArray.length(); i++) {
			JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

			GalleryItem item = new GalleryItem();
			item.setId(photoJsonObject.getString("id"));
			item.setCaption(photoJsonObject.getString("title"));

			if(!photoJsonObject.has("url_s")) {
				continue;
			}

			item.setUrl(photoJsonObject.getString("url_s"));
			items.add(item);
		} */
}
