package com.inpen.shuffle.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;

import com.inpen.shuffle.model.database.MediaContract;
import com.inpen.shuffle.model.database.MediaDbHelper;
import com.inpen.shuffle.model.database.MediaProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Vector;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Abhishek on 11/11/2016.
 */

@RunWith(AndroidJUnit4.class)
public class PlaylistHelperTest {
    private static final String LOG_TAG = PlaylistHelper.class.getSimpleName();
    private Context mContext;
    private SQLiteDatabase mDb;

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
        mDb = new MediaDbHelper(mContext).getWritableDatabase();


        Vector<ContentValues> cvVector = new Vector<>();
        ContentValues cv;
        // checking Inserts into Media Table
        for (int i = 1; i < 5; i++) {
            cv = new ContentValues();

            cv.put(MediaContract.MediaEntry.COLUMN_SONG_ID, "testSongId" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_TITLE, "testTitle" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_ALBUM, "testAlbum" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_ALBUM_KEY, 100 + i);
            cv.put(MediaContract.MediaEntry.COLUMN_ARTIST, "testArtist" + i);
            cv.put(MediaContract.MediaEntry.COLUMN_ARTIST_KEY, 100 + i);
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
    }

    @Test
    public void testPlaylistHelperMethods() throws Exception {
        String[] playlistNames = new String[]{"playlist1", "playlist2"};
        String wrongPLaylist = "wrongPLaylist";
        String[] audioIds = new String[]{"testSongId1", "testSongId2"};
        String incorrectAudioId = "incorrectAudioId";

        assertTrue(PlaylistHelper.insertAudioIntoPlaylist(playlistNames[0], audioIds[0], mContext));
        assertTrue(PlaylistHelper.insertAudioIntoPlaylist(playlistNames[0], audioIds[1], mContext));
        assertTrue(PlaylistHelper.insertAudioIntoPlaylist(playlistNames[1], audioIds[0], mContext));
        assertTrue(PlaylistHelper.insertAudioIntoPlaylist(playlistNames[1], audioIds[1], mContext));


        final String[] PLAYLISTS_CURSOR_COLUMNS = {
                MediaContract.PlaylistsEntry.TABLE_NAME + "." + MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME,
                MediaContract.PlaylistsEntry.TABLE_NAME + "." + MediaContract.PlaylistsEntry.COLUMN_SONG_ID
        };

        Cursor cursor = mContext.getContentResolver()
                .query(MediaContract.PlaylistsEntry.CONTENT_URI,
                        PLAYLISTS_CURSOR_COLUMNS,
                        null,
                        null,
                        MediaProvider.mPlaylistsOrder);

        LogHelper.e(LOG_TAG, "Cursor size : " + cursor.getCount());

        cursor.moveToFirst();
        do {
            LogHelper.e(LOG_TAG, cursor.getString(0) + ", " + cursor.getString(1));
        } while (cursor.moveToNext());

        assertTrue("Required count: 4, actual count: " + cursor.getCount(), cursor.getCount() == 4);
        cursor.close();

        assertTrue(PlaylistHelper.isAudioInPlaylist(playlistNames[0], audioIds[0], mContext));
        assertTrue(PlaylistHelper.isAudioInPlaylist(playlistNames[0], audioIds[1], mContext));
        assertTrue(PlaylistHelper.isAudioInPlaylist(playlistNames[1], audioIds[0], mContext));
        assertTrue(PlaylistHelper.isAudioInPlaylist(playlistNames[1], audioIds[1], mContext));
        assertFalse(PlaylistHelper.isAudioInPlaylist(playlistNames[1], incorrectAudioId, mContext));
        assertFalse(PlaylistHelper.isAudioInPlaylist(wrongPLaylist, audioIds[1], mContext));
        assertFalse(PlaylistHelper.isAudioInPlaylist(wrongPLaylist, incorrectAudioId, mContext));

        assertTrue(PlaylistHelper.removeAudioFromPlaylist(playlistNames[0], audioIds[0], mContext));
        assertTrue(PlaylistHelper.removeAudioFromPlaylist(playlistNames[0], audioIds[1], mContext));
        assertTrue(PlaylistHelper.removeAudioFromPlaylist(playlistNames[1], audioIds[0], mContext));
        assertTrue(PlaylistHelper.removeAudioFromPlaylist(playlistNames[1], audioIds[1], mContext));
        assertFalse(PlaylistHelper.removeAudioFromPlaylist(playlistNames[1], incorrectAudioId, mContext));
        assertFalse(PlaylistHelper.removeAudioFromPlaylist(wrongPLaylist, audioIds[1], mContext));
        assertFalse(PlaylistHelper.removeAudioFromPlaylist(wrongPLaylist, incorrectAudioId, mContext));

        assertFalse(PlaylistHelper.isAudioInPlaylist(playlistNames[0], audioIds[0], mContext));
        assertFalse(PlaylistHelper.isAudioInPlaylist(playlistNames[0], audioIds[1], mContext));
        assertFalse(PlaylistHelper.isAudioInPlaylist(playlistNames[1], audioIds[0], mContext));
        assertFalse(PlaylistHelper.isAudioInPlaylist(playlistNames[1], audioIds[1], mContext));

    }


    @After
    public void tearDown() throws Exception {
        deleteTheDatabase();
        mDb.close();
    }

}