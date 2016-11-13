package com.inpen.shuffle.syncmedia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inpen.shuffle.model.database.MediaContract;
import com.inpen.shuffle.utils.LogHelper;

import java.util.Vector;

/**
 * Created by Abhishek on 11/12/2016.
 */

public class FirebaseMediaEndpoint implements MediaEndpoint {

    public static final String FIREBASE_USERS_PARENT_KEY = "users";
    public static final String FIREBASE_EMAIL_KEY = "email";
    public static final String FIREBASE_USER_NAME_KEY = "name";
    public static final String FIREBASE_IS_PRO_KEY = "isPro";
    public static final String FIREBASE_PLAYLISTS_KEY = "playlists";
    private static final String LOG_TAG = LogHelper.makeLogTag(FirebaseMediaEndpoint.class);
    Context mContext;

    ValueEventListener dataListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Vector<ContentValues> cvVector = new Vector<>();

            for(DataSnapshot playlistSnapshot: dataSnapshot.getChildren()) {
                for(DataSnapshot songSnapshot: playlistSnapshot.getChildren()) {
                    ContentValues cv = new ContentValues();
                    cv.put(MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME, playlistSnapshot.getKey());
                    cv.put(MediaContract.PlaylistsEntry.COLUMN_SONG_ID, songSnapshot.getKey());

                    cvVector.add(cv);
                }
            }

            int insertedCount = mContext.getContentResolver().bulkInsert(MediaContract.PlaylistsEntry.CONTENT_URI,
                    cvVector.toArray(new ContentValues[0]));

            LogHelper.d(LOG_TAG, "No of values inserted: " + insertedCount);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;

    public FirebaseMediaEndpoint(Context context) {
        mContext = context;
    }

    @Override
    public void syncMedia(Callback callback) {

// Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser != null) {
            mUserId = mFirebaseUser.getUid();

            uploadToFirebase();
            downloadFromFirebase();
        }
    }

    private void uploadToFirebase() {

//        SharedPreferences prefs = mContext.getSharedPreferences(StaticStrings.PREF_NAME_USER_SIGN_DATA, Context.MODE_PRIVATE);
//        String uid = prefs.getString(StaticStrings.PREF_EXTRA_USER_AUTH_ID_STRING_KEY, "");


        LogHelper.d(LOG_TAG, "uploading to firebase, uid:" + mUserId);

        String name = mFirebaseUser.getDisplayName();
        String email = mFirebaseUser.getEmail();

        DatabaseReference userRef = mDatabase.child(FIREBASE_USERS_PARENT_KEY).child(mUserId);
        userRef.child(FIREBASE_EMAIL_KEY).setValue(email);
        userRef.child(FIREBASE_USER_NAME_KEY).setValue(name);
//        userRef.child(FIREBASE_IS_PRO_KEY).setValue(false);

        DatabaseReference playlistsRef = userRef.child(FIREBASE_PLAYLISTS_KEY);

        String[] PLAYLIST_COLUMNS = new String[]{
                MediaContract.PlaylistsEntry.COLUMN_PLAYLIST_NAME,
                MediaContract.PlaylistsEntry.COLUMN_SONG_ID};

        Cursor cursor = mContext.getContentResolver().query(MediaContract.PlaylistsEntry.CONTENT_URI,
                PLAYLIST_COLUMNS,
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlistsRef
                        .child(cursor.getString(0))
                        .child(cursor.getString(1)).setValue(true);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    private void downloadFromFirebase() {


        DatabaseReference userRef = mDatabase.child(FIREBASE_USERS_PARENT_KEY).child(mUserId);
        userRef.child(FIREBASE_PLAYLISTS_KEY).addListenerForSingleValueEvent(dataListener);
        //TODO ADD listener for is pro change


    }
}
