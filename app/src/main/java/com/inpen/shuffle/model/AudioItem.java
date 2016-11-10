package com.inpen.shuffle.model;

/**
 * Created by Abhishek on 10/22/2016.
 */

import java.io.Serializable;

/**
 * Created by Abhishek on 10/22/2016.
 */
public class AudioItem implements Serializable {

    private String mSourcePath;
    private String mTitle;
    private String mAlbum;
    private String mArtist;
    private String mAlbumArt;
    private long mDuration;
    private String mSongID;

    public AudioItem(String songId, String path, String title, String album, String artist, String albumArt, long duration) {
        this.mSourcePath = path;
        this.mTitle = title;
        this.mAlbum = album;
        this.mArtist = artist;
        this.mAlbumArt = albumArt;
        this.mDuration = duration;
        this.mSongID = generateSongID(title, artist, duration);
    }

    public static String generateSongID(String title, String artist, long duration) {
        // Math.min to prevent endIndex > length()
        // which throws IndexOutOfBondsException
        String durationString = String.valueOf(duration);
        return new StringBuffer(title.substring(0, Math.min(title.length(), 10)))
                .append(artist.substring(0, Math.min(artist.length(), 5)))
                .append(durationString
                        .substring(0, Math.min(durationString.length(), 5)))
                .toString();
    }

    public String getmSourcePath() {
        return mSourcePath;
    }

    public void setmSourcePath(String mSourcePath) {
        this.mSourcePath = mSourcePath;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmAlbum() {
        return mAlbum;
    }

    public void setmAlbum(String mAlbum) {
        this.mAlbum = mAlbum;
    }

    public String getmArtist() {
        return mArtist;
    }

    public void setmArtist(String mArtist) {
        this.mArtist = mArtist;
    }

    public String getmAlbumArt() {
        return mAlbumArt;
    }

    public void setmAlbumArt(String mAlbumArt) {
        this.mAlbumArt = mAlbumArt;
    }

    public long getmDuration() {
        return mDuration;
    }

    public void setmDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    public String getmSongID() {
        return mSongID;
    }

    @Override
    public String toString() {
        return getmSongID();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AudioItem) {
            AudioItem audioItem = (AudioItem) obj;
            if (audioItem.getmSongID().equals(getmSongID()))
                return true;
        }
        return super.equals(obj);
    }
}