package com.crossbowffs.quotelock.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.model.VnaasQuote;

public class MainActivity extends Activity {
    private class ActivityQuoteDownloaderTask extends QuoteDownloaderTask {
        private ProgressDialog mDialog;

        private ActivityQuoteDownloaderTask() {
            super(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage(getString(R.string.downloading_quote));
            mDialog.setIndeterminate(true);
            mDialog.show();
        }

        @Override
        protected void onPostExecute(VnaasQuote vnaasQuote) {
            super.onPostExecute(vnaasQuote);
            mDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getFragmentManager()
            .beginTransaction()
            .replace(R.id.content_frame, new SettingsFragment())
            .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refesh_quote:
            new ActivityQuoteDownloaderTask().execute();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
