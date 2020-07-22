package com.sai.app.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.UUID;

public class TimePickerFragment extends Fragment {

	TimePicker mTimePicker;
	Crime mCrime;
	Calendar calendar;

	public static String EXTRA_CRIME = "com.sai.app.criminalintent.extra_crime";

	public static Intent newIntent(Context packageContext, UUID id) {

		Intent intent = new Intent(packageContext, TimePickerActivity.class);
		intent.putExtra(EXTRA_CRIME, id);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		UUID id = (UUID)getActivity().getIntent().getSerializableExtra(EXTRA_CRIME);
		mCrime = CrimeLab.get(getActivity()).getCrime(id);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.time_fragment, container, false);

		mTimePicker = (TimePicker) view.findViewById(R.id.time_picker);

		calendar = Calendar.getInstance();
		calendar.setTime(mCrime.getTime());

		int minute = calendar.get(Calendar.MINUTE);
		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

		mTimePicker.setCurrentMinute(minute);
		mTimePicker.setCurrentHour(hourOfDay);

		mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {

				calendar = Calendar.getInstance();
				calendar.set(Calendar.MINUTE, minute);
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			}
		});

		Button mbutton = (Button) view.findViewById(R.id.time_fragment_button);
		mbutton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = CrimeFragment.newIntent(calendar);
				getActivity().setResult(Activity.RESULT_OK, intent);
				getActivity().onBackPressed();
			}
		});
		return view;
	}
}
