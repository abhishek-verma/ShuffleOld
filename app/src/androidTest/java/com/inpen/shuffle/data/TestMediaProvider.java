package com.inpen.shuffle.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;

import com.inpen.shuffle.model.database.MediaContract.MediaEntry;
import com.inpen.shuffle.model.database.MediaContract.PlaylistsEntry;
import com.inpen.shuffle.model.database.MediaDbHelper;
import com.inpen.shuffle.model.database.MediaProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Vector;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertTrue;


/**
 * Created by Abhishek on 10/24/2016.
 */

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestMediaProvider {
    public static final String LOG_TAG = TestMediaProvider.class.getSimpleName();

    SQLiteDatabase mDb;
    Context mContext = getTargetContext();

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
        mDb = new MediaDbHelper(mContext).getWritableDatabase();
    }

    @Test
    public void test1Insert() {
        Vector<ContentValues> cvVector = new Vector<>();

        ContentValues cv;

        // checking Inserts into Media Table
        for (int i = 1; i < 5; i++) {
            cv = new ContentValues();

            cv.put(MediaEntry.COLUMN_SONG_ID, "testSongId" + i);
            cv.put(MediaEntry.COLUMN_TITLE, "testTitle" + i);
            cv.put(MediaEntry.COLUMN_ALBUM, "testAlbum" + i);
            cv.put(MediaEntry.COLUMN_ALBUM_KEY, "albumKey" + i);
            cv.put(MediaEntry.COLUMN_ARTIST, "testArtist" + i);
            cv.put(MediaEntry.COLUMN_ARTIST_KEY, "artistKey" + i);
            cv.put(MediaEntry.COLUMN_FOLDER_PATH, "testFolderName" + i);
            cv.put(MediaEntry.COLUMN_ALBUM_ART, "testArtUrl/subpath" + i);
            cv.put(MediaEntry.COLUMN_DURATION, "98293" + i);
            cv.put(MediaEntry.COLUMN_PATH, "testPath/subpath" + i);

            cvVector.add(cv);
        }

        int inserted = 0;
        //adding to db
        if (cvVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MediaEntry.CONTENT_URI, cvArray);
        }

        assertTrue(inserted == cvVector.size());

        //Single insert
        cv = new ContentValues();

        cv.put(MediaEntry.COLUMN_SONG_ID, "testSongId" + 5);
        cv.put(MediaEntry.COLUMN_TITLE, "testTitle" + 5);
        cv.put(MediaEntry.COLUMN_ALBUM, "testAlbum" + 5);
        cv.put(MediaEntry.COLUMN_ALBUM_KEY, "albumKey" + 5);
        cv.put(MediaEntry.COLUMN_ARTIST, "testArtist" + 5);
        cv.put(MediaEntry.COLUMN_ARTIST_KEY, "artistKey" + 5);
        cv.put(MediaEntry.COLUMN_FOLDER_PATH, "testFolderName" + 5);
        cv.put(MediaEntry.COLUMN_ALBUM_ART, "testArtUrl/subpath" + 5);
        cv.put(MediaEntry.COLUMN_DURATION, "98293" + 5);
        cv.put(MediaEntry.COLUMN_PATH, "testPath/subpath" + 5);

        assertTrue(mContext.getContentResolver().insert(MediaEntry.CONTENT_URI, cv) != null);

        //Checking inserts into playlist table
        cvVector.clear();

        for (int i = 1; i < 5; i++) {
            cv = new ContentValues();

            cv.put(PlaylistsEntry.COLUMN_SONG_ID, "testSongId" + i);
            cv.put(PlaylistsEntry.COLUMN_PLAYLIST_NAME, "playlistName" + i);

            cvVector.add(cv);
        }

        inserted = 0;
        //adding to db
        if (cvVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(PlaylistsEntry.CONTENT_URI, cvArray);
        }

        assertTrue(inserted == cvVector.size());

        //single insert into playlist table
        cv = new ContentValues();

        cv.put(PlaylistsEntry.COLUMN_SONG_ID, "testSongId" + 5);
        cv.put(PlaylistsEntry.COLUMN_PLAYLIST_NAME, "playlistName" + 5);

        assertTrue(mContext.getContentResolver().insert(PlaylistsEntry.CONTENT_URI, cv) != null);
    }

    @Test
    public void test2RetrieveSongs() {

        final int COLUMN_INDEX_TITLE = 0;
        final int COLUMN_INDEX_ALBUM = 1;
        final int COLUMN_INDEX_ALBUM_ID = 2;
        final int COLUMN_INDEX_ARTIST = 3;
        final int COLUMN_INDEX_ARTIST_ID = 4;
        final int COLUMN_INDEX_ALBUM_ART = 5;
        final int COLUMN_INDEX_DURATION = 6;
        final int COLUMN_INDEX_PATH = 7;

        final String[] SONGS_QUEUE_CURSOR_COLUMNS = {
                MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_TITLE,
                MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ALBUM,
                MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ALBUM_KEY,
                MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ARTIST,
                MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ARTIST_KEY,
                MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_ALBUM_ART,
                MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_DURATION,
                MediaEntry.TABLE_NAME + "." + MediaEntry.COLUMN_PATH
        };

        Cursor cursor = mContext.getContentResolver()
                .query(MediaEntry.CONTENT_URI,
                        SONGS_QUEUE_CURSOR_COLUMNS,
                        null,
                        null,
                        MediaProvider.mSongsSortOrder);

        assertTrue("required size: 5, actual size: " + cursor.getCount(),
                cursor.getCount() == 5);

        int i = 1;
        cursor.moveToFirst();
        do {
            assertTrue("required: testTitle" + i + ", actual: " + cursor.getString(COLUMN_INDEX_TITLE),
                    cursor.getString(COLUMN_INDEX_TITLE).equals("testTitle" + i));
            assertTrue("required: " + i + ", actual: " + cursor.getString(COLUMN_INDEX_ALBUM),
                    cursor.getString(COLUMN_INDEX_ALBUM).equals("testAlbum" + i));
            assertTrue("required: " + i + ", actual: " + cursor.getString(COLUMN_INDEX_ALBUM_ID),
                    cursor.getString(COLUMN_INDEX_ALBUM_ID).equals("albumKey" + i));
            assertTrue("required: " + i + ", actual: " + cursor.getString(COLUMN_INDEX_ARTIST),
                    cursor.getString(COLUMN_INDEX_ARTIST).equals("testArtist" + i));
            assertTrue("required: " + i + ", actual: " + cursor.getString(COLUMN_INDEX_ARTIST_ID),
                    cursor.getString(COLUMN_INDEX_ARTIST_ID).equals("artistKey" + i));
            assertTrue("required: " + i + ", actual: " + cursor.getString(COLUMN_INDEX_DURATION),
                    cursor.getString(COLUMN_INDEX_DURATION).equals("98293" + i));
            assertTrue("required: " + i + ", actual: " + cursor.getString(COLUMN_INDEX_ALBUM_ART),
                    cursor.getString(COLUMN_INDEX_ALBUM_ART).equals("testArtUrl/subpath" + i));
            assertTrue("required: " + i + ", actual: " + cursor.getString(COLUMN_INDEX_PATH),
                    cursor.getString(COLUMN_INDEX_PATH).equals("testPath/subpath" + i));
            i++;
        } while (cursor.moveToNext());

    }

    @Test
    public void test3RetrievePlaylists() {

        final int COLUMN_INDEX_PLAYLIST_NAME = 0;
        final int COLUMN_INDEX_SONG_ID = 1;

        final String[] PLAYLISTS_CURSOR_COLUMNS = {
                PlaylistsEntry.TABLE_NAME + "." + PlaylistsEntry.COLUMN_PLAYLIST_NAME,
                PlaylistsEntry.TABLE_NAME + "." + PlaylistsEntry.COLUMN_SONG_ID
        };

        Cursor cursor = mContext.getContentResolver()
                .query(PlaylistsEntry.CONTENT_URI,
                        PLAYLISTS_CURSOR_COLUMNS,
                        null,
                        null,
                        MediaProvider.mPlaylistsOrder);

        assertTrue("required size: 5, actual size: " + cursor.getCount(),
                cursor.getCount() == 5);

        int i = 1;
        cursor.moveToFirst();
        do {
            assertTrue("required: playlistName" + i + ", actual: " + cursor.getString(COLUMN_INDEX_PLAYLIST_NAME),
                    cursor.getString(COLUMN_INDEX_PLAYLIST_NAME).equals("playlistName" + i));
            assertTrue("required: testSongId" + i + ", actual: " + cursor.getString(COLUMN_INDEX_SONG_ID),
                    cursor.getString(COLUMN_INDEX_SONG_ID).equals("testSongId" + i));
            i++;
        } while (cursor.moveToNext());

    }


    @After
    public void tearDown() {
        mDb.close();
        deleteTheDatabase();
    }

}
