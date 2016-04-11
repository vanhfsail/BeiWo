package util;

import android.widget.Toast;

import app.BeiWoApplication;

/**
 * Created by huang.fan on 2016-3-21.
 */
public class ToastUtil {
    public static void showShort(String msg) {
        Toast.makeText(BeiWoApplication.getInstance().getContext(), msg, Toast.LENGTH_SHORT).show();
    }
    public static void showIntShort(int msg) {
        Toast.makeText(BeiWoApplication.getInstance().getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
