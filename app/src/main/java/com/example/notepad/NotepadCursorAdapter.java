package com.example.notepad;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.notepad.data.NotepadContract;

import java.util.zip.Inflater;

public class NotepadCursorAdapter extends CursorAdapter {


    public NotepadCursorAdapter(Context context, Cursor c ) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameView= (TextView) view.findViewById(R.id.name);
        TextView  descriptionView=(TextView) view.findViewById(R.id.summary);

        int nameIndex=cursor.getColumnIndex(NotepadContract.NotepadEntry.COLUMN_NAME);
        int descriptionIndex=cursor.getColumnIndex(NotepadContract.NotepadEntry.COLUMN_DESCRIPTION);

        nameView.setText(cursor.getString(nameIndex));
        descriptionView.setText(cursor.getString(descriptionIndex));

    }
}
