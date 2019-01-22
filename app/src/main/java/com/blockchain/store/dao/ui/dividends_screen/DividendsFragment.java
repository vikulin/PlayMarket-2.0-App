package com.blockchain.store.dao.ui.dividends_screen;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.blockchain.store.dao.data.entities.DaoToken;
import com.blockchain.store.dao.repository.DaoTransactionRepository;
import com.blockchain.store.dao.ui.dao_activity.DaoActivity;
import com.blockchain.store.playmarket.R;
import com.blockchain.store.playmarket.adapters.DaoTokenAdapter;
import com.blockchain.store.playmarket.api.RestApi;
import com.blockchain.store.playmarket.data.entities.PurchaseAppResponse;
import com.blockchain.store.playmarket.repositories.TokenRepository;
import com.blockchain.store.playmarket.repositories.TransactionInteractor;
import com.blockchain.store.playmarket.ui.main_list_screen.MainMenuActivity;
import com.blockchain.store.playmarket.utilities.AccountManager;
import com.blockchain.store.playmarket.utilities.Constants;
import com.blockchain.store.playmarket.utilities.DialogManager;
import com.blockchain.store.playmarket.utilities.crypto.CryptoUtils;

import org.ethereum.geth.Transaction;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DividendsFragment extends Fragment {

    @BindView(R.id.error_view_repeat_btn) Button error_view_repeat_btn;
    @BindView(R.id.error_holder) LinearLayout errorHolder;
    private static final String TAG = "DividendsFragment";

    @BindView(R.id.background) View background;
    @BindView(R.id.back_arrow) ImageView backArrow;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    DaoTokenAdapter adapter;

    public DividendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dividends, container, false);
        ButterKnife.bind(this, view);
        getTokens();
        return view;
    }

    @OnClick(R.id.back_arrow)
    void onBackArrowClicked() {
        getActivity().onBackPressed();
    }

    @OnClick(R.id.error_holder)
    void onRepeatLayotClicked() {
        getTokens();
    }

    private void getTokens() {
        errorHolder.setVisibility(View.GONE);
        DaoTransactionRepository.getTokens().subscribe(this::onTokensReady, this::onTokensError);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onTokensError(Throwable throwable) {
        errorHolder.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void onTokensReady(List<DaoToken> daoTokens) {
        errorHolder.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        initAdapter(daoTokens);
    }

    private void initAdapter(List<DaoToken> daoTokens) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new DaoTokenAdapter(daoTokens, new DaoActivity.DaoAdapterCallback() {
            @Override
            public void onPmTokenClicked(DaoToken daoToken) {
                ((MainMenuActivity) getActivity()).onTokenTransferClicked(daoToken);
            }

            @Override
            public void onDaoTokenClicked(DaoToken daoToken) {
                new DialogManager().showDividendsDialog(getActivity(), new DialogManager.DividendCallback() {
                    @Override
                    public void onAccountUnlocked() {
                        TokenRepository.addToken(daoToken);
                        if (daoToken.isNeedSecondTx()) {
                            runDoubleTx(daoToken);
                        } else {
                            runSingleTx(daoToken);
                        }
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void runSingleTx(DaoToken daoToken) {
        RestApi.getServerApi().getAccountInfo(AccountManager.getAddress().getHex())
                .flatMap(result -> {
                    try {
                        Transaction tx = CryptoUtils.generateDaoWithdraw(result, daoToken);
                        TransactionInteractor.addToJobSchedule(tx.getHash().getHex(), Constants.TransactionTypes.GET_DIVIDENDS);
                        return RestApi.getServerApi().deployTransaction(CryptoUtils.getRawTransaction(tx));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::receive, this::transferFailed);
    }

    private void receive(PurchaseAppResponse purchaseAppResponse) {
        Log.d(TAG, "receive() called with: purchaseAppResponse = [" + purchaseAppResponse + "]");
    }

    private void runDoubleTx(DaoToken daoToken) {
        RestApi.getServerApi().getAccountInfo(AccountManager.getAddress().getHex())
                .flatMap(result -> {
                    try {
                        Pair<Transaction, Transaction> stringStringPair = CryptoUtils.generateDaoTransferTransactions(result, daoToken);
                        String rawTransaction = CryptoUtils.getRawTransaction(stringStringPair.first);
                        String rawSecondTransaction = CryptoUtils.getRawTransaction(stringStringPair.second);
                        TransactionInteractor.addToJobSchedule(stringStringPair.first.getHash().getHex(), stringStringPair.second.getHash().getHex(), rawSecondTransaction, Constants.TransactionTypes.GET_DIVIDENDS);
                        return RestApi.getServerApi().deployTransaction(rawTransaction);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("111");
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::transferSuccess, this::transferFailed);
    }

    private void transferFailed(Throwable throwable) {
        Log.d(TAG, "transferFailed() called with: throwable = [" + throwable + "]");
    }

    private void transferSuccess(PurchaseAppResponse purchaseAppResponse) {
        Log.d(TAG, "transferSuccess() called with: purchaseAppResponse = [" + purchaseAppResponse + "]");
    }

}
