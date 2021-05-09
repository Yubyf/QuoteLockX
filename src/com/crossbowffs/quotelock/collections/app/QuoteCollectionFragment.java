package com.crossbowffs.quotelock.collections.app;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionContract;
import com.crossbowffs.quotelock.consts.PrefKeys;

/**
 * @author Yubyf
 */
public class QuoteCollectionFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
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
        String[] from = {QuoteCollectionContract.Collections.TEXT, QuoteCollectionContract.Collections.SOURCE};
        int[] to = {R.id.listitem_custom_quote_text, R.id.listitem_custom_quote_source};
        mAdapter = new SimpleCursorAdapter(requireContext(), R.layout.listitem_custom_quote, null, from, to, 0);
        setListAdapter(mAdapter);

        LoaderManager loaderManager = LoaderManager.getInstance(this);
        loaderManager.initLoader(0, null, this);

        ListView listView = getListView();
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.custom_quote_context, menu);
        menu.findItem(R.id.edit_quote).setVisible(false);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final long rowId = info.id;
        if (item.getItemId() == R.id.delete_quote) {
            deleteQuote(rowId);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(requireContext(),
                QuoteCollectionContract.Collections.CONTENT_URI,
                QuoteCollectionContract.Collections.ALL,
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

    private void deleteQuote(long rowId) {
        Uri uri = ContentUris.withAppendedId(QuoteCollectionContract.Collections.CONTENT_URI, rowId);
        int result = requireContext().getContentResolver().delete(uri, null, null);
        if (result >= 0) {
            requireContext().getSharedPreferences(PrefKeys.PREF_QUOTES, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(PrefKeys.PREF_QUOTES_COLLECTION_STATE, false)
                    .apply();
            Toast.makeText(requireContext(), R.string.module_custom_deleted_quote, Toast.LENGTH_SHORT).show();
        }
    }
}
