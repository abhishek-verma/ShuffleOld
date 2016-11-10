package com.inpen.shuffle.utils;

import android.support.v4.media.MediaMetadataCompat;

import com.inpen.shuffle.model.AudioItem;

/**
 * Created by Abhishek on 11/7/2016.
 */

public class MediaHelper {

    public static MediaMetadataCompat getMetaDataForAudio(AudioItem audioItem) {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, audioItem.getmSongID())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audioItem.getmAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audioItem.getmArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, audioItem.getmDuration())
//                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre) TODO add genre
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, audioItem.getmAlbumArt())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audioItem.getmTitle())
//                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
//                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                .build();
    }
}
