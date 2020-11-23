package com.blockchain.store.playmarket.ui.exchange_screen;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.blockchain.store.playmarket.data.entities.ChangellyCurrency;

import java.util.ArrayList;

/**
 * Created by Crypton04 on 27.02.2018.
 */

public class ExchangeActivityViewModel extends ViewModel {
    public MutableLiveData<ChangellyCurrency> chosenCurrency = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ChangellyCurrency>> changellyCurrencies = new MutableLiveData<>();
    public MutableLiveData<String> userEnteredAmount = new MutableLiveData<>();
    public MutableLiveData<String> payinAddress = new MutableLiveData<>();
    public MutableLiveData<String> minEnteredAmount = new MutableLiveData<>();
    public MutableLiveData<String> estimatedAmount = new MutableLiveData<>();
}
