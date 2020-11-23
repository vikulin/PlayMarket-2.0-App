package com.blockchain.store.playmarket.ui.webview_screen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.utilities.BaseActivity;
import com.blockchain.store.playmarket.utilities.device.PermissionUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewActivity extends BaseActivity {
    private static final String TAG = "WebViewActivity";
    private static final String EXTRA_ADDRESS_ARG = "address_extra";

    @BindView(R.id.web_view) WebView webView;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    private String urlAddress;

    public static void start(Context context, String urlAddress) {
        Intent starter = new Intent(context, WebViewActivity.class);
        starter.putExtra(EXTRA_ADDRESS_ARG, urlAddress);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);
        if (getIntent() != null) {
            urlAddress = getIntent().getStringExtra(EXTRA_ADDRESS_ARG);
        } else {
            this.finish();
        }

        webView.getSettings().setAppCacheMaxSize(50 * 1024 * 1024); // 10MB
        webView.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setWebChromeClient(new WebChromeClient() {


            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(PermissionUtils.getPermissionsLocation());
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });
        webView.loadUrl(urlAddress);
    }

}
