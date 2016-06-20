package com.crossbowffs.quotelock.modules.custom.app;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.modules.custom.provider.CustomQuoteContract;

public class CustomQuoteConfigActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] from = {CustomQuoteContract.Quotes.TEXT, CustomQuoteContract.Quotes.SOURCE};
        int[] to = {R.id.listitem_custom_quote_text, R.id.listitem_custom_quote_source};
        mAdapter = new SimpleCursorAdapter(this, R.layout.listitem_custom_quote, null, from, to, 0);
        setListAdapter(mAdapter);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(0, null, this);

        ListView listView = getListView();
        registerForContextMenu(listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.custom_quote_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.create_quote:
            showEditQuoteDialog(-1);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.custom_quote_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final long rowId = info.id;
        switch (item.getItemId()) {
        case R.id.edit_quote:
            showEditQuoteDialog(rowId);
            return true;
        case R.id.delete_quote:
            deleteQuote(rowId);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
            CustomQuoteContract.Quotes.CONTENT_URI,
            CustomQuoteContract.Quotes.ALL,
            null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    private QuoteData queryQuote(long rowId) {
        if (rowId < 0) {
            return null;
        }

        Uri uri = ContentUris.withAppendedId(CustomQuoteContract.Quotes.CONTENT_URI, rowId);
        String[] columns = {CustomQuoteContract.Quotes.TEXT, CustomQuoteContract.Quotes.SOURCE};
        Cursor cursor = getContentResolver().query(uri, columns, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                return new QuoteData(cursor.getString(0), cursor.getString(1));
            } else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void persistQuote(long rowId, String text, String source) {
        ContentValues values = new ContentValues(3);
        values.put(CustomQuoteContract.Quotes.TEXT, text);
        values.put(CustomQuoteContract.Quotes.SOURCE, source);
        ContentResolver resolver = getContentResolver();
        if (rowId >= 0) {
            Uri uri = ContentUris.withAppendedId(CustomQuoteContract.Quotes.CONTENT_URI, rowId);
            values.put(CustomQuoteContract.Quotes._ID, rowId);
            resolver.update(uri, values, null, null);
        } else {
            resolver.insert(CustomQuoteContract.Quotes.CONTENT_URI, values);
        }
        Toast.makeText(this, R.string.module_custom_saved_quote, Toast.LENGTH_SHORT).show();
    }

    private void deleteQuote(long rowId) {
        Uri uri = ContentUris.withAppendedId(CustomQuoteContract.Quotes.CONTENT_URI, rowId);
        getContentResolver().delete(uri, null, null);
        Toast.makeText(this, R.string.module_custom_deleted_quote, Toast.LENGTH_SHORT).show();
    }

    private void showEditQuoteDialog(final long rowId) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_custom_quote, null);
        final EditText textEditText = (EditText)dialogView.findViewById(R.id.dialog_custom_quote_text);
        final EditText sourceEditText = (EditText)dialogView.findViewById(R.id.dialog_custom_quote_source);
        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.module_custom_enter_quote)
            .setView(dialogView)
            .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    persistQuote(rowId, textEditText.getText().toString(), sourceEditText.getText().toString());
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                boolean canSave = !TextUtils.isEmpty(textEditText.getText()) && !TextUtils.isEmpty(sourceEditText.getText());
                button.setEnabled(canSave);
            }
        };
        textEditText.addTextChangedListener(watcher);
        sourceEditText.addTextChangedListener(watcher);

        QuoteData quote = queryQuote(rowId);
        if (quote != null) {
            textEditText.setText(quote.getQuoteText());
            sourceEditText.setText(quote.getQuoteSource());
        } else {
            textEditText.setText("");
            sourceEditText.setText("");
        }
    }
}
