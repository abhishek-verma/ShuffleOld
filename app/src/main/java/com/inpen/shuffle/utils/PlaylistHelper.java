package com.inpen.shuffle.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inpen.shuffle.model.database.MediaContract;
import com.inpen.shuffle.syncmedia.FirebaseMediaEndpoint;

/**
 * Created by Abhishek on 11/11/2016.
 */

public class PlaylistHelper {

    private static final String LOG_TAG = LogHelper.makeLogTag(PlaylistHelper.class);

    public static void isAudioInPlaylist(final String playlistName, final String audioId, final Context context, final Callback callback) {


        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                final String[] PLAYLISTS_CURSOR_COLUMNS = {
                        MediaContract.PlaylistsEntry.TABLE_NAME + "." + MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME,
                        MediaContract.PlaylistsEntry.TABLE_NAME + "." + MediaContract.PlaylistsEntry.COLUMN_SONG_ID
                };

                Cursor cursor = context.getContentResolver()
                        .query(MediaContract.PlaylistsEntry.CONTENT_URI,
                                PLAYLISTS_CURSOR_COLUMNS,
                                MediaContract.PlaylistsEntry.TABLE_NAME + "." +
                                        MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME + " = ? AND " +
                                        MediaContract.PlaylistsEntry.TABLE_NAME + "." +
                                        MediaContract.PlaylistsEntry.COLUMN_SONG_ID + " = ? ",
                                new String[]{playlistName, audioId},
                                null, null);

                if (cursor == null) {
                    LogHelper.e(LOG_TAG, "Cursor is null while retrieving playlist!");
                }

                boolean isInPlaylist = cursor.moveToFirst() && cursor.getCount() == 1;

                cursor.close();

                return isInPlaylist;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);

                callback.isInPlaylist(aBoolean);
            }
        }.execute();

    }

    public static void insertAudioIntoPlaylist(final String playlistName, final String audioId, final Context context) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues cv = new ContentValues();
                cv.put(MediaContract.PlaylistsEntry.COLUMN_SONG_ID, audioId);
                cv.put(MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME, playlistName);
                Uri uri = context.getContentResolver()
                        .insert(MediaContract.PlaylistsEntry.CONTENT_URI,
                                cv);

                LogHelper.e(LOG_TAG, "Uri received on inserting: " + uri);

                //adding to firebase syncing
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                if (firebaseUser != null) {
                    String userId = firebaseUser.getUid();
                    DatabaseReference userRef = database.child(FirebaseMediaEndpoint.FIREBASE_USERS_PARENT_KEY).child(userId);
                    DatabaseReference playlistsRef = userRef.child(FirebaseMediaEndpoint.FIREBASE_PLAYLISTS_KEY);

                    playlistsRef
                            .child(playlistName)
                            .child(audioId).setValue(true);
                }

                return null;
            }
        }.execute();

    }

    public static void removeAudioFromPlaylist(final String playlistName, final String audioId, final Context context) {


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                int deletedCount = context.getContentResolver()
                        .delete(MediaContract.PlaylistsEntry.CONTENT_URI,
                                MediaContract.PlaylistsEntry.TABLE_NAME + "." +
                                        MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME + " = ? AND " +
                                        MediaContract.PlaylistsEntry.TABLE_NAME + "." +
                                        MediaContract.PlaylistsEntry.COLUMN_SONG_ID + " = ? ",
                                new String[]{playlistName, audioId});


                //adding to firebase syncing
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                if (firebaseUser != null) {
                    String userId = firebaseUser.getUid();
                    DatabaseReference userRef = database.child(FirebaseMediaEndpoint.FIREBASE_USERS_PARENT_KEY).child(userId);
                    DatabaseReference playlistsRef = userRef.child(FirebaseMediaEndpoint.FIREBASE_PLAYLISTS_KEY);

                    playlistsRef
                            .child(playlistName)
                            .child(audioId).removeValue();
                }

                return null;
            }
        }.execute();
    }

    public interface Callback {
        void isInPlaylist(boolean result);
    }
}
