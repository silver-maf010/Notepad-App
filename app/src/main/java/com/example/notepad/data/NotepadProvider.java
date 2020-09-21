package com.example.notepad.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NotepadProvider extends ContentProvider {

    private NotePadDbHelper mNotepadDbHelper;


    public static final String LOG_TAG = NotepadProvider.class.getSimpleName();

    private static final int NOTEPAD = 100;


    private static final int NOTEPAD_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(NotepadContract.CONTENT_AUTHORITY,NotepadContract.PATH_NOTEPAD,NOTEPAD);
        sUriMatcher.addURI(NotepadContract.CONTENT_AUTHORITY,NotepadContract.PATH_NOTEPAD + "/#",NOTEPAD_ID);

    }


    @Override
    public boolean onCreate() {
        mNotepadDbHelper= new NotePadDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database=mNotepadDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match){

            case NOTEPAD:
                cursor= database.query(NotepadContract.NotepadEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;

            case NOTEPAD_ID:

                selection= NotepadContract.NotepadEntry._ID + "+?";
                selectionArgs=new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor=database.query(NotepadContract.NotepadEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;

            default:
                throw new IllegalArgumentException("cannot query unknown query"+ uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);


        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTEPAD:

                return NotepadContract.NotepadEntry.CONTENT_LIST_TYPE;
            case NOTEPAD_ID:
                return NotepadContract.NotepadEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }

    }


    @Override
    public Uri insert(@NonNull Uri uri,  ContentValues values) {

        final int match=sUriMatcher.match(uri);

        switch (match){

            case NOTEPAD:
                return  insertHelper(uri,values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for" + uri);
        }

    }


    private  Uri  insertHelper(Uri uri,ContentValues values)
    {

        String name = values.getAsString(NotepadContract.NotepadEntry.COLUMN_NAME);
        if(name==null){
            Log.v(LOG_TAG,"name is null");

            throw  new IllegalArgumentException("name is required");
        }

        String description=values.getAsString(NotepadContract.NotepadEntry.COLUMN_DESCRIPTION);
        if(description==null){
            throw  new IllegalArgumentException("description is required");
        }
        SQLiteDatabase database=mNotepadDbHelper.getWritableDatabase();

        long rowId= database.insert(NotepadContract.NotepadEntry.TABLE_NAME,null,values);

        if (rowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }


        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);


        return ContentUris.withAppendedId(uri, rowId);

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match=sUriMatcher.match(uri);
        SQLiteDatabase database=mNotepadDbHelper.getWritableDatabase();

        int rowsDeleted=0;
        switch (match){
            case  NOTEPAD:
                rowsDeleted=  database.delete(NotepadContract.NotepadEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case NOTEPAD_ID:

                selection= NotepadContract.NotepadEntry._ID+"=?";
                selectionArgs=new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted=  database.delete(NotepadContract.NotepadEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw  new IllegalArgumentException("cant perform delete for the uri"+ uri);

        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final  int march=sUriMatcher.match(uri);

        switch (march)
        {
            case NOTEPAD:
                return  updateNotepad(uri,values,selection,selectionArgs);

            case NOTEPAD_ID:
                selection= NotepadContract.NotepadEntry._ID + "=?";
                selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
                return  updateNotepad(uri,values,selection,selectionArgs);
            default:
                throw  new IllegalArgumentException("can perform update for the uri"+ uri);

        }
    }

    private  int  updateNotepad(Uri uri,ContentValues values,String selection,String[] selectionArgs)
    {

        if (values.containsKey(NotepadContract.NotepadEntry.COLUMN_NAME)) {
            String name = values.getAsString(NotepadContract.NotepadEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Notepad requires a name");
            }
        }
        if (values.containsKey(NotepadContract.NotepadEntry.COLUMN_DESCRIPTION)) {
            String name = values.getAsString(NotepadContract.NotepadEntry.COLUMN_DESCRIPTION);
            if (name == null) {
                throw new IllegalArgumentException("Notepad requires a summary");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database=mNotepadDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(NotepadContract.NotepadEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    return  rowsUpdated;
    }
}
