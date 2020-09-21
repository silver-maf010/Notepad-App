package com.example.notepad;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.notepad.data.NotepadContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private Uri mNewUri;


    private Boolean mNotehasChanged = false;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            mNotehasChanged = true;
            return true;
        }
    };


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mNewUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_on_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();

        mNewUri = intent.getData();
        if (mNewUri == null) {
            setTitle("Add a note");

            invalidateOptionsMenu();
        } else {
            setTitle("Edit note");

            getSupportLoaderManager().initLoader(0, null, this);
        }


        mNameEditText = (EditText) findViewById(R.id.edit_name_view);
        mDescriptionEditText = (EditText) findViewById(R.id.description_view);


        mNameEditText.setOnTouchListener(mOnTouchListener);
        mDescriptionEditText.setOnTouchListener(mOnTouchListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        return true;
    }


    private void insert() {

        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();

        if (mNewUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(descriptionString)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(NotepadContract.NotepadEntry.COLUMN_NAME, nameString);
        values.put(NotepadContract.NotepadEntry.COLUMN_DESCRIPTION, descriptionString);


        if (mNewUri == null) {
            Uri newUri = getContentResolver().insert(NotepadContract.NotepadEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, "Error with saving note", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "note saved", Toast.LENGTH_SHORT).show();
            }
        } else {
            int row = getContentResolver().update(mNewUri, values, null, null);

            if (row == 0) {
                Toast.makeText(this, "Error with updating", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "updated Successfully", Toast.LENGTH_SHORT).show();
            }
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
                deleteNote();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the Note.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteNote() {
        // Only perform the delete if this is an existing pet.
        if (mNewUri != null) {
            // Call the ContentResolver to delete the note at the given content URI.
            // Pass in null for the selection and selection args because the mNewUri
            // content URI already identifies the Note that we want.
            int rowsDeleted = getContentResolver().delete(mNewUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
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

        // Close the activity
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_on_save:
                insert();
                finish();
                return true;

            case R.id.action_on_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:

                if (!mNotehasChanged) {
                    // Navigate back to parent activity
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Create a click listener to handle the user confirming that changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, close the current activity.
                                finish();
                            }
                        };

                // Show dialog that there are unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        String[] projection = {
                NotepadContract.NotepadEntry._ID,
                NotepadContract.NotepadEntry.COLUMN_NAME,
                NotepadContract.NotepadEntry.COLUMN_DESCRIPTION};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mNewUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            // Find the columns of notepad attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(NotepadContract.NotepadEntry.COLUMN_NAME);
            int breedColumnIndex = cursor.getColumnIndex(NotepadContract.NotepadEntry.COLUMN_DESCRIPTION);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mDescriptionEditText.setText(breed);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescriptionEditText.setText("");

    }

    @Override
    public void onBackPressed() {
        if (!mNotehasChanged) {
            super.onBackPressed();
        }

        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Not saved");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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
}
