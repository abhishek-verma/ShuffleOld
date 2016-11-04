package com.inpen.shuffle.model;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inpen.shuffle.utils.CustomTypes;
import com.inpen.shuffle.utils.CustomTypes.RepositoryState;
import com.inpen.shuffle.utils.LogHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Simple data provider for music tracks. The actual metadata source is delegated to a
 * MusicProviderSource defined by a constructor argument of this class.
 */
public class QueueRepository {
    private static final String LOG_TAG = LogHelper.makeLogTag(QueueRepository.class);
    private static final String AUDIO_STORAGE = "com.inpen.shuffle.AUDIO_STORAGE";
    private static final String KEY_PLAYING_QUEUE = "playing_queue";
    private static final String KEY_CURRENT_TRACK_INDEX = "current_track_index";
    public static QueueRepository mQueueRepositoryInstance;

    // cached playing queue
    public List<Audio> mPlayingQueue;
    public int mCurrentTrackIndex;
    private SharedPreferences mPreferences;
    private volatile RepositoryState mCurrentState = CustomTypes.RepositoryState.NON_INITIALIZED;


    public static QueueRepository getInstance() {
        LogHelper.v(LOG_TAG, "getInstance()");
        if (mQueueRepositoryInstance == null) {
            LogHelper.v(LOG_TAG, "new instance created");
            mQueueRepositoryInstance = new QueueRepository();
        }

        return mQueueRepositoryInstance;
    }

    /**
     * To generate queue and save in memory as well as catch it
     * Call this only once per instance
     *
     * @param selector
     * @param selectorItems
     * @param context
     * @param callback
     */
    synchronized public void initializeQueue(CustomTypes.ItemType selector,
                                             List<String> selectorItems,
                                             Context context,
                                             QueueRepositoryCallback callback) {
        LogHelper.v(LOG_TAG, "initializeQueue( selector:" + selector.toString()
                + ", selectorItems size:" + selectorItems.size() + " )");

        mCurrentState = CustomTypes.RepositoryState.INITIALIZING;

        // to generate queue
        // get data from queue helper
        // store data into memory
        // cache data

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Object, Void, List<Audio>>() {

            QueueRepositoryCallback mCallback;
            Context mContext;

            @Override
            protected List<Audio> doInBackground(Object... params) {
                CustomTypes.ItemType selector = (CustomTypes.ItemType) params[0];
                List<String> selectorItems = (List<String>) params[1];
                mContext = (Context) params[2];
                mCallback = (QueueRepositoryCallback) params[3];

                QueueHelper queueHelper = new QueueHelper(mContext);
                List<Audio> audioList = queueHelper.generateQueue(selector, selectorItems);
                return audioList;
            }

            @Override
            protected void onPostExecute(List<Audio> audioList) {

                if (audioList != null && audioList.size() != 0) {
                    try {
                        mPlayingQueue = audioList;
                        mCurrentTrackIndex = 0;
                        storeQueue(mContext);
                        mCurrentState = RepositoryState.INITIALIZED;
                    } finally {
                        if (!mCurrentState.equals(RepositoryState.INITIALIZED)) {
                            mCurrentState = RepositoryState.NON_INITIALIZED;
                        }
                    }
                }
                if (mCurrentState.equals(RepositoryState.INITIALIZED))
                    mCallback.onMusicCatalogReady(true);
                else
                    mCallback.onMusicCatalogReady(false);

            }
        }.execute(selector, selectorItems, context, callback);

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
        if (mCurrentState == RepositoryState.INITIALIZED)

            try {
                if (mCurrentState == CustomTypes.RepositoryState.NON_INITIALIZED)
                    mCurrentState = CustomTypes.RepositoryState.INITIALIZING;

                Gson gson = new Gson();
                String json = getmPreferences(context).getString(KEY_PLAYING_QUEUE, null);

                Type type = new TypeToken<ArrayList<Audio>>() {
                }.getType();

                mPlayingQueue = gson.fromJson(json, type);
                mCurrentTrackIndex = getmPreferences(context).getInt(KEY_CURRENT_TRACK_INDEX, -1);

                if (mPlayingQueue != null && mPlayingQueue.size() > 0) {
                    mCurrentState = CustomTypes.RepositoryState.INITIALIZED;
                }

            } finally {
                if (mCurrentState != RepositoryState.INITIALIZED) {
                    // Something bad happened, so we reset state to NON_INITIALIZED to allow
                    // retries (eg if the network connection is temporary unavailable)
                    mCurrentState = CustomTypes.RepositoryState.NON_INITIALIZED;
                }
            }
    }

    public void clearCachedAudioPlaylist(Context context) {

        mPlayingQueue.clear();
        mCurrentTrackIndex = 0;

        SharedPreferences.Editor editor = getmPreferences(context).edit();
        editor.clear();
        editor.apply();
    }

    private SharedPreferences getmPreferences(Context context) {

        if (mPreferences == null)
            mPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE);

        return mPreferences;
    }


    public interface QueueRepositoryCallback {

        void onMusicCatalogReady(boolean success);
    }
}
