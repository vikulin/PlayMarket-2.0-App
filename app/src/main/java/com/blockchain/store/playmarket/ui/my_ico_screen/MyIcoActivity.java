package com.blockchain.store.playmarket.ui.my_ico_screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.adapters.IcoListAdapter;
import com.blockchain.store.playmarket.data.entities.AppInfo;
import com.blockchain.store.playmarket.interfaces.AppInfoCallback;
import com.blockchain.store.playmarket.ui.app_detail_screen.AppDetailActivity;
import com.blockchain.store.playmarket.ui.transfer_screen.TransferActivity;
import com.blockchain.store.playmarket.utilities.BaseActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.blockchain.store.playmarket.ui.transfer_screen.TransferActivity.RECIPIENT_ARG;

public class MyIcoActivity extends BaseActivity implements MyIcoContract.View, AppInfoCallback {
    private static final String TAG = "MyIcoActivity";

    @BindView(R.id.top_layout_app_name) TextView toolbarTitle;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.error_holder) LinearLayout errorHolder;
    @BindView(R.id.empty_view) TextView emptyView;

    private MyIcoPresenter presenter;
    private IcoListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ico);
        ButterKnife.bind(this);
        toolbarTitle.setText(R.string.my_ico);
        attachPresenter();
    }

    private void attachPresenter() {
        presenter = new MyIcoPresenter();
        presenter.init(this);
        presenter.getMyIcoApps();
    }

    @Override
    public void showProgress(boolean isShow) {
        progressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onIcoAppsReady(ArrayList<AppInfo> apps) {
        if (apps.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            return;
        }
        emptyView.setVisibility(View.GONE);
        adapter = new IcoListAdapter(apps, this, true);
        adapter.setHasStableIds(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onIcoAppsFailed(Throwable throwable) {
        errorHolder.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    @OnClick(R.id.top_layout_back_arrow)
    void onBackArrowClicked() {
        this.onBackPressed();
    }

    @OnClick(R.id.error_view_repeat_btn)
    public void onErrorViewRepeatClicked() {
        errorHolder.setVisibility(View.GONE);
        presenter.getMyIcoApps();
    }

    @Override
    public void onAppInfoClicked(AppInfo appinfo) {
        AppDetailActivity.start(this, appinfo);
    }

    @Override
    public void onAppTransferTokenClicked(AppInfo appinfo) {
        TransferActivity.startAsTokenTransfer(this, appinfo);
    }

    @Override
    public void onCryptoDuelClicked(AppInfo appInfo) {
        AppDetailActivity.start(this, appInfo);
//        startActivity(new Intent(this, IcoLocalActivity.class));
    }

    @Override
    public void onAppInvestClicked(String address) {
        Intent intent = new Intent(this, TransferActivity.class);
        intent.putExtra(RECIPIENT_ARG, address);
        startActivity(intent);
    }


}
