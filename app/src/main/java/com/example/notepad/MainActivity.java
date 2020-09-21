package com.example.notepad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notepad.data.NotepadContract;
import com.example.notepad.data.NotepadContract.NotepadEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private NotepadCursorAdapter mNotepadCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);

            }
        });

        ListView listView = (ListView) findViewById(R.id.list);

        //Empty view
        View emptyView = (View) findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri uri = ContentUris.withAppendedId(NotepadEntry.CONTENT_URI, id);

                intent.setData(uri);

                startActivity(intent);
            }
        });

        mNotepadCursorAdapter = new NotepadCursorAdapter(this, null);
        listView.setAdapter(mNotepadCursorAdapter);


        //initialisig the bakcground  methods
        getSupportLoaderManager().initLoader(0, null, this);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }

    private void insertData() {

        ContentValues values = new ContentValues();
        values.put(NotepadContract.NotepadEntry.COLUMN_NAME, "dummy");
        values.put(NotepadContract.NotepadEntry.COLUMN_DESCRIPTION, "this is a dummy note ,asdfasdgasagsdagsdagasdgsdagsda");


        Uri newUri = getContentResolver().insert(NotepadContract.NotepadEntry.CONTENT_URI, values);

        if (newUri == null) {
            Toast.makeText(this, "Error with saving dummynote", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "dummynote saved", Toast.LENGTH_SHORT).show();
        }


    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Condirm delete");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAll();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAll() {

        int rowsDeleted = getContentResolver().delete(NotepadEntry.CONTENT_URI, null, null);
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, "Error with deletion",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, "deletion Successful",
                    Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_on_dummynotes:
                insertData();
                return true;

            case R.id.action_on_delete_all:

                showDeleteConfirmationDialog();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        String[] projection = {
                NotepadEntry._ID,
                NotepadEntry.COLUMN_NAME,
                NotepadEntry.COLUMN_DESCRIPTION};

        return new CursorLoader(this,
                NotepadEntry.CONTENT_URI,
                projection,
                null,
                null, null);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mNotepadCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNotepadCursorAdapter.swapCursor(null);
    }
}