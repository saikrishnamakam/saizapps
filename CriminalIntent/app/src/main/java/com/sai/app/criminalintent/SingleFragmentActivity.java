package com.sai.app.criminalintent;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public abstract class SingleFragmentActivity extends AppCompatActivity {

	protected abstract Fragment createFragment();

	@LayoutRes
	protected int getLayoutResId() {

		return R.layout.activity_fragment;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());

		FragmentManager fm = getSupportFragmentManager();

		Fragment fragment = fm.findFragmentById(R.id.fragment_container);

		if(fragment == null) {

			fragment = createFragment();

			fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
		}
	}
}

	<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
		xmlns:tools="http://schemas.android.com/tools"
		android:id="@+id/fragment_container"
		android:layout_width="match_parent"
		android:layout_height="match_parent"
		tools:context=".NerdLauncherActivity">

</FrameLayout>