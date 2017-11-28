package net.coding.program.common.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.network.NetworkImpl;

import org.json.JSONObject;

/**
 * Created by chenchao on 15/8/13.
 */
public class SingleToast {

    Context mContext;
    Toast mToast;

    public SingleToast(Context mContext) {
        this.mContext = mContext;
        mToast = new Toast(mContext);
        mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
    }

    public static void showMiddleToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showMiddleToast(Context context, int message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showErrorMsg(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showErrorMsg(Context context, int code, JSONObject json) {
        if (code == NetworkImpl.NETWORK_ERROR) {
            SingleToast.showMiddleToast(context, R.string.connect_service_fail);
        } else {
            String msg = Global.getErrorMsg(json);
            if (!msg.isEmpty()) {
                SingleToast.showMiddleToast(context, msg);
            }
        }
    }

    public void showButtomToast(String msg) {
        mToast.setText(msg);
        mToast.setGravity(Gravity.BOTTOM, 0, mContext.getResources().getDimensionPixelOffset(R.dimen.toast_y));
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void showButtomToast(int messageId) {
        mToast.setText(messageId);
        mToast.setGravity(Gravity.BOTTOM, 0, mContext.getResources().getDimensionPixelOffset(R.dimen.toast_y));
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void showMiddleToast(int id) {
        mToast.setText(id);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void showMiddleToast(String msg) {
        mToast.setText(msg);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void showMiddleToast(String msg, int duration) {
        mToast.setText(msg);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setDuration(duration);
        mToast.show();
    }

    public void showMiddleToastLong(String msg) {
        mToast.setText(msg);
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.show();
    }

}
