package com.sai.app.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.text.format.DateFormat;
import android.content.Intent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CrimeListFragment extends Fragment {

	private RecyclerView mCrimeRecyclerView;
	private CrimeAdapter mAdapter;
	private boolean mSubtitleVisible;
	private boolean isRemove;
	private boolean isLongPressed;
	private MenuItem subtitleItem;
	private Callbacks mCallbacks;

	private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

	List<String> deletedCrimes;

	public interface Callbacks {

		void onCrimeSelected(Crime crime);
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Log.d("FragmentCount", "List-onCreate()");
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
		mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
		mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		if (savedInstanceState != null) {

			mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
		}

		updateUI();
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);
		outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
	}

	@Override
	public void onDetach() {

		super.onDetach();
		mCallbacks = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_crime_list, menu);

		subtitleItem = (MenuItem) menu.findItem(R.id.menu_item_show_subtitle);
		if(mSubtitleVisible) {
			subtitleItem.setTitle(R.string.hide_subtitle);
		} else {
			subtitleItem.setTitle(R.string.show_subtitle);
		}
		updateSubtitle();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		switch(item.getItemId()) {

			case R.id.menu_item_new_crime:
				addNewCrime();
				return true;
			case R.id.menu_item_remove_item:
				isRemove = true;
				deletedCrimes = new ArrayList<>();
				getActivity().invalidateOptionsMenu();
				return true;
			case R.id.menu_item_show_subtitle:
				mSubtitleVisible = !mSubtitleVisible;
				updateSubtitle();
				return true;
        	default:
        		return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {

		if(isRemove) {

			menu.findItem(R.id.menu_item_new_crime).setVisible(false);
			menu.findItem(R.id.menu_item_remove_item).setVisible(false);
			menu.findItem(R.id.menu_item_show_subtitle).setVisible(false);

			int deleteItemId = 101;

			if (menu.findItem(deleteItemId) == null) {

				MenuItem delete = menu.add(Menu.NONE, deleteItemId, 1, "Delete Selected");
				delete.setIcon(R.drawable.ic_menu_delete);
				delete.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

				delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {

						isRemove = false;
						CrimeLab crimeLab = CrimeLab.get(getActivity());
						crimeLab.deleteCrimes(deletedCrimes);
						menu.findItem(R.id.menu_item_new_crime).setVisible(true);
						menu.findItem(R.id.menu_item_remove_item).setVisible(true);
						menu.findItem(R.id.menu_item_show_subtitle).setVisible(true);

						menu.findItem(101).setVisible(false);
						isLongPressed = false;
						deletedCrimes = null;

						if(crimeLab.getCrimes().size() == 0) {
							startActivity(getActivity().getIntent());
							getActivity().finish();
						} else {
							mAdapter = new CrimeAdapter(crimeLab.getCrimes());
							mCrimeRecyclerView.setAdapter(mAdapter);
						}
						return true;
					}
				});
			}
			super.onPrepareOptionsMenu(menu);
		}
	}

	private void addNewCrime() {

		Crime crime = new Crime();
		CrimeLab.get(getActivity()).addCrime(crime);
		updateUI();
		mCallbacks.onCrimeSelected(crime);
	}

	private void updateSubtitle() {

		if(mSubtitleVisible)
			subtitleItem.setTitle(R.string.hide_subtitle);
		else
			subtitleItem.setTitle(R.string.show_subtitle);

		CrimeLab crimeLab = CrimeLab.get(getActivity());
		int crimeCount = crimeLab.getCrimes().size();

		String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);

		if(!mSubtitleVisible) {
			subtitle = null;
		}
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.getSupportActionBar().setSubtitle(subtitle);
	}

	@Override
	public void onResume() {

		Log.d("FragmentCount", "List-onResume()");
		super.onResume();
		if(CrimeLab.get(getActivity()).getCrimes().size() != 0)
			updateUI();
	}

	public static Intent newIntent(Context packageContext) {

		Intent intent = new Intent(packageContext, CrimeListActivity.class);
		return intent;
	}

	public void updateUI() {

		CrimeLab crimeLab = CrimeLab.get(getActivity());
		List<Crime> crimes = crimeLab.getCrimes();

		if (mAdapter == null) {
			mAdapter = new CrimeAdapter(crimes);
			mCrimeRecyclerView.setAdapter(mAdapter);
		} else {
			mAdapter.setCrimes(crimes);
			mAdapter.notifyDataSetChanged();
		}
	}

	private class CrimeHolder extends RecyclerView.ViewHolder implements
	View.OnClickListener, View.OnLongClickListener {

		private TextView mTitleTextView;
		private TextView mDateTextView;
		private CheckBox mSolvedCheckBox;

		private Crime mCrime;

		public CrimeHolder(View itemView) {

			super(itemView);
			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);

			itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
			mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_text_view);
			mDateTextView =  (TextView) itemView.findViewById(R.id.list_item_crime_date_text_view);
			mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_crime_solved_check_box);
		}

		public void bindCrime(Crime crime) {

			mCrime = crime;
			String updateDateAndTime = DateFormat.format("EEEE, dd MMMM, yyyy  ", mCrime.getDate()).toString() + DateFormat.format("hh:mm:ss a", mCrime.getTime()).toString();
			mTitleTextView.setText(mCrime.getTitle());
			mDateTextView.setText(updateDateAndTime);
			mSolvedCheckBox.setChecked(mCrime.isSolved());
		}

		@Override
		public void onClick(View v) {

			Log.d("FragmentCount","onClick");
			String crimeId = mCrime.getId().toString();

			if(isLongPressed == true && deletedCrimes != null) {

				if(deletedCrimes.contains(crimeId)) {
					deletedCrimes.remove(crimeId);
					itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
					if(deletedCrimes.size() == 0)
						isLongPressed = false;
				} else if(deletedCrimes != null){
					itemView.setBackgroundColor(Color.parseColor("#EEEEEE"));
					deletedCrimes.add(crimeId);
				}
			} else if(!isRemove) {
				mCallbacks.onCrimeSelected(mCrime);
			}
		}

		@Override
		public boolean onLongClick(View view) {

			Log.d("FragmentCount","onLongClick");
			String crimeId = mCrime.getId().toString();

			if(deletedCrimes != null) {

				if(deletedCrimes.contains(crimeId)) {
					deletedCrimes.remove(crimeId);
					if(deletedCrimes.size() == 0)
						isLongPressed = false;
					itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
				} else {
					itemView.setBackgroundColor(Color.parseColor("#EEEEEE"));
					deletedCrimes.add(crimeId);
					isLongPressed = true;
				}
			}
			return true;
		}
	}

	private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

		private List<Crime> mCrimes;

		public CrimeAdapter(List<Crime> crimes) {

			mCrimes = crimes;
		}

		@Override
		public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {

			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
			View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
			view.setBackgroundColor(Color.parseColor("#FFFFFF"));
			return new CrimeHolder(view);
		}

		@Override
		public void onBindViewHolder(CrimeHolder holder, int position) {

			Crime crime = mCrimes.get(position);
			holder.bindCrime(crime);
		}

		@Override
		public int getItemCount() {

			return mCrimes.size();
		}

		public void setCrimes(List<Crime> crimes) {

			mCrimes = crimes;
		}
	}
}
