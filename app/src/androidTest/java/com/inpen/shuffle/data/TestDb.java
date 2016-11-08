/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inpen.shuffle.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;

import com.inpen.shuffle.model.database.MediaContract;
import com.inpen.shuffle.model.database.MediaDbHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TestDb {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    SQLiteDatabase mDb;
    Context mContext;

    // Since we want each test to play with a clean slate
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
        mDb = new MediaDbHelper(mContext)
                .getWritableDatabase();

    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    @Test
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (mDb version information)
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(MediaContract.MediaEntry.TABLE_NAME);
        tableNameHashSet.add(MediaContract.PlaylistsEntry.TABLE_NAME);

        assertEquals(true, mDb.isOpen());

        // have we created the tables we want?
        Cursor c = mDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without the tables",
                tableNameHashSet.isEmpty());
    }

    @Test
    public void testMediaTable() {

        // now, do our tables contain the correct columns?
        Cursor c = mDb.rawQuery("PRAGMA table_info(" + MediaContract.MediaEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> columnHashSet = new HashSet<>();
        columnHashSet.add(MediaContract.MediaEntry._ID);
        columnHashSet.add(MediaContract.MediaEntry.COLUMN_SONG_ID);
        columnHashSet.add(MediaContract.MediaEntry.COLUMN_TITLE);
        columnHashSet.add(MediaContract.MediaEntry.COLUMN_ALBUM);
        columnHashSet.add(MediaContract.MediaEntry.COLUMN_ALBUM_KEY);
        columnHashSet.add(MediaContract.MediaEntry.COLUMN_ARTIST);
        columnHashSet.add(MediaContract.MediaEntry.COLUMN_ARTIST_KEY);
        columnHashSet.add(MediaContract.MediaEntry.COLUMN_ALBUM_ART);
        columnHashSet.add(MediaContract.MediaEntry.COLUMN_DURATION);
        columnHashSet.add(MediaContract.MediaEntry.COLUMN_PATH);
        columnHashSet.add(MediaContract.MediaEntry.COLUMN_FOLDER_PATH);


        List<String> existingColumns = new ArrayList<>();
        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
            existingColumns.add(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns," +
                        "\n   Existing columns: " + existingColumns.toString(),
                columnHashSet.isEmpty());
    }

    @Test
    public void testPlaylistTable() {

        Cursor c = mDb.rawQuery("PRAGMA table_info(" + MediaContract.PlaylistsEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());


        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> columnHashSet = new HashSet<>();
        columnHashSet.add(MediaContract.PlaylistsEntry._ID);
        columnHashSet.add(MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME);
        columnHashSet.add(MediaContract.PlaylistsEntry.COLUMN_SONG_ID);

        List<String> existingColumns = new ArrayList<>();
        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
            existingColumns.add(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns," +
                        "\n   Existing columns: " + existingColumns.toString(),
                columnHashSet.isEmpty());
    }

    @After
    public void tearDown() throws Exception {
//        mDb.close();
        deleteTheDatabase();
    }

}
