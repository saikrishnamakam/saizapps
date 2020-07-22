package com.sai.app.criminalintent;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.io.File;

import android.database.sqlite.SQLiteDatabase;

import com.sai.app.criminalintent.database.CrimeBaseHelper;
import com.sai.app.criminalintent.database.CrimeCursorWrapper;
import com.sai.app.criminalintent.database.CrimeDbSchema.CrimeTable;

import static com.sai.app.criminalintent.database.CrimeDbSchema.CrimeTable.*;

public class CrimeLab {

	private static CrimeLab sCrimeLab;

	private Context mContext;
	private SQLiteDatabase mDatabase;

	private CrimeLab(Context context) {

		mContext = context.getApplicationContext();
		mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
	}

	public static CrimeLab get(Context context) {

		if(sCrimeLab == null) {

			sCrimeLab = new CrimeLab(context);
		}

		return sCrimeLab;
	}

	public void addCrime(Crime c) {

		ContentValues values = getContentValues(c);
		mDatabase.insert(NAME, null, values);
	}

	public void deleteCrimes(List<String> crimeIds) {

		for(String crimeId : crimeIds) {

			deleteCrime(crimeId);
		}
	}

	public void deleteCrime(String crimeId) {

		mDatabase.delete(NAME, Cols.UUID + " = ?", new String[] { crimeId });
	}

	public List<Crime> getCrimes() {

		List<Crime> crimes = new ArrayList<>();

		CrimeCursorWrapper cursor = queryCrimes(null, null);

		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				crimes.add(cursor.getCrime());
				cursor.moveToNext();
			}
		} finally {
			cursor.close();
		}

		return crimes;
	}

	public Crime getCrime(UUID id) {

		CrimeCursorWrapper cursor = queryCrimes(CrimeTable.Cols.UUID + "= ?", new String[] { id.toString() });

		try {
			if (cursor.getCount() == 0)
				return null;

			cursor.moveToFirst();
			return cursor.getCrime();
		} finally {
			cursor.close();
		}
	}

	public void updateCrime(Crime crime) {

		String uuidString = crime.getId().toString();
		ContentValues values = getContentValues(crime);

		mDatabase.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID + " = ?", new String[] { uuidString });
	}

	private static ContentValues getContentValues(Crime crime) {

		ContentValues values = new ContentValues();
		values.put(CrimeTable.Cols.UUID, crime.getId().toString());
		values.put(CrimeTable.Cols.TITLE, crime.getTitle());
		values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
		values.put(CrimeTable.Cols.TIME, crime.getTime().getTime());
		values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
		values.put(Cols.SUSPECT, crime.getSuspect());
		values.put(Cols.PHONENUM, crime.getPhoneNumber());

		return values;
	}

	public CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {

		Cursor cursor = mDatabase.query(CrimeTable.NAME, null, whereClause, whereArgs, null, null, null);
		return new CrimeCursorWrapper(cursor);
	}

	public File getPhotoFile(Crime crime) {

		File externalFileDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

		if(externalFileDir == null) {

			return null;
		}

		return new File(externalFileDir, crime.getPhotoFileName());
	}
}
