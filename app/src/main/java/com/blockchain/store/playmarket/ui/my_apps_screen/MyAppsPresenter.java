package com.blockchain.store.playmarket.ui.my_apps_screen;

import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.blockchain.store.playmarket.api.RestApi;
import com.blockchain.store.playmarket.data.entities.App;
import com.blockchain.store.playmarket.data.entities.AppLibrary;
import com.blockchain.store.playmarket.utilities.MyPackageManager;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MyAppsPresenter implements MyAppsContract.Presenter {
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
                appList.add(appLibrary.app);
                appLibraries.add(0, appLibrary);
            } else {
                appLibraries.add(appLibrary);
            }

        }
        addIconAndTitle(appLibraries);
        return appLibraries;
    }

    private void addIconAndTitle(ArrayList<AppLibrary> appLibraries) {
        for (AppLibrary library : appLibraries) {
            library.title = (String) MyPackageManager.get().getApplicationLabel(library.applicationInfo);
            library.icon = library.applicationInfo.loadIcon(MyPackageManager.get());
        }
    }

    private void onAppsReady(ArrayList<AppLibrary> appLibraries) {
        Log.d("testtt", "onAppsReady: " + appList.size());
        view.onAppsReady(appLibraries);
    }


    private void onAppsFailed(Throwable throwable) {
        view.onAppsFailed(throwable);
    }

}
