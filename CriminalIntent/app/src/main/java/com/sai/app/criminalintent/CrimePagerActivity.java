
package com.sai.app.criminalintent;

import android.os.Bundle;
import android.content.Intent;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.UUID;
import java.util.List;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks {

	private static final String CRIME_EXTRA_ID = "com.sai.app.criminalintent.crime_id";
	private static final String HIDE_DELETE_BUTTON = "com.sai.app.criminalintent.hide_delete_button";

	private ViewPager mViewPager;
	private List<Crime> mCrimes;
	public boolean mHideDeleteButton;

	public static Intent newIntent(Context packageContext, UUID crimeId, boolean deleteButtonVisibility) {

		Intent intent = new Intent(packageContext, CrimePagerActivity.class);
		intent.putExtra(CRIME_EXTRA_ID, crimeId);
		intent.putExtra(HIDE_DELETE_BUTTON, deleteButtonVisibility);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crime_pager);

		UUID crimeId = (UUID) getIntent().getSerializableExtra(CRIME_EXTRA_ID);
		mHideDeleteButton = (boolean) getIntent().getBooleanExtra(HIDE_DELETE_BUTTON, false);

		mViewPager = (ViewPager) findViewById(R.id.activity_crime_pager_view_pager);

		mCrimes = CrimeLab.get(this).getCrimes();
		FragmentManager fragmentManager = getSupportFragmentManager();
		mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

			@Override
			public Fragment getItem(int position) {

				Crime crime = mCrimes.get(position);
				return CrimeFragment.newInstance(crime.getId(), mHideDeleteButton);
			}

			@Override
			public int getCount() {

				return mCrimes.size();
			}
		});

		for(int i = 0; i < mCrimes.size(); i++) {

			if(mCrimes.get(i).getId().equals(crimeId)) {
				mViewPager.setCurrentItem(i);
				break;
			}
		}
	}

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
	public void onCrimeUpdated(Crime crime) {

	}
}

