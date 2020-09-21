package com.example.notepad.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class NotepadContract {

    private NotepadContract(){

    }

    public static final String CONTENT_AUTHORITY = "com.example.notepad";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_NOTEPAD = "Notepad";



    public static  final class  NotepadEntry implements BaseColumns{

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTEPAD;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTEPAD;



        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NOTEPAD);


        public final static String TABLE_NAME = "Notepad";


        public final static String _ID = BaseColumns._ID;


        public final static String COLUMN_NAME ="name";
        public final static String COLUMN_DESCRIPTION="description";
    }


}
