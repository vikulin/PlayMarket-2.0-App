package com.blockchain.store.playmarket;

import android.content.res.Configuration;
import android.text.TextUtils;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.room.Room;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.blockchain.store.dao.database.DaoDatabase;
import com.blockchain.store.dao.repository.DaoTokenRepository;
import com.blockchain.store.playmarket.data.content.AppsDispatcher;
import com.blockchain.store.playmarket.data.content.AppsManager;
import com.blockchain.store.playmarket.data.entities.AppBuyTransactionModel;
import com.blockchain.store.playmarket.data.entities.InvestTransactionModel;
import com.blockchain.store.playmarket.data.entities.SendEthereumTransactionModel;
import com.blockchain.store.playmarket.data.entities.SendReviewTransactionModel;
import com.blockchain.store.playmarket.data.entities.SendTokenTransactionModel;
import com.blockchain.store.playmarket.utilities.AccountManager;
import com.blockchain.store.playmarket.utilities.Constants;
import com.blockchain.store.playmarket.utilities.FingerprintUtils;
import com.blockchain.store.playmarket.utilities.LocaleUtils;
import com.blockchain.store.playmarket.utilities.ToastUtil;
import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.Parser;

import java.lang.reflect.Type;
import java.util.Locale;

import io.ethmobile.ethdroid.KeyManager;
import io.fabric.sdk.android.Fabric;

import static com.blockchain.store.playmarket.utilities.Constants.ENCRYPTED_PASSWORD;

/**
 * Created by Crypton04 on 24.01.2018.
 */

public class Application extends MultiDexApplication {
    private static final String TAG = "Application";

    private static AppsDispatcher appsDispatcher;
    private static AppsManager appsManager;
    private static Application instance;
    private static PinpointManager pinpointManager;
    private static DaoDatabase daoDatabase;
    private static DaoTokenRepository daoTokenRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        instance = this;

        MultiDex.install(this);
        ToastUtil.setContext(this);
        AccountManager.setKeyManager(KeyManager.newKeyManager(getFilesDir().getAbsolutePath()));

        daoDatabase = Room.databaseBuilder(this, DaoDatabase.class, "DaoDB")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        daoTokenRepository = new DaoTokenRepository();
        initHawk();
        performMigrationIntoMultiAccounting();
        setUpFresco();
        setUpAWS();
        setUpLocale();

    }


    private void setUpLocale() {
        if (Hawk.contains(Constants.SETTINGS_USER_LOCALE)) {
            LocaleUtils.setLocale(new Locale(Hawk.get(Constants.SETTINGS_USER_LOCALE)));
        } else {
            Locale defaultLocale = Locale.getDefault();
            LocaleUtils.setLocale(defaultLocale);
            Hawk.put(Constants.SETTINGS_USER_LOCALE, defaultLocale.getLanguage());
        }
        LocaleUtils.updateConfig(this, getBaseContext().getResources().getConfiguration());

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.updateConfig(this, newConfig);
    }

    private void performMigrationIntoMultiAccounting() {
        String password = Hawk.get(ENCRYPTED_PASSWORD);
        if (password != null && AccountManager.getAddress() != null) {
            FingerprintUtils.addEncryptedPassword(password);
        }
    }


    private void setUpFresco() {
        Fresco.initialize(this);
    }

    public static AppsDispatcher getAppsDispatcher() {
        if (appsDispatcher == null) {
            appsDispatcher = new AppsDispatcher();
        }
        return appsDispatcher;
    }

    public static AppsManager getAppsManager() {
        if (appsManager == null) {
            appsManager = new AppsManager();
        }
        return appsManager;
    }

    public static DaoDatabase getDaoDatabase() {
        return daoDatabase;
    }

    public static DaoTokenRepository getDaoTokenRepository() {
        return daoTokenRepository;
    }

    public static Application getInstance() {
        return instance;
    }


    public static boolean isForeground() {
        return instance == null;
    }

    private void setUpAWS() {
        AWSMobileClient.getInstance().initialize(this).execute();
        PinpointConfiguration config = new PinpointConfiguration(
                this,
                AWSMobileClient.getInstance().getCredentialsProvider(),
                AWSMobileClient.getInstance().getConfiguration());
        pinpointManager = new PinpointManager(config);
        pinpointManager.getSessionClient().startSession();
        pinpointManager.getAnalyticsClient().submitEvents();

    }

    public static void stopAnalytic() {
        pinpointManager.getSessionClient().stopSession();
        pinpointManager.getAnalyticsClient().submitEvents();
    }

    private void initHawk() {
        Hawk.init(this).setParser(new Parser() {
            @Override
            public <T> T fromJson(String content, Type type) {
                if (TextUtils.isEmpty(content)) {
                    return null;
                }
                try {
                    JsonObject object = new Gson().fromJson(content, JsonObject.class);
                    if (object.has("TransactionType")) {
                        int viewType = object.get("TransactionType").getAsInt();

                        if (viewType == Constants.TransactionTypes.BUY_APP.ordinal()) {
                            return new Gson().fromJson(content, (Class<T>) AppBuyTransactionModel.class);
                        }
                        if (viewType == Constants.TransactionTypes.INVEST.ordinal()) {
                            return new Gson().fromJson(content, (Class<T>) InvestTransactionModel.class);
                        }
                        if (viewType == Constants.TransactionTypes.TRANSFER.ordinal()) {
                            return new Gson().fromJson(content, (Class<T>) SendEthereumTransactionModel.class);
                        }
                        if (viewType == Constants.TransactionTypes.TRANSFER_TOKEN.ordinal()) {
                            return new Gson().fromJson(content, (Class<T>) SendTokenTransactionModel.class);
                        }
                        if (viewType == Constants.TransactionTypes.SEND_REVIEW.ordinal()) {
                            return new Gson().fromJson(content, (Class<T>) SendReviewTransactionModel.class);
                        }
                    }
                } catch (Exception ignored) {
                }

                return new Gson().fromJson(content, type);
            }

            @Override
            public String toJson(Object body) {
                return new Gson().toJson(body);
            }
        }).build();
    }
}
