package com.blockchain.store.playmarket.ui.transaction_history_screen;

import android.os.Bundle;

import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.data.entities.TransactionModel;
import com.blockchain.store.playmarket.ui.transaction_history_screen.TransactionHistoryActivityContract.View;
import com.blockchain.store.playmarket.utilities.BaseActivity;
import com.blockchain.store.playmarket.utilities.Constants;
import com.blockchain.store.playmarket.utilities.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionHistoryActivity extends BaseActivity implements View {

    private static final String TAG = "TransactionHistoryActiv";

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    TransactionHistoryActivityPresenter presenter;
    TransactionHistoryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this).get(TransactionHistoryViewModel.class);
        initPresenter();
        setViews();
        loadData();
        initViewPager();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadData() {
        ArrayList<TransactionModel> allTransaction = presenter.getAllTransaction();
        viewModel.transactionModels.setValue(allTransaction);

    }

    private void initPresenter() {
        presenter = new TransactionHistoryActivityPresenter();
        presenter.init(this);

    }

    private void initViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(TransactionHistoryFragment.instance(Constants.TransactionStatus.ALL), getString(R.string.history_title_history));
        viewPagerAdapter.addFragment(TransactionHistoryFragment.instance(Constants.TransactionStatus.PENDING), getString(R.string.history_title_pending));
        viewPagerAdapter.addFragment(TransactionHistoryFragment.instance(Constants.TransactionStatus.SUCCEES), getString(R.string.history_title_succeed));
        viewPagerAdapter.addFragment(TransactionHistoryFragment.instance(Constants.TransactionStatus.FAILED), getString(R.string.history_title_failed));

        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setViews() {
        title.setText(R.string.transaction_history_title);
    }

    @OnClick(R.id.top_layout_back_arrow)
    void onBackArrowClicked() {
        this.onBackPressed();
    }


}
