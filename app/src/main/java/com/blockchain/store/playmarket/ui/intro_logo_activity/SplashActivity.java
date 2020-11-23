package com.blockchain.store.playmarket.ui.intro_logo_activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.ui.login_screen.LoginPromptActivity;
import com.blockchain.store.playmarket.ui.main_list_screen.MainMenuActivity;
import com.blockchain.store.playmarket.ui.permissions_prompt_activity.PermissionsPromptActivity;
import com.blockchain.store.playmarket.utilities.AccountManager;
import com.blockchain.store.playmarket.utilities.BaseActivity;
import com.blockchain.store.playmarket.utilities.device.PermissionUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SplashActivity extends BaseActivity implements SplashContracts.View {
    private static final String TAG = "SplashActivity";
    private static final int SplashDisplayLength = 500;
    private static final int PERMISSION_REQUEST_CODE = 101;
    public static final int LOCATION_DIALOG_REQUEST = 102;

    @BindView(R.id.network_status)
    TextView networkStatus;
    @BindView(R.id.error_holder)
    LinearLayout errorHolder;

    private SplashPresenter presenter;
    private String errorString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_logo);
        ButterKnife.bind(this);
        presenter = new SplashPresenter();
        presenter.init(this);
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (PermissionUtils.isLocationPermissionGranted(this)) {
            presenter.requestUserLocation(this);
        } else {
            PermissionUtils.verifyLocationPermissions(this, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            presenter.requestUserLocation(this);
        } else {
            this.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_DIALOG_REQUEST) {
            if (resultCode == RESULT_OK) {
                presenter.requestUserLocation(this);
            } else if (resultCode == RESULT_CANCELED) {
                presenter.requestUserLocation(this);
            }
        }
    }

    protected void openLoginPromptActivity() {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent myIntent;
            if (PermissionUtils.storagePermissionGranted(this)) {
                if (AccountManager.isHasUsers()) {
                    myIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
                } else {
                    myIntent = new Intent(getApplicationContext(), LoginPromptActivity.class);
                }
            } else {
                myIntent = new Intent(getApplicationContext(), PermissionsPromptActivity.class);
            }

            startActivity(myIntent);
            this.finish();
        }, SplashDisplayLength);
    }


    @Override
    public void onLocationReady() {
        openLoginPromptActivity();
    }

    @Override
    public void setStatusText(@StringRes int text) {
        networkStatus.setText(text);
    }

    @Override
    public void setStatusText(int stringRes, String errorString) {
        this.errorString = errorString;
        networkStatus.setText(getString(stringRes));

    }

    @OnClick(R.id.network_status)
    void onNetworkStatusClicked() {
        if (errorString != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setMessage(errorString).create();
            alertDialog.getWindow().addFlags(Window.FEATURE_NO_TITLE);
            alertDialog.show();
        }
    }

    @Override
    public void setStatusText(String text) {
        networkStatus.setText(text);
    }

    @Override
    public void onNearestNodeFailed(Throwable throwable) {
        errorHolder.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.error_view_repeat_btn)
    public void error_view_repeat_btn() {
        errorHolder.setVisibility(View.GONE);
        presenter.requestUserLocation(this);
    }

}

