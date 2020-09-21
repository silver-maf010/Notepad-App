package com.example.notepad.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class NotePadDbHelper extends SQLiteOpenHelper {


    public static final String LOG_TAG = NotePadDbHelper.class.getSimpleName();


    private static final String DATABASE_NAME = "shelter.db";

    private static final int DATABASE_VERSION = 1;
    public NotePadDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + NotepadContract.NotepadEntry.TABLE_NAME + " ("
                + NotepadContract.NotepadEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NotepadContract.NotepadEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + NotepadContract.NotepadEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_PETS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
