package com.sai.app.photogallery;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GalleryPage {

	PhotoPages photos;
	String stat;

	public static GalleryPage sGalleryPage;

	private GalleryPage() {}

	public static GalleryPage getGalleryPage() {

		if(sGalleryPage == null) {

			sGalleryPage = new GalleryPage();
		}
		return sGalleryPage;
	}

	public List<GalleryItem> getGalleryItemList() {

		return photos.getPhotoList();
	}

	public int getTotalPages() {

		return photos.pages;
	}

	public int getItemPerpage() {

		return photos.perpage;
	}
}

class PhotoPages {

	int page;
	int pages;
	int perpage;
	int total;

	@SerializedName("photo")
	List<GalleryItem> photoList;

	List<GalleryItem> getPhotoList() {

		return photoList;
	}
}
