package com.crossbowffs.quotelock.modules.custom.app;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.modules.custom.provider.CustomQuoteContract;

public class CustomQuoteConfigFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.layout_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String[] from = {CustomQuoteContract.Quotes.TEXT, CustomQuoteContract.Quotes.SOURCE};
        int[] to = {R.id.listitem_custom_quote_text, R.id.listitem_custom_quote_source};
        mAdapter = new SimpleCursorAdapter(requireContext(), R.layout.listitem_custom_quote, null, from, to, 0);
        setListAdapter(mAdapter);

        LoaderManager loaderManager = LoaderManager.getInstance(this);
        loaderManager.initLoader(0, null, this);

        ListView listView = getListView();
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.custom_quote_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create_quote) {
            showEditQuoteDialog(-1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.custom_quote_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final long rowId = info.id;
        int itemId = item.getItemId();
        if (itemId == R.id.edit_quote) {
            showEditQuoteDialog(rowId);
            return true;
        } else if (itemId == R.id.delete_quote) {
            deleteQuote(rowId);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(requireContext(),
                CustomQuoteContract.Quotes.CONTENT_URI,
                CustomQuoteContract.Quotes.ALL,
                null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    private QuoteData queryQuote(long rowId) {
        if (rowId < 0) {
            return null;
        }

        Uri uri = ContentUris.withAppendedId(CustomQuoteContract.Quotes.CONTENT_URI, rowId);
        String[] columns = {CustomQuoteContract.Quotes.TEXT, CustomQuoteContract.Quotes.SOURCE};
        try (Cursor cursor = requireContext().getContentResolver().query(uri, columns, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return new QuoteData(cursor.getString(0), cursor.getString(1));
            } else {
                return null;
            }
        }
    }

    private void persistQuote(long rowId, String text, String source) {
        ContentValues values = new ContentValues(3);
        values.put(CustomQuoteContract.Quotes.TEXT, text);
        values.put(CustomQuoteContract.Quotes.SOURCE, source);
        ContentResolver resolver = requireContext().getContentResolver();
        if (rowId >= 0) {
            Uri uri = ContentUris.withAppendedId(CustomQuoteContract.Quotes.CONTENT_URI, rowId);
            values.put(CustomQuoteContract.Quotes._ID, rowId);
            resolver.update(uri, values, null, null);
        } else {
            resolver.insert(CustomQuoteContract.Quotes.CONTENT_URI, values);
        }
        Toast.makeText(requireContext(), R.string.module_custom_saved_quote, Toast.LENGTH_SHORT).show();
    }

    private void deleteQuote(long rowId) {
        Uri uri = ContentUris.withAppendedId(CustomQuoteContract.Quotes.CONTENT_URI, rowId);
        requireContext().getContentResolver().delete(uri, null, null);
        Toast.makeText(requireContext(), R.string.module_custom_deleted_quote, Toast.LENGTH_SHORT).show();
    }

    private void showEditQuoteDialog(final long rowId) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_custom_quote, null);
        final EditText textEditText = (EditText) dialogView.findViewById(R.id.dialog_custom_quote_text);
        final EditText sourceEditText = (EditText) dialogView.findViewById(R.id.dialog_custom_quote_source);
        final AlertDialog dialog = new AlertDialog.Builder(requireContext())
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                boolean canSave = !TextUtils.isEmpty(textEditText.getText());
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
