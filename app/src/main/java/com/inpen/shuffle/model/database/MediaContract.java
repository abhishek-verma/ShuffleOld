package com.inpen.shuffle.model.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.io.File;

/**
 * Created by Abhishek on 10/21/2016.
 */

public class MediaContract {

    public static final String CONTENT_AUTHORITY = "com.inpen.shuffle";
    public static final String PATH_MEDIA = "media";
    public static final String PATH_ALBUM = "album";
    public static final String PATH_ARTIST = "artist";
    public static final String PATH_FOLDER = "folder";
    public static final String PATH_BY_PLAYLIST = "by_playlist";
    public static final String PATH_PLAYLISTS = "playlists";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class MediaEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MEDIA).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_MEDIA;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_MEDIA;

        // Table Name
        public static final String TABLE_NAME = "songs";

        public static final String COLUMN_SONG_ID = "song_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ALBUM = "album";
        public static final String COLUMN_ALBUM_ID = "album_id";
        public static final String COLUMN_ARTIST = "artist";
        public static final String COLUMN_ARTIST_ID = "artist_id";
        public static final String COLUMN_FOLDER_PATH = "folder";
        public static final String COLUMN_ALBUM_ART = "album_art";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_PATH = "path";


        public static Uri buildSongWithIdUri(String songId) {
            return CONTENT_URI.buildUpon().appendPath(songId).build();
        }

        public static Uri buildSongByPlaylistUri() {
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_BY_PLAYLIST)
                    .build();
        }

        public static String getSongIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String getPlaylistNameFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String getSongFolderFromFolderPath(String path) {
            return Uri.parse(path).getLastPathSegment();
        }

        public static String getFolderPathFromFullPath(String path) {
            return new File(path).getParent();
        }
    }

    public static final class PlaylistsEntry implements BaseColumns {


        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAYLISTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_PLAYLISTS;

        public static final String TABLE_NAME = "playlists";

        public static final String COLUMN_PLAYLIST_NAME = "playlist_name";
        public static final String COLUMN_SONG_ID = "playlist_song_id";
    }
}
