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
    public List<AudioItem> mPlayingQueue;
    public int mCurrentTrackIndex = -1;
    private SharedPreferences mPreferences;
    private volatile RepositoryState mCurrentState = CustomTypes.RepositoryState.NON_INITIALIZED;

    private List<CurrentItemIndexChangedObserver> mCurrentItemIndexChangedObserverList;


    public static QueueRepository getInstance() {
        LogHelper.v(LOG_TAG, "getInstance()");
        if (mQueueRepositoryInstance == null) {
            LogHelper.v(LOG_TAG, "new instance created");
            mQueueRepositoryInstance = new QueueRepository();
        }

        return mQueueRepositoryInstance;
    }

    public static boolean hasCachedQueue(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE);
        return prefs.contains(KEY_PLAYING_QUEUE);
    }

    public RepositoryState getState() {
        return mCurrentState;
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
                                             QueueRepositoryInitializedCallback callback) {
        LogHelper.v(LOG_TAG, "initializeQueue( selector:" + selector.toString()
                + ", selectorItems size:" + selectorItems.size() + " )");

        mCurrentState = CustomTypes.RepositoryState.INITIALIZING;

        // to generate queue
        // get data from queue helper
        // store data into memory
        // cache data

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Object, Void, List<AudioItem>>() {

            QueueRepositoryInitializedCallback mCallback;
            Context mContext;

            @Override
            protected List<AudioItem> doInBackground(Object... params) {
                CustomTypes.ItemType selector = (CustomTypes.ItemType) params[0];
                List<String> selectorItems = (List<String>) params[1];
                mContext = (Context) params[2];
                mCallback = (QueueRepositoryInitializedCallback) params[3];

                QueueHelper queueHelper = new QueueHelper(mContext);
                List<AudioItem> audioItemList = queueHelper.generateQueue(selector, selectorItems);
                return audioItemList;
            }

            @Override
            protected void onPostExecute(List<AudioItem> audioItemList) {

                if (audioItemList != null && audioItemList.size() != 0) {
                    try {
                        mPlayingQueue = audioItemList;
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
                    mCallback.onPlayingQueueReady(true);
                else
                    mCallback.onPlayingQueueReady(false);

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

    /**
     * @param context
     * @param callback
     * @return true if a cached queue exists and Repo is trying to load it asynchronously else false
     */
    public boolean loadQueue(Context context, CachedQueueLoadedCallback callback) {

        if (!hasCachedQueue(context))
            return false;

        // retrieve asynchronously, inform callback
        new AsyncTask<Object, Void, Void>() {

            Context mContext;
            CachedQueueLoadedCallback mCallback;

            @Override
            protected Void doInBackground(Object... objects) {

                mContext = (Context) objects[0];
                mCallback = (CachedQueueLoadedCallback) objects[1];

                try {
                    if (mCurrentState.equals(CustomTypes.RepositoryState.NON_INITIALIZED))
                        mCurrentState = CustomTypes.RepositoryState.INITIALIZING;

                    Gson gson = new Gson();
                    String json = getmPreferences(mContext).getString(KEY_PLAYING_QUEUE, null);

                    Type type = new TypeToken<ArrayList<AudioItem>>() {
                    }.getType();

                    mPlayingQueue = gson.fromJson(json, type);
                    mCurrentTrackIndex = getmPreferences(mContext).getInt(KEY_CURRENT_TRACK_INDEX, -1);

                    if (mPlayingQueue != null && mPlayingQueue.size() > 0) {
                        mCurrentState = CustomTypes.RepositoryState.INITIALIZED;
                    }

                } finally {
                    if (!mCurrentState.equals(RepositoryState.INITIALIZED)) {
                        // Something bad happened, so we reset state to NON_INITIALIZED to allow
                        // retries (eg if the network connection is temporary unavailable)
                        mCurrentState = CustomTypes.RepositoryState.NON_INITIALIZED;
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mCallback != null)
                    mCallback.onCachedQueueLoaded();
            }
        }.execute(context, callback);


        return true;

    }

    public void clearCachedAudioPlaylist(Context context) {

        mCurrentState = RepositoryState.NON_INITIALIZED;
        mPlayingQueue.clear();
        mCurrentTrackIndex = -1;

        SharedPreferences.Editor editor = getmPreferences(context).edit();
        editor.clear();
        editor.apply();
    }

    public boolean isCatchEmpty(Context context) {
        return getmPreferences(context).contains(KEY_PLAYING_QUEUE);
    }

    private SharedPreferences getmPreferences(Context context) {

        if (mPreferences == null)
            mPreferences = context.getSharedPreferences(AUDIO_STORAGE, Context.MODE_PRIVATE);

        return mPreferences;
    }

    public AudioItem getCurrentMusic() {
        return mCurrentState.equals(RepositoryState.INITIALIZED) ?
                mPlayingQueue.get(mCurrentTrackIndex)
                : null;
    }

    public void setCurrentQueueItem(AudioItem audioItem) {
        if (mPlayingQueue != null)
            mCurrentTrackIndex = mPlayingQueue.indexOf(audioItem);

        notifyCurrentItemIndexChangedObservers();
    }

    public void skipQueuePosition(int amount) {
        int index = mCurrentTrackIndex + amount;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0;
        } else {
            // skip forwards when in last song will cycle back to play of the queue
            index %= mPlayingQueue.size();
        }
        mCurrentTrackIndex = index;
        notifyCurrentItemIndexChangedObservers();
    }

    public void addCurrentItemIndexChangedObserver(CurrentItemIndexChangedObserver observer) {
        if (mCurrentItemIndexChangedObserverList == null)
            mCurrentItemIndexChangedObserverList = new ArrayList<>();

        if (!mCurrentItemIndexChangedObserverList.contains(observer))
            mCurrentItemIndexChangedObserverList.add(observer);
    }

    public void removeCurrentItemIndexChangedObserver(CurrentItemIndexChangedObserver observer) {
        if (mCurrentItemIndexChangedObserverList != null)
            mCurrentItemIndexChangedObserverList.remove(observer);
    }

    private void notifyCurrentItemIndexChangedObservers() {
        if (mCurrentItemIndexChangedObserverList == null)
            return;

        for (CurrentItemIndexChangedObserver observers :
                mCurrentItemIndexChangedObserverList) {
            if (observers != null)
                observers.onQueueIndexChanged();
        }
    }

    public interface QueueRepositoryInitializedCallback {
        void onPlayingQueueReady(boolean success);
    }

    public interface CurrentItemIndexChangedObserver {
        void onQueueIndexChanged();
    }

    public interface CachedQueueLoadedCallback {
        void onCachedQueueLoaded();
    }
}
