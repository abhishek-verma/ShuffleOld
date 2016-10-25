package com.inpen.shuffle.model;


import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
    private final Context mContext;
    // cached playing queue
    public List<Audio> mPlayingQueue;
    public int mCurrentTrackIndex;
    private SharedPreferences mPreferences;
    private volatile State mCurrentState = State.NON_INITIALIZED;

    public QueueRepository(Context context) {
        this.mContext = context;
    }

    public static QueueRepository getInstance(Context context) {
        if (mQueueRepositoryInstance == null) {
            mQueueRepositoryInstance = new QueueRepository(context);
        }

        return mQueueRepositoryInstance;
    }

    private synchronized void initializeQueue(QueueHelper.MediaSelectorType selector, List<String> selectorItems) {
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

                storeQueue();
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

    public void storeQueue() {

        if (mPreferences == null)
            mPreferences = mContext.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = mPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mPlayingQueue);
        editor.putString(KEY_PLAYING_QUEUE, json);

        editor.putInt(KEY_CURRENT_TRACK_INDEX, mCurrentTrackIndex);
        editor.apply();
    }

    public void loadQueue() {
        try {
            if (mCurrentState == State.NON_INITIALIZED)
                mCurrentState = State.INITIALIZING;

            if (mPreferences == null)
                mPreferences = mContext.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String json = mPreferences.getString("audioArrayList", null);

            Type type = new TypeToken<ArrayList<Audio>>() {
            }.getType();

            mPlayingQueue = gson.fromJson(json, type);
            mCurrentTrackIndex = mPreferences.getInt(KEY_CURRENT_TRACK_INDEX, -1);

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

    public void clearCachedAudioPlaylist() {

        if (mPreferences == null)
            mPreferences = mContext.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        editor.apply();
    }

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }


    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }
}
