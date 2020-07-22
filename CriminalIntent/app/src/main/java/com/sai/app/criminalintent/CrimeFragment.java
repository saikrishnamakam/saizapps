package com.sai.app.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.text.TextWatcher;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.text.Editable;
import android.view.Menu;
import android.provider.ContactsContract;
import android.net.Uri;
import android.database.Cursor;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class CrimeFragment extends Fragment {

	private static final String ARG_CRIME_ID = "crime_id";
	private static final String DIALOG_DATE = "DialogDate";
	private static final String DIALOG_IMAGE = "DialogImage";
	private static final String ARG_TIME = "time";
	private static final String HIDE_DELETE_BUTTON = "com.sai.app.criminalintent.hide_delete_button";
	private static final int REQUEST_DATE = 0;
	private static final int REQUEST_TIME = 1;
	private static final int REQUEST_CONTACT = 2;
	private static final int REQUEST_PHOTO = 3;

	private Crime mCrime;
	private EditText mTitleField;
	private Button mDateButton;
	private Button mTimeButton;
	private CheckBox mSolvedCheckBox;
	private Button mReportButton;
	private Button mSuspectButton;
	private Button mCallButton;
	private ImageButton mPhotoButton;
	private ImageView mPhotoView;
	private File mPhotoFile;
	private boolean isCallVisible;
	Intent callSuspect;
	private Callbacks mCallbacks;

	public interface Callbacks {

		void onCrimeUpdated(Crime crime);
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
		mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
		mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);

		if(!(boolean) getArguments().getBoolean(HIDE_DELETE_BUTTON))
			setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstaneceState) {

		View v = inflater.inflate(R.layout.crime_fragment, container, false);

		mTitleField = (EditText) v.findViewById(R.id.crime_title);
		mTitleField.setText(mCrime.getTitle());
		mTitleField.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				mCrime.setTitle(s.toString());
				updateCrime(mCrime);
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mDateButton = (Button) v.findViewById(R.id.crime_date);
		mTimeButton = (Button) v.findViewById(R.id.crime_time);
		updateDate();
		updateTime();

		mDateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				FragmentManager manager = getActivity().getSupportFragmentManager();
				DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
				dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
				dialog.show(manager, DIALOG_DATE);
			}
		});

		mTimeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = TimePickerFragment.newIntent(getActivity(), mCrime.getId());
				startActivityForResult(intent, REQUEST_TIME);
			}
		});

		mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
		mSolvedCheckBox.setChecked(mCrime.isSolved());
		mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				mCrime.setSolved(isChecked);
				updateCrime(mCrime);
			}
		});

		mReportButton = (Button) v.findViewById(R.id.crime_report);
		mReportButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
				i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
				i = i.createChooser(i, getString(R.string.send_report));
				startActivity(i);
			}
		});

		final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
		callSuspect = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mCrime.getPhoneNumber()));

		mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
		mCallButton = (Button) v.findViewById(R.id.crime_call);
		updateSuspect();

		PackageManager packageManager = getActivity().getPackageManager();

		if(packageManager.resolveActivity(callSuspect, PackageManager.MATCH_DEFAULT_ONLY) != null)
			isCallVisible = true;

		if(mCrime.getSuspect() != null) {

			if(isCallVisible) {
				isCallVisible = true;
				mCallButton.setEnabled(true);
			} else
				mCallButton.setEnabled(false);
		} else {
			mCallButton.setEnabled(false);
		}

		if(packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
			mSuspectButton.setEnabled(false);
		}

		mSuspectButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivityForResult(pickContact, REQUEST_CONTACT);
			}
		});

		mCallButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				callSuspect.setData(Uri.parse("tel:" + mCrime.getPhoneNumber()));
				callPhoneNumber();
			}
		});

		mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
		final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
		mPhotoButton.setEnabled(canTakePhoto);

		if(canTakePhoto) {

			//Uri uri = Uri.fromFile(mPhotoFile); => This line is fine if our app's targetSdkVersion < 24
			Uri uri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID, mPhotoFile);
			captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		}

		mPhotoButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivityForResult(captureImage, REQUEST_PHOTO);
			}
		});

		mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
		updatePhotoView();

		mPhotoView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				FragmentManager manager = getActivity().getSupportFragmentManager();
				ImageViewDialog dialog = ImageViewDialog.newInstance(mPhotoFile, mCrime.getTitle());
				dialog.show(manager, DIALOG_IMAGE);
			}
		});

		return v;
	}

	@Override
	public void onDetach() {

		super.onDetach();
		mCallbacks = null;
	}

	private void updatePhotoView() {

		if(mPhotoFile == null || !mPhotoFile.exists()) {
			mPhotoView.setImageDrawable(null);
		} else {
			Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
			mPhotoView.setImageBitmap(bitmap);
		}
	}
	private void callPhoneNumber() {

		try {
			if(Build.VERSION.SDK_INT > 22) {

				if(checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
					requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 101);
					startActivity(callSuspect);
					return;
				}
			}
				startActivity(callSuspect);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

		if(requestCode == 101) {
			if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startActivity(callSuspect);
			} else {
				Log.e("FragmentCount", "Permission not granted");
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_crime_detail, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getItemId() == R.id.menu_item_delete_crime) {
			CrimeLab.get(getActivity()).deleteCrime(mCrime.getId().toString());

			if(CrimeLab.get(getActivity()).getCrimes().size() == 0) {

				Intent intent = CrimeListFragment.newIntent(getActivity());
				startActivity(intent);
			} else {
				getActivity().onBackPressed();
			}
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPause() {

		super.onPause();

		CrimeLab.get(getActivity()).updateCrime(mCrime);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		super.onActivityResult(requestCode, resultCode, intent);
		if(resultCode != Activity.RESULT_OK ) {
			return;
		}

		if(requestCode == REQUEST_DATE) {

			Date date = (Date) intent.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
			mCrime.setDate(date);
			updateCrime(mCrime);
			updateDate();
		} else if(requestCode == REQUEST_TIME) {

			if(intent.getSerializableExtra(ARG_TIME) != null) {
				Date date = ((Calendar) intent.getSerializableExtra(ARG_TIME)).getTime();
				mCrime.setTime(date);
				updateCrime(mCrime);
				updateTime();
			}
		} else if(requestCode == REQUEST_CONTACT && intent != null) {
			Uri contactUri = intent.getData();

			Cursor c = getActivity().getContentResolver().query(contactUri, null, null, null, null);

			try {

				if(c.getCount() == 0) {
					return;
				}
				c.moveToFirst();
				String suspect = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

				String phoneNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				Log.d("FragmentCount", "name:" + suspect + " ph:" + phoneNumber);
				mCrime.setSuspect(suspect);
				mCrime.setPhoneNumber(phoneNumber);
				updateCrime(mCrime);
				Log.d("FragmentCount", "Name: " + suspect + " Mob: " + phoneNumber);
				updateSuspect();
				if(isCallVisible == true)
					mCallButton.setEnabled(true);
			} finally {
				c.close();
			}
		} else if(requestCode == REQUEST_PHOTO) {
			updateCrime(mCrime);
			updatePhotoView();
		}
	}

	private void updateDate() {

		mDateButton.setText(DateFormat.format("EEEE, dd MM yyyy", mCrime.getDate()));
	}

	private void updateTime() {

		mTimeButton.setText(DateFormat.format("hh:mm:ss a", mCrime.getTime()));
	}

	private void updateSuspect() {

		if(mCrime.getSuspect() != null)
			mSuspectButton.setText(mCrime.getSuspect());
	}

	private String getCrimeReport() {

		String solvedString = null;
		if(mCrime.isSolved()) {
			solvedString = getString(R.string.crime_report_solved);
		} else {
			solvedString = getString(R.string.crime_report_unsolved);
		}

		String dateFormat = "EEE, dd MMM yyyy";
		String timeFormat = "hh:mm:ss a";

		String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
		String timeString = DateFormat.format(timeFormat, mCrime.getTime()).toString();

		String suspect = mCrime.getSuspect();

		if(suspect == null) {
			suspect = getString(R.string.crime_report_no_suspect);
		} else {
			suspect = getString(R.string.crime_report_suspect, suspect);
		}

		String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, timeString, solvedString, suspect);

		return report;
	}

	public static CrimeFragment newInstance(UUID crimeId, boolean hideDeleteButton) {

		Bundle args = new Bundle();
		args.putSerializable(ARG_CRIME_ID, crimeId);
		args.putBoolean(HIDE_DELETE_BUTTON, hideDeleteButton);

		CrimeFragment fragment = new CrimeFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public static Intent newIntent(Calendar settedTime) {

		Intent intent = new Intent();
		intent.putExtra(ARG_TIME, settedTime);
		return intent;
	}

	public void updateCrime(Crime crime) {

		CrimeLab.get(getActivity()).updateCrime(mCrime);
		mCallbacks.onCrimeUpdated(mCrime);
	}
}
