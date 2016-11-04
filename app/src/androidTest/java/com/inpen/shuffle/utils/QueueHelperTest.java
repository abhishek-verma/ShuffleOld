package com.inpen.shuffle.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.inpen.shuffle.model.Audio;
import com.inpen.shuffle.model.QueueHelper;
import com.inpen.shuffle.model.database.MediaContract;
import com.inpen.shuffle.model.database.MediaDbHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Vector;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertTrue;

/**
 * Created by Abhishek on 10/24/2016.
 */

@RunWith(AndroidJUnit4.class)
public class QueueHelperTest {

    private static final String LOG_TAG = QueueHelperTest.class.getSimpleName();
    private Context mContext;

    // Since we want each test to start with a clean slate
    private void deleteTheDatabase() {
        getTargetContext().deleteDatabase(MediaDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    @Before
    public void setUp() {
        deleteTheDatabase();

        mContext = getTargetContext();
        SQLiteDatabase db = new MediaDbHelper(mContext).getWritableDatabase();
        db.close();

        Vector<ContentValues> cvVector = new Vector<>();
        ContentValues cv;
        // checking Inserts into Media Table
        for (int i = 1; i < 5; i++) {
            cv = new ContentValues();

            cv.put(MediaContract.MediaEntry.COLUMN_SONG_ID, "testSongId" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_TITLE, "testTitle" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_ALBUM, "testAlbum" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_ALBUM_ID, 100 + i);
            cv.put(MediaContract.MediaEntry.COLUMN_ARTIST, "testArtist" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_ARTIST_ID, 100 + i);
            cv.put(MediaContract.MediaEntry.COLUMN_FOLDER_PATH, "testFolderName" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_ALBUM_ART, "testArtUrl/subpath" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_DURATION, "98293" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_PATH, "testPath/subpath" + i);

            cvVector.add(cv);
        }

        //adding to db
        if (cvVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(MediaContract.MediaEntry.CONTENT_URI, cvArray);
        }

        //Checking inserts into playlist table
        cvVector.clear();

        for (int i = 1; i < 5; i++) {
            cv = new ContentValues();

            cv.put(MediaContract.PlaylistsEntry.COLUMN_SONG_ID, "testSongId" + i);
            cv.put(MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME, "playlistName" + i);

            cvVector.add(cv);
        }

        //adding to db
        if (cvVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(MediaContract.PlaylistsEntry.CONTENT_URI, cvArray);
        }

    }

    @Test
    public void generateQueue() throws Exception {

        QueueHelper qh = new QueueHelper(mContext);

        // Retrieving all songs
        List<Audio> audioList = qh.generateQueue(QueueHelper.MediaSelectorType.NONE,
                null);
        Log.i(LOG_TAG, "all songs: " + audioList.toString());
        assertTrue(audioList.size() == 4);

        // Retrieving all songs for album
        audioList = qh.generateQueue(QueueHelper.MediaSelectorType.ALBUM_ID,
                new String[]{"101", "102"});
        Log.i(LOG_TAG, "all songs for albumId: " + audioList.toString());
        assertTrue("Expected Size: 2, Actual size: " + audioList.size(), audioList.size() == 2);

        // Retrieving all songs for artist
        audioList = qh.generateQueue(QueueHelper.MediaSelectorType.ARTIST_ID,
                new String[]{"101", "102"});
        Log.i(LOG_TAG, "all songs for artistId: " + audioList.toString());
        assertTrue(audioList.size() == 2);

//         Retrieving all songs for playlist
        audioList = qh.generateQueue(QueueHelper.MediaSelectorType.PLAYLIST,
                new String[]{"playlistName1", "playlistName2"});
        Log.i(LOG_TAG, "all songs playlist: " + audioList.toString());
        assertTrue("Expected size: 2, Actual size: " + audioList.size(),
                audioList.size() == 2);

        // Retrieving all songs for path
        audioList = qh.generateQueue(QueueHelper.MediaSelectorType.PATH,
                new String[]{"testPath/subpath1", "testPath/subpath2"});
        Log.i(LOG_TAG, "all songs for path: " + audioList.toString());
        assertTrue(audioList.size() == 2);
    }

    @After
    public void tearDown() {
        deleteTheDatabase();
    }
}