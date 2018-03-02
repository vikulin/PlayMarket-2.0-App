package com.blockchain.store.playmarket.ui.navigation_view;

import android.util.Log;

import com.blockchain.store.playmarket.api.RestApi;
import com.blockchain.store.playmarket.data.entities.BalanceResponse;
import com.blockchain.store.playmarket.data.entities.ChangellyBaseBody;
import com.blockchain.store.playmarket.data.entities.ChangellyCurrenciesResponse;
import com.blockchain.store.playmarket.data.entities.ChangellyMinimumAmountResponse;
import com.blockchain.store.playmarket.utilities.AccountManager;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Crypton04 on 09.02.2018.
 */

public class NavigationViewPresenter implements NavigationViewContract.Presenter {
    private static final String TAG = "NavigationViewPresenter";

    NavigationViewContract.View view;

    @Override
    public void init(NavigationViewContract.View view) {
        this.view = view;
    }

    @Override
    public void loadUserBalance() {
        String accountAddress = AccountManager.getAddress().getHex();
        Log.d(TAG, "loadUserBalance: account address " + accountAddress);
        RestApi.getServerApi().getBalance(accountAddress)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> view.showUserBalanceProgress(true))
                .doOnTerminate(() -> view.showUserBalanceProgress(false))
                .subscribe(this::onBalanceReady, this::onBalanceFail);
    }

    private void onBalanceReady(String userBalance) {
        view.onBalanceReady(userBalance);
    }

    private void onBalanceFail(Throwable throwable) {
        view.onBalanceFail(throwable);
    }

}
