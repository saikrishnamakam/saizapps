package com.sai.app.beatbox;

import android.content.res.AssetManager;
import android.content.res.AssetFileDescriptor;
import android.content.Context;
import android.util.Log;
import android.media.SoundPool;
import android.media.AudioManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BeatBox {

	private static final String TAG = "BeatBox";
	private static final String SOUNDS_FOLDER = "sample_sounds";
	private static final int MAX_SOUNDS = 5;

	private AssetManager mAssets;
	private List<Sound> mSounds = new ArrayList<>();
	private SoundPool mSoundPool;

	public BeatBox(Context context) {

		mAssets = context.getAssets();
		mSoundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0);
		loadSounds();
	}

	private void loadSounds() {

		String[] soundNames;

		try {
			soundNames = mAssets.list(SOUNDS_FOLDER);
			Log.i(TAG, "Found" + soundNames.length + "sounds");
		} catch(IOException ioe) {
			Log.e(TAG, "Could not list assets", ioe);
			return;
		}

		for(String fileName : soundNames) {
			try {
				String assetPath = SOUNDS_FOLDER + "/" + fileName;
				Sound sound = new Sound(assetPath);
				load(sound);
				mSounds.add(sound);
			} catch (IOException ioe) {
				Log.e(TAG, "Could not load sound" + fileName, ioe);
			}
		}
	}

	private void load(Sound sound) throws IOException {

		AssetFileDescriptor afd = mAssets.openFd(sound.getAssetPath());
		int soundId = mSoundPool.load(afd, 1);
		sound.setSoundId(soundId);
	}

	public void play(Sound sound) {

		Integer soundId = sound.getSoundId();

		if(soundId == null) {
			return;
		}
		mSoundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
	}

	public void release() {

		mSoundPool.release();
	}

	public List<Sound> getSounds() {

		return mSounds;
	}
}
