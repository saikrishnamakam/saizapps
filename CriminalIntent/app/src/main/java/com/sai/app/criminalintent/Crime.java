package com.sai.app.criminalintent;

import java.util.UUID;
import java.util.Date;

public class Crime {

	private UUID mId;
	private String mTitle;
	private Date mDate;
	private Date mTime;
	private boolean mSolved;
	private String mSuspect;
	private String mPhoneNumber;

	public Crime() {

		this(UUID.randomUUID());
	}

	public Crime(UUID id) {

		//mSuspect = "Choose Suspect";
		mId = id;
		mDate = new Date();
		mTime = mDate;
		mPhoneNumber = "12345";
	}

	public UUID getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public void setId(UUID id) {
		mId = id;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}

	public Date getTime() {

		return mTime;
	}

	public void setTime(Date date) {

		mTime = date;
	}

	public boolean isSolved() {
		return mSolved;
	}

	public void setSolved(boolean solved) {
		mSolved = solved;
	}

	public String getSuspect() {

		return mSuspect;
	}

	public void setSuspect(String suspect) {

		mSuspect = suspect;
	}

	public void setPhoneNumber(String phoneNumber) {

		mPhoneNumber = phoneNumber;
	}

	public String getPhoneNumber() {

		return mPhoneNumber;
	}

	public String getPhotoFileName() {

		return "IMG_" + getId().toString() + ".jpg";
	}
}
