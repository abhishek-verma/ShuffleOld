package com.inpen.shuffle.syncmedia;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.inpen.shuffle.utils.LogHelper;


public class SyncMediaIntentService extends IntentService {
    final static String LOG_TAG = LogHelper.makeLogTag(SyncMediaIntentService.class);

    public SyncMediaIntentService() {
        super("SyncMediaIntentService");
    }

    public static void syncMedia(Context context) {
        Intent serviceIntent = new Intent(context, SyncMediaIntentService.class);
        context.startService(serviceIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO sync data from local device
        LocalMediaEndpoint localMediaEndpoint = new LocalMediaEndpoint(this);
        localMediaEndpoint.syncMedia(new MediaEndpoint.Callback() {
            @Override
            public void onDataSynced(int songsAddedCount) {
                LogHelper.d(LOG_TAG, "Local media synced, Songs added: " + songsAddedCount);
            }
        });
        // TODO sync data from Firebase
    }


}
