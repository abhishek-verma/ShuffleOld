package com.inpen.shuffle.model;

/**
 * Created by Abhishek on 10/22/2016.
 */

import java.io.Serializable;

/**
 * Created by Abhishek on 10/22/2016.
 */
public class Audio implements Serializable {

    private String path;
    private String title;
    private String album;
    private String artist;
    private String albumArt;
    private long duration;
    private String songID;

    public Audio(String songId, String path, String title, String album, String artist, String albumArt, long duration) {
        this.path = path;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.albumArt = albumArt;
        this.duration = duration;
        this.songID = generateSongID();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getSongID() {
        return songID;
    }

    @Override
    public String toString() {
        return getSongID();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Audio) {
            Audio audio = (Audio) obj;
            if (audio.getSongID().equals(getSongID()))
                return true;
        }
        return super.equals(obj);
    }


    public String generateSongID() {
        // Math.min to prevent endIndex > length()
        // which throws IndexOutOfBondsException
        String durationString = String.valueOf(duration);
        return new StringBuffer(title.substring(0, Math.min(title.length(), 10)))
                .append(artist.substring(0, Math.min(artist.length(), 5)))
                .append(durationString
                        .substring(0, Math.min(durationString.length(), 5)))
                .toString();
    }
}