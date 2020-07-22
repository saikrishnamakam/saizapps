package com.sai.app.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;

public class ImageViewDialog extends DialogFragment {

	private static final String ARG_IMAGE = "image";
	private static final String ARG_TITLE = "title";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		File imageFile = (File) getArguments().getSerializable(ARG_IMAGE);
		String crimeTitle = (String) getArguments().getString(ARG_TITLE);

		super.onCreateDialog(savedInstanceState);

		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_image, null);

		ImageView imageView = (ImageView) view.findViewById(R.id.dialog_image_view);

		if(imageFile.exists()) {

			Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
			imageView.setImageBitmap(myBitmap);
		}
		return new AlertDialog.Builder(getActivity()).setView(view).setTitle(crimeTitle).create();
	}

	@Override
	public void onResume() {

		super.onResume();
		getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 800);
	}

	public static ImageViewDialog newInstance(File photoFile, String crimeTitle) {

		Bundle args = new Bundle();
		args.putSerializable(ARG_IMAGE, photoFile);
		args.putString(ARG_TITLE, crimeTitle);

		ImageViewDialog fragment = new ImageViewDialog();
		fragment.setArguments(args);
		return fragment;
	}
}
