package com.inpen.shuffle.syncmedia;

/**
 * Created by Abhishek on 10/25/2016.
 */

public interface MediaEndpoint {
    void syncMedia(Callback callback);

    interface Callback {
        void onDataSynced(int songsAddedCount);


    }
}
