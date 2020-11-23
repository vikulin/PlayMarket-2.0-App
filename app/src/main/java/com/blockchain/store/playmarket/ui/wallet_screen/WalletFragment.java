package com.blockchain.store.playmarket.ui.wallet_screen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blockchain.store.dao.data.entities.DaoToken;
import com.blockchain.store.dao.ui.DaoConstants;
import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.adapters.DaoTokenAdapter;
import com.blockchain.store.playmarket.data.entities.UserBalance;
import com.blockchain.store.playmarket.data.types.EthereumPrice;
import com.blockchain.store.playmarket.interfaces.DaoAdapterCallback;
import com.blockchain.store.playmarket.interfaces.NavigationCallback;
import com.blockchain.store.playmarket.ui.exchange_screen.ExchangeActivity;
import com.blockchain.store.playmarket.ui.main_list_screen.MainMenuActivity;
import com.blockchain.store.playmarket.ui.qr_screen.QrActivity;
import com.blockchain.store.playmarket.ui.transaction_history_screen.TransactionHistoryActivity;
import com.blockchain.store.playmarket.ui.transfer_screen.TransferActivity;
import com.blockchain.store.playmarket.utilities.AccountManager;
import com.blockchain.store.playmarket.utilities.ToastUtil;
import com.blockchain.store.playmarket.utilities.data.ClipboardUtils;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalletFragment extends Fragment implements WalletContract.View, DaoAdapterCallback {

    private static final String TAG = "WalletFragment";
    private NavigationCallback navigationCallback;
    private WalletPresenter presenter;
    private DaoTokenAdapter adapter;

    @BindView(R.id.close_button) ImageView close_button;
    @BindView(R.id.textView1) TextView textView1;
    @BindView(R.id.textView2) TextView textView2;
    @BindView(R.id.address_textView) TextView userAddress;
    @BindView(R.id.qr_button) ImageView qr_button;
    @BindView(R.id.copy_button) ImageView copy_button;
    @BindView(R.id.addEth_button) ImageView addEth_button;
    @BindView(R.id.transfer_button) ImageView transfer_button;
    @BindView(R.id.history_button) ImageView history_button;
    @BindView(R.id.imageView1) ImageView imageView1;
    @BindView(R.id.ethBalance_textView) TextView ethBalance;
    @BindView(R.id.refreshBalance_button) ImageView refreshBalance;
    @BindView(R.id.rubBalance_textView) TextView balanceInLocal;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.view) View view;
    @BindView(R.id.button) Button button;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.error_view_repeat_btn) Button error_view_repeat_btn;
    @BindView(R.id.error_holder) LinearLayout errorHolder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        navigationCallback = (NavigationCallback) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initAdapter();
        attachPresenter();
        userAddress.setText(Objects.requireNonNull(AccountManager.getAddress()).getHex());
    }

    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setMeasurementCacheEnabled(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new DaoTokenAdapter(new ArrayList<>(), this, true);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPmTokenClicked(DaoToken daoToken) {
        ((MainMenuActivity) Objects.requireNonNull(getActivity())).onTokenTransferClicked(daoToken);
    }

    @Override
    public void onDaoTokenClicked(DaoToken daoToken) {
        if (daoToken.address.equalsIgnoreCase(DaoConstants.CRYPTO_DUEL_CONTRACT)) {
            ((MainMenuActivity) Objects.requireNonNull(getActivity())).onTokenTransferClicked(daoToken);
        } else {
            TransferActivity.startAsTokenTransfer(Objects.requireNonNull(getActivity()), daoToken);
        }
    }

    private void attachPresenter() {
        presenter = new WalletPresenter();
        presenter.init(this);
        loadUserBalance();
        getWalletTokens();
    }

    private void getWalletTokens() {
        presenter.getWalletTokens();
        errorHolder.setVisibility(View.GONE);
    }

    @Override
    public void onTokenNext(DaoToken daoToken) {
        errorHolder.setVisibility(View.GONE);
        adapter.updateToken(daoToken);
    }

    @Override
    public void onTokensComplete() {
    }

    @Override
    public void onTokenError(Throwable throwable) {
        errorHolder.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLocalTokensAdded(ArrayList<DaoToken> tokens) {
        adapter.setTokens(tokens);
    }

    private void loadUserBalance() {
        presenter.loadUserBalance();
        progressBar.setVisibility(View.VISIBLE);
        refreshBalance.setVisibility(View.GONE);
    }

    @Override
    public void onBalanceReady(UserBalance balance) {
        AccountManager.setUserBalance(balance.balanceInWei);
        ethBalance.setText(new EthereumPrice(balance.balanceInWei).inEther().toString() + " ETH");
        balanceInLocal.setText(String.format(getString(R.string.local_currency), balance.symbol, balance.getFormattedLocalCurrency()));
        progressBar.setVisibility(View.GONE);
        refreshBalance.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBalanceFail(Throwable throwable) {
        progressBar.setVisibility(View.GONE);
        refreshBalance.setVisibility(View.VISIBLE);
    }

    @Override
    public void showUserBalanceProgress(boolean isShow) {
        progressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.close_button)
    void onCloseButtonClicked() {
        if (getActivity() != null) getActivity().onBackPressed();
    }

    @OnClick(R.id.button)
    void onItemClick() {
        navigationCallback.onTokenTransferClicked(new DaoToken());
    }

    @OnClick(R.id.qr_button)
    void onQrButtonClicked() {
        startActivity(new Intent(getActivity(), QrActivity.class));
    }

    @OnClick(R.id.copy_button)
    void onCopyButtonClicked() {
        ClipboardUtils.copyToClipboard(Objects.requireNonNull(getActivity()), userAddress.getText().toString().replaceAll(" ", ""));
        ToastUtil.showToast(R.string.address_copied);
    }

    @OnClick(R.id.addEth_button)
    void onAddEthClicked() {
        startActivity(new Intent(getActivity(), ExchangeActivity.class));
    }

    @OnClick(R.id.transfer_button)
    void onTransferClicked() {
        startActivity(new Intent(getActivity(), TransferActivity.class));
    }

    @OnClick(R.id.history_button)
    void onHistoryClicked() {
        startActivity(new Intent(getActivity(), TransactionHistoryActivity.class));
    }

    @OnClick(R.id.error_view_repeat_btn)
    void onErrorHolderClicked() {
        getWalletTokens();
    }

    @OnClick(R.id.refreshBalance_button)
    void onRefreshBalanceClicked() {
        loadUserBalance();
    }

    @OnClick(R.id.change_account)
    void onchangeAccountClicked() {
        navigationCallback.onChangeAccountClicked();
    }

    @OnClick(R.id.fab)
    void onFabClicked() {
        navigationCallback.onAddTokenClicked();
    }

}
