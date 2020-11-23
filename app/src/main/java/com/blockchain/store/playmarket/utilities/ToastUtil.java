package com.blockchain.store.playmarket.utilities;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;

/**
 * Created by Crypton04 on 24.01.2018.
 */

public class ToastUtil {
    private static Toast toast;
    private static Context context;

    public static void showToast(String msg) {
        if (toast == null) {
            toast = makeEmptyToast();
        }
        toast.setText(msg);
        show();
    }

    public static void showToast(@StringRes int msg) {
        if (toast == null) {
            toast = makeEmptyToast();
        }
        toast.setText(msg);
        show();
    }

    private static Toast makeEmptyToast() {
        return Toast.makeText(context, null, Toast.LENGTH_LONG);
    }

    private static void show() {
        if (toast.getView().isShown()) return;
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    public static void setContext(Context context) {
        ToastUtil.context = context;
    }
}
