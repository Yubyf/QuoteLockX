package com.crossbowffs.quotelock.collections.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.crossbowffs.quotelock.R;
import com.crossbowffs.quotelock.backup.LocalBackup;
import com.crossbowffs.quotelock.backup.ProgressCallback;
import com.crossbowffs.quotelock.backup.RemoteBackup;
import com.crossbowffs.quotelock.collections.provider.QuoteCollectionHelper;
import com.crossbowffs.quotelock.utils.DpUtils;

/**
 * @author Yubyf
 */
public class QuoteCollectionActivity extends AppCompatActivity {

    private ProgressDialog mLoadingDialog;
    private Menu mMenu;

    private final ProgressCallback mLocalBackupCallback = new AbstractBackupCallback() {
        @Override
        public void success() {
            Toast.makeText(QuoteCollectionActivity.this, "Local backup completed",
                    Toast.LENGTH_SHORT).show();
            hideProgress();
        }
    };

    private final ProgressCallback mLocalRestoreCallback = new AbstractBackupCallback() {
        @Override
        public void success() {
            Toast.makeText(QuoteCollectionActivity.this, "Local restore completed",
                    Toast.LENGTH_SHORT).show();
            hideProgress();
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (fragment instanceof QuoteCollectionFragment) {
                ((QuoteCollectionFragment) fragment).reloadData();
            }
        }
    };

    private final ProgressCallback mRemoteBackupCallback = new AbstractBackupCallback() {
        @Override
        public void success() {
            Toast.makeText(QuoteCollectionActivity.this, "Remote backup completed",
                    Toast.LENGTH_SHORT).show();
            hideProgress();
        }
    };

    private final ProgressCallback mRemoteRestoreCallback = new AbstractBackupCallback() {
        @Override
        public void success() {
            Toast.makeText(QuoteCollectionActivity.this, "Remote restore completed",
                    Toast.LENGTH_SHORT).show();
            hideProgress();
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (fragment instanceof QuoteCollectionFragment) {
                ((QuoteCollectionFragment) fragment).reloadData();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new QuoteCollectionFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collections_options, menu);
        mMenu = menu;
        initMenu(mMenu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.account_action) {
            if (RemoteBackup.getInstance().isGoogleAccountSignedIn(this)) {
                RemoteBackup.getInstance().switchAccount(this, RemoteBackup.REQUEST_CODE_SIGN_IN,
                        new ProgressCallback() {
                            @Override
                            public void inProcessing(String message) {
                                showProgress(message);
                            }

                            @Override
                            public void success() {
                                hideProgress();
                                initMenu(mMenu);
                                invalidateOptionsMenu();
                            }

                            @Override
                            public void failure(String message) {
                                hideProgress();
                            }
                        });
            } else {
                RemoteBackup.getInstance().requestSignIn(this, RemoteBackup.REQUEST_CODE_SIGN_IN);
            }
        } else if (item.getItemId() == R.id.local_backup) {
            showProgress("Start local backup...");
            localBackup();
            return true;
        } else if (item.getItemId() == R.id.local_restore) {
            showProgress("Start local restore...");
            localRestore();
            return true;
        } else if (item.getItemId() == R.id.remote_backup) {
            showProgress("Start remote backup...");
            remoteBackup();
            return true;
        } else if (item.getItemId() == R.id.remote_restore) {
            showProgress("Start remote restore...");
            remoteRestore();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocalBackup.REQUEST_CODE_PERMISSIONS_BACKUP) {
            LocalBackup.handleRequestPermissionsResult(grantResults, mLocalBackupCallback,
                    this::localBackup);
        } else if (requestCode == LocalBackup.REQUEST_CODE_PERMISSIONS_RESTORE) {
            LocalBackup.handleRequestPermissionsResult(grantResults, mLocalRestoreCallback,
                    this::localRestore);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RemoteBackup.REQUEST_CODE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                RemoteBackup.getInstance().handleSignInResult(this, data,
                        mRemoteBackupCallback, () -> {
                            Toast.makeText(this,
                                    "Successfully connected to Google account!", Toast.LENGTH_SHORT)
                                    .show();
                            initMenu(mMenu);
                            invalidateOptionsMenu();
                        });
            }
        } else if (requestCode == RemoteBackup.REQUEST_CODE_SIGN_IN_BACKUP) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                RemoteBackup.getInstance().handleSignInResult(this, data,
                        mRemoteBackupCallback, this::remoteBackup);
            }
        } else if (requestCode == RemoteBackup.REQUEST_CODE_SIGN_IN_RESTORE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                RemoteBackup.getInstance().handleSignInResult(this, data,
                        mRemoteRestoreCallback, this::remoteRestore);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initMenu(Menu menu) {
        if (RemoteBackup.getInstance().isGoogleAccountSignedIn(this)) {
            menu.findItem(R.id.account).setVisible(true);
            menu.findItem(R.id.account).setTitle(RemoteBackup.getInstance().getSignedInGoogleAccountEmail(this));
            menu.findItem(R.id.account_action).setTitle(R.string.switch_account);
            menu.findItem(R.id.remote_backup).setEnabled(true);
            menu.findItem(R.id.remote_restore).setEnabled(true);
            Uri avatar = RemoteBackup.getInstance().getSignedInGoogleAccountPhoto(this);
            int iconSize = (int) DpUtils.dp2px(this, 24);
            Glide.with(getApplicationContext()).asDrawable().load(avatar)
                    .apply(new RequestOptions().override(iconSize, iconSize))
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource,
                                                    @Nullable Transition<? super Drawable> transition) {
                            mMenu.findItem(R.id.account).setIcon(resource);
                            invalidateOptionsMenu();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } else {
            menu.findItem(R.id.account_action).setTitle(R.string.connect_account);
            menu.findItem(R.id.account).setVisible(false);
            menu.findItem(R.id.remote_backup).setEnabled(false);
            menu.findItem(R.id.remote_restore).setEnabled(false);
        }
    }

    private void localBackup() {
        LocalBackup.performBackup(this, QuoteCollectionHelper.DATABASE_NAME,
                mLocalBackupCallback);
    }

    private void localRestore() {
        LocalBackup.performRestore(this, QuoteCollectionHelper.DATABASE_NAME,
                mLocalRestoreCallback);
    }

    private void remoteBackup() {
        RemoteBackup.getInstance().performDriveBackup(this,
                QuoteCollectionHelper.DATABASE_NAME, mRemoteBackupCallback);
    }

    private void remoteRestore() {
        RemoteBackup.getInstance().performDriveRestore(this,
                QuoteCollectionHelper.DATABASE_NAME, mRemoteRestoreCallback);
    }

    private void showProgress() {
        showProgress(null);
    }

    private void showProgress(String message) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new ProgressDialog(this);
        }
        mLoadingDialog.setCancelable(true);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setMessage(message);
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    private void hideProgress() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    private abstract class AbstractBackupCallback implements ProgressCallback {

        @Override
        public void inProcessing(String message) {
            showProgress(message);
        }

        @Override
        public void failure(String message) {
            Toast.makeText(QuoteCollectionActivity.this, message,
                    Toast.LENGTH_SHORT).show();
            hideProgress();
        }
    }
}
