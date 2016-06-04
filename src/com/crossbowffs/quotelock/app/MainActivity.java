package com.crossbowffs.quotelock.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.api.QuoteData;
import com.crossbowffs.quotelock.utils.JobUtils;

public class MainActivity extends Activity {
    private class ActivityQuoteDownloaderTask extends QuoteDownloaderTask {
        private ProgressDialog mDialog;

        private ActivityQuoteDownloaderTask() {
            super(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage(getString(R.string.downloading_quote));
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected void onPostExecute(QuoteData quote) {
            super.onPostExecute(quote);
            mDialog.dismiss();
            if (quote == null) {
                Toast.makeText(mContext, R.string.quote_download_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, R.string.quote_download_success, Toast.LENGTH_SHORT).show();
            }
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

        // In case the user opens the app for the first time *after* rebooting,
        // we want to make sure the background job has been created.
        JobUtils.createQuoteDownloadJob(this, false);
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
            refreshQuote();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void refreshQuote() {
        new ActivityQuoteDownloaderTask().execute();
    }
}
