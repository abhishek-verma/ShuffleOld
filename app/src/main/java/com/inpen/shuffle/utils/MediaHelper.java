package com.inpen.shuffle.utils;

import android.support.v4.media.MediaMetadataCompat;

import com.inpen.shuffle.model.Audio;

/**
 * Created by Abhishek on 11/7/2016.
 */

public class MediaHelper {

    public static MediaMetadataCompat getMetaDataForAudio(Audio audio) {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, audio.getmSongID())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audio.getmAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audio.getmArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, audio.getmDuration())
//                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre) TODO add genre
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, audio.getmAlbumArt())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audio.getmTitle())
//                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
//                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }
}
