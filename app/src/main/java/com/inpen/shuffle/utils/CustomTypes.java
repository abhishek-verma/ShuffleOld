package com.inpen.shuffle.utils;

/**
 * Created by Abhishek on 11/1/2016.
 */

public class CustomTypes {


    public enum ItemType {
        ALBUM_ID, ARTIST_ID, PLAYLIST, FOLDER;

        public static int toInt(ItemType it) {
            switch (it) {
                case ALBUM_ID:
                    return 0;
                case ARTIST_ID:
                    return 1;
                case PLAYLIST:
                    return 2;
                case FOLDER:
                    return 3;
                default:
                    return -1;
            }
        }

        public static ItemType fromInt(int i) {
            switch (i) {
                case 0:
                    return ALBUM_ID;
                case 1:
                    return ARTIST_ID;
                case 2:
                    return PLAYLIST;
                case 3:
                    return FOLDER;
                default:
                    return null;
            }
        }
    }

    public enum RepositoryState {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    public enum MainFabState {
        HIDDEN, PLAYER, SHUFFLE
    }

}
