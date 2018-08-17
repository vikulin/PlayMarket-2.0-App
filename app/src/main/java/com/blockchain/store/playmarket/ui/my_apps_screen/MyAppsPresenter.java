package com.blockchain.store.playmarket.ui.my_apps_screen;

import android.content.pm.ApplicationInfo;

import com.blockchain.store.playmarket.api.RestApi;
import com.blockchain.store.playmarket.data.entities.App;
import com.blockchain.store.playmarket.data.entities.AppLibrary;
import com.blockchain.store.playmarket.interfaces.NotificationManagerCallbacks;
import com.blockchain.store.playmarket.notification.NotificationManager;
import com.blockchain.store.playmarket.utilities.Constants;
import com.blockchain.store.playmarket.utilities.MyPackageManager;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MyAppsPresenter implements MyAppsContract.Presenter, NotificationManagerCallbacks {
    private MyAppsContract.View view;
    private String arrayOfInstalledApps;
    private ArrayList<App> appList = new ArrayList<>();

    @Override
    public void init(MyAppsContract.View view) {
        this.view = view;
        arrayOfInstalledApps = MyPackageManager.prepareApplicationInfoForRequest();
    }

    public void getApps() {
        RestApi.getServerApi().getAppsByPackage(arrayOfInstalledApps)
                .map(this::mapWithLocalApps)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> view.showLoading(true))
                .doOnTerminate(() -> view.showLoading(false))
                .subscribe(this::onAppsReady, this::onAppsFailed);
    }

    private ArrayList<AppLibrary> mapWithLocalApps(ArrayList<App> apps) {
        ArrayList<AppLibrary> appLibraries = new ArrayList<>();
        List<ApplicationInfo> allInstalledApps = MyPackageManager.getAllInstalledApps();
        for (ApplicationInfo applicationInfo : allInstalledApps) {
            AppLibrary appLibrary = new AppLibrary();
            appLibrary.applicationInfo = applicationInfo;
            boolean isHasLocalCopy = false;
            for (App app : apps) {
                if (applicationInfo.packageName.equalsIgnoreCase(app.packageName)) {
                    appLibrary.app = app;
                    isHasLocalCopy = true;
                }

            }
            appLibrary.isHasUpdate = MyPackageManager.isAppHasUpdate(appLibrary.app);
            appLibrary.versionName = MyPackageManager.getVersionNameByPackageName(appLibrary.applicationInfo.packageName);
            if (isHasLocalCopy && appLibrary.isHasUpdate) {
                loadState(appLibrary);
                appLibraries.add(0, appLibrary);
            } else {
                appLibraries.add(appLibrary);
            }

        }

        addIconAndTitle(appLibraries);
        return appLibraries;
    }

    private void loadState(AppLibrary appLibrary) {
        if (NotificationManager.getManager().isCallbackAlreadyRegistered(appLibrary.app)) {
            NotificationManager.getManager().registerCallback(appLibrary.app, this);
        }
    }

    private void addIconAndTitle(ArrayList<AppLibrary> appLibraries) {
        for (AppLibrary library : appLibraries) {
            library.title = (String) MyPackageManager.get().getApplicationLabel(library.applicationInfo);
            library.icon = library.applicationInfo.loadIcon(MyPackageManager.get());
        }
    }

    private void onAppsReady(ArrayList<AppLibrary> appLibraries) {
        view.onAppsReady(appLibraries);
    }


    private void onAppsFailed(Throwable throwable) {
        view.onAppsFailed(throwable);
    }

    public void onActionItemClicked(AppLibrary appLibrary, int position) {
        NotificationManager.getManager().registerCallback(appLibrary.app, this);
        new MyPackageManager().startDownloadApkService(appLibrary.app, true);
    }

    @Override
    public void onAppDownloadStarted(App app) {
        view.updateApp(app, 0, Constants.APP_STATE.STATE_DOWNLOAD_STARTED);
    }

    @Override
    public void onAppDownloadProgressChanged(App app, int progress) {
        view.updateApp(app, progress, Constants.APP_STATE.STATE_DOWNLOADING);
    }

    @Override
    public void onAppDownloadSuccessful(App app) {
        view.updateApp(app, 0, Constants.APP_STATE.STATE_DOWNLOADING);
    }

    @Override
    public void onAppDownloadError(App app, String message) {
        view.updateApp(app, 0, Constants.APP_STATE.STATE_DOWNLOAD_ERROR);
    }

    public void onDestroy(ArrayList<AppLibrary> allItems) {
        for (AppLibrary library : allItems) {
            NotificationManager.getManager().removeCallback(library.app, this);
        }
    }
}
