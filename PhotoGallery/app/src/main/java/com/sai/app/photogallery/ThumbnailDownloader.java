package com.sai.app.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ThumbnailDownloader<T> extends HandlerThread {

	private static final String TAG = "ThumbnailDownloader";
	private static final int MESSAGE_DOWNLOAD = 0;

	private boolean mHasQuit = false;
	private Handler mRequestHandler;
	private Handler mResponseHandler;
	private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();
	private ThumbnailDownloadListener<T> mTThumbnailDownloadListener;
	public LruCache<String, Bitmap> mPhotoCache;
	private Bitmap mBitmap1;

	public interface ThumbnailDownloadListener<T> {

		void onThumbnailDownloaded(T target, Bitmap thumbnail);
	}

	public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {

		mTThumbnailDownloadListener = listener;
	}

	public ThumbnailDownloader(Handler responseHandler) {

		super(TAG);
		mResponseHandler = responseHandler;

		int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
		int cacheSize = maxMemory / 8;
		mPhotoCache = new LruCache<String, Bitmap>(cacheSize) {

			@Override
			protected int sizeOf(String url, Bitmap bitmap) {

				return  (int) (bitmap.getByteCount() / 1024);
			}
		};
	}

	@Override
	protected void onLooperPrepared() {

		mRequestHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if(msg.what == MESSAGE_DOWNLOAD) {
					T target = (T) msg.obj;
					Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
					Log.i("UrlCheck", "getting from Map: " + "target:" + target + " url:" + mRequestMap.get(target));
					handleRequest(target);
				}
			}
		};
	}

	@Override
	public boolean quit() {

		mHasQuit = true;
		return super.quit();
	}

	public void queueThumbnail(T target, String url) {

		//Log.i(TAG, "Got a URL: " + url);

		if(url == null) {
			mRequestMap.remove(target);
		} else {

		//	Log.i("UrlCheck", "Putting in Map: " + "target:" + target + " url:" + url);
			mRequestMap.put(target, url);
			mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
		}
	}

	public void clearQueue() {

		mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
		mRequestMap.clear();
	}

	public void clearCache() {

		mPhotoCache.evictAll();
	}

	private void handleRequest(final T target) {

		try {
			final String url = mRequestMap.get(target);

			if(url == null) {
				return;
			}

			byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);

			final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
			Log.i(TAG, "Bitmap created");
			Bitmap mBitmap1 = mPhotoCache.put(url, bitmap);
			Log.i(TAG, "val:" + mPhotoCache.get(url) + "size: " + mPhotoCache.size() + "ele:" + mPhotoCache + "b1:" + mBitmap1);

			mResponseHandler.post(new Runnable() {

				@Override
				public void run() {

					if(mRequestMap.get(target) != url || mHasQuit) {
						return;
					}

					mRequestMap.remove(target);
					mTThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
				}
			});
		} catch(IOException ioe) {
			Log.e(TAG, "Error downloading image", ioe);
		}
	}
}
