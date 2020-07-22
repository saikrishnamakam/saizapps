package com.sai.app.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhotoGalleryFragment extends Fragment {

	private static final String TAG = "PhotoGalleryFragment";

	private RecyclerView mPhotoRecyclerView;
	private GridLayoutManager mGridManager;
	private List<GalleryItem> mItems = new ArrayList<>();
	private Set<Integer> mCacheEntries = new HashSet<>();
	private AsyncTask<Void, Void, List<GalleryItem>> mAsyncTask;
	private ThumbnailDownloader<Integer> mThumbnailDownloader;
	private int pageFetched = 1;
	boolean asyncFetching = false;
	int mCurrentPage = 1;
	int mMaxPage = 1;
	int mItemsPerPage = 1;
	int mFirstItemPosition, mLastItemPosition;


	public static PhotoGalleryFragment newInstance() {

		return new PhotoGalleryFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);

		Log.d(TAG, "On Create");
		mAsyncTask = updateItems();

		Handler responseHandler = new Handler();
		mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
		mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<Integer>() {

			@Override
			public void onThumbnailDownloaded(Integer position, Bitmap bitmap) {

				mPhotoRecyclerView.getAdapter().notifyItemChanged(position);
			}
		});

		mThumbnailDownloader.start();
		mThumbnailDownloader.getLooper();
		Log.i(TAG, "Background thread started");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
		mPhotoRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL));
		mPhotoRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

		/*
		 Added GlobalLayoutListener because we want to want to calculate the
		 no of columns from Recycler view's width but recycler view still not
		 get the size(now it's size is zero). So I setted GlobalLayoutListener
		 to recycler view so that when the visibility of views within the view
		 tree changes (i.e. here when recycler view gets size) the method
		 onGlobalLayout() is called, & we can get the recycler view width size
		 & can calculate the no. of columns.
		 */
		mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {

				float columnWidthInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, getActivity().getResources().getDisplayMetrics());
				int width = mPhotoRecyclerView.getWidth(); //returns width of view in pixels
				int noOfColumns = Math.round(width / columnWidthInPixels);
				Log.i("ViewTreeObserver", "Recycler View Changed");
				mGridManager = new GridLayoutManager(getActivity(), noOfColumns);
				mPhotoRecyclerView.setLayoutManager(mGridManager);
				mPhotoRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				setupAdapter();
			}
		});

		mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

				int lastVisibleItem = mGridManager.findLastVisibleItemPosition();
				int firstVisibleItem = mGridManager.findFirstVisibleItemPosition();

				if((mLastItemPosition != lastVisibleItem) || (mFirstItemPosition != firstVisibleItem)) {

					mLastItemPosition = lastVisibleItem;
					mFirstItemPosition = firstVisibleItem;
					int begin = Math.max(firstVisibleItem - 10, 0);
					int end = Math.min(lastVisibleItem + 10, mItems.size() - 1);

					for(int position = begin; position <= end; position++) {

						String url = mItems.get(position).getUrl();

						if(url == null) {
							continue;
						} else if(mThumbnailDownloader.mPhotoCache.get(url) == null && !mCacheEntries.contains(position)) {

							mCacheEntries.add(position);
							mThumbnailDownloader.queueThumbnail(position, url);
						}
					}
				}

				if(!(asyncFetching) && (dy > 0) && (mCurrentPage < mMaxPage) && (lastVisibleItem >= mItems.size() - 1)) {
					Log.d(TAG, "On Scrolled");
					updateItems();
				} else {
					mCurrentPage = firstVisibleItem / mItemsPerPage + 1;
				}
			}
		});

		return v;
	}

	private void setupAdapter() {

		if(isAdded()) {
			mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
		}
	}

	@Override
	public void onPause() {

		super.onPause();
		Log.d(TAG, "on Pause");
	}

	@Override
	public void onResume() {

		super.onResume();
		Log.d(TAG, "on Resume");
	}

	@Override
	public void onStop() {

		super.onStop();
		Log.d(TAG, "on Stop");
	}

	@Override
	public void onDestroyView() {

		super.onDestroyView();
		mThumbnailDownloader.clearQueue();
		mThumbnailDownloader.clearCache();
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		mAsyncTask.cancel(true);
		mThumbnailDownloader.quit();

		Log.i(TAG, "Background thread destroyed");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {

		super.onCreateOptionsMenu(menu, menuInflater);
		menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

		MenuItem searchItem = menu.findItem(R.id.menu_item_search);
		final SearchView searchView = (SearchView) searchItem.getActionView();

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String s) {

				Log.d(TAG, "QueryTextSubmit:" + s);
				QueryPreferences.setStoredQuery(getActivity(), s);
				initializeResources();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String s) {

				Log.d(TAG, "QueryTextChange:" + s);
				return false;
			}
		});

		searchView.setOnSearchClickListener( new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String query = QueryPreferences.getStoredQuery(getActivity());
				searchView.setQuery(query, false);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
			case R.id.menu_item_clear:
				QueryPreferences.setStoredQuery(getActivity(), null);
				initializeResources();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void initializeResources() {

		mThumbnailDownloader.clearQueue();
		mThumbnailDownloader.clearCache();
		mItems = new ArrayList<>();
		mCacheEntries = new HashSet<>();
		pageFetched = 1;
		updateItems();
		setupAdapter();
	}

	private AsyncTask<Void, Void, List<GalleryItem>> updateItems() {

		Log.d(TAG, "updateItems called");
		String query = QueryPreferences.getStoredQuery(getActivity());
		Log.d(TAG, "query:" + query);
		return new FetchItemsTask(query).execute();
	}

	private class PhotoHolder extends RecyclerView.ViewHolder {

		private ImageView mItemImageView;

		public PhotoHolder(View itemView) {

			super(itemView);

			mItemImageView = (ImageView) itemView
					.findViewById(R.id.fragment_photo_gallery_image_view);
		}

		public void bindDrawable(Drawable drawable) {
			mItemImageView.setImageDrawable(drawable);
		}
	}

	private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

		private List<GalleryItem> mGalleryItems;

		public PhotoAdapter(List<GalleryItem> galleryItems) {

			mGalleryItems = galleryItems;
		}

		@Override
		public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
			return new PhotoHolder(view);
		}

		@Override
		public void onBindViewHolder(PhotoHolder photoHolder, int position) {

			Bitmap bitmap = null;
			GalleryItem galleyItem = mGalleryItems.get(position);
			String url = galleyItem.getUrl();

			if(url != null) {
				bitmap = mThumbnailDownloader.mPhotoCache.get(url);
			}

			if(bitmap == null) {
				Drawable placeHolder = getResources().getDrawable(R.drawable.no_image);
				photoHolder.bindDrawable(placeHolder);
			} else {
				Drawable drawable = new BitmapDrawable(getResources(), bitmap);
				photoHolder.bindDrawable(drawable);
			}
		}

		@Override
		public int getItemCount() {
			return mGalleryItems.size();
		}
	}

	public class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

		private String mQuery;

		public FetchItemsTask(String query) {

			mQuery = query;
		}

		@Override
		protected List<GalleryItem> doInBackground(Void... params) {

			asyncFetching = true;

			if(mQuery == null) {
				return new FlickrFetchr().fetchRecentPhotos(pageFetched);
			} else {
				return new FlickrFetchr().searchPhotos(pageFetched, mQuery);
			}
		}

		@Override
		protected void onPostExecute(List<GalleryItem> items) {

			pageFetched++;
			asyncFetching = false;
			mItems.addAll(items);
			GalleryPage page = GalleryPage.getGalleryPage();
			mMaxPage = page.getTotalPages();
			mItemsPerPage = page.getItemPerpage();

			if(mPhotoRecyclerView.getAdapter() == null) {
				setupAdapter();
			}

			mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
		}
	}
}
