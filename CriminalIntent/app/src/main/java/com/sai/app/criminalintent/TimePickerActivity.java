package com.sai.app.criminalintent;

import androidx.fragment.app.Fragment;

public class TimePickerActivity extends SingleFragmentActivity {

	@Override
	public void onBackPressed() {

		int count = getSupportFragmentManager().getBackStackEntryCount();

		if(count == 0) {
			super.onBackPressed();
		} else {
			getSupportFragmentManager().popBackStack();
		}
	}

	@Override
	protected Fragment createFragment() {

		return new TimePickerFragment();
	}
}
