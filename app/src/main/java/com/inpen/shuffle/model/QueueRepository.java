package com.inpen.shuffle.model;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inpen.shuffle.utils.CustomTypes.State;
import com.inpen.shuffle.utils.LogHelper;
import com.inpen.shuffle.utils.QueueHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Simple data provider for music tracks. The actual metadata source is delegated to a
 * MusicProviderSource defined by a constructor argument of this class.
 */
public class QueueRepository {
    private static final String TAG = LogHelper.makeLogTag(QueueRepository.class);
    private static final String AUDIO_STORAGE = "com.inpen.shuffle.AUDIO_STORAGE";
    private static final String KEY_PLAYING_QUEUE = "playing_queue";
    private static final String KEY_CURRENT_TRACK_INDEX = "current_track_index";
    public static QueueRepository mQueueRepositoryInstance;

    // cached playing queue
    public List<Audio> mPlayingQueue;
    public int mCurrentTrackIndex;
    private SharedPreferences mPreferences;
    private volatile State mCurrentState = State.NON_INITIALIZED;


    public static QueueRepository getInstance() {
        if (mQueueRepositoryInstance == null) {
            mQueueRepositoryInstance = new QueueRepository();
        }

        return mQueueRepositoryInstance;
    }

    synchronized void initializeQueue(QueueHelper.MediaSelectorType selector,
                                      List<String> selectorItems,
                                      Context context) {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                // TODO to generate playlist
                // get data from queue helper
                // cache data
                // store data into memory

//                 Asynchronously load the music catalog in a separate thread
//                new AsyncTask<Void, Void, State>() {
//                    @Override
//                    protected State doInBackground(Void... params) {
//                        retrieveMedia();
//                        return mCurrentState;
//                    }
//
//                    @Override
//                    protected void onPostExecute(State current) {
//                        if (callback != null) {
//                            callback.onMusicCatalogReady(current == State.INITIALIZED);
//                        }
//                    }
//                }.execute();

                storeQueue(context);
                mCurrentState = State.INITIALIZED;
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    public void storeQueue(Context context) {

        SharedPreferences.Editor editor = getmPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(mPlayingQueue);
        editor.putString(KEY_PLAYING_QUEUE, json);

        editor.putInt(KEY_CURRENT_TRACK_INDEX, mCurrentTrackIndex);
        editor.apply();
    }

    public void loadQueue(Context context) {
        try {
            if (mCurrentState == State.NON_INITIALIZED)
                mCurrentState = State.INITIALIZING;

            Gson gson = new Gson();
            String json = getmPreferences(context).getString(KEY_PLAYING_QUEUE, null);

            Type type = new TypeToken<ArrayList<Audio>>() {
            }.getType();

            mPlayingQueue = gson.fromJson(json, type);
            mCurrentTrackIndex = getmPreferences(context).getInt(KEY_CURRENT_TRACK_INDEX, -1);

            if (mPlayingQueue != null && mPlayingQueue.size() > 0) {
                mCurrentState = State.INITIALIZED;
            }

        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    public void clearCachedAudioPlaylist(Context context) {

        mPlayingQueue.clear();

        SharedPreferences.Editor editor = getmPreferences(context).edit();
        editor.clear();
        editor.apply();
    }

    private SharedPreferences getmPreferences(Context context) {

        if (mPreferences == null)
            mPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE);

        return mPreferences;
    }


    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }
}
