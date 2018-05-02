package net.coding.program.common.util;

import android.view.View;

import net.coding.program.R;
import net.coding.program.route.BlankViewDisplay;

/**
 * Created by chenchao on 2016/12/14.
 * 与 view_exception 配套使用
 */

public class BlankViewHelp extends BlankViewDisplay {

    public static void setBlankLoading(View v, boolean show) {
        showLoading(v, show);
        if (show) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    public static void setErrorBlank(View v, View.OnClickListener onClick) {
        showLoading(v, false);
        BlankViewDisplay.setErrorBlank(v, onClick);
    }

    public static void setBlank(int itemSize, Object fragment, boolean request, View v, View.OnClickListener onClick) {
        showLoading(v, false);
        BlankViewDisplay.setBlank(itemSize, fragment, request, v, onClick);
    }

    public static void setBlank(int itemSize, Object fragment, boolean request, View v, View.OnClickListener onClick, String tipString) {
        showLoading(v, false);
        BlankViewDisplay.setBlank(itemSize, fragment, request, v, onClick, tipString);
    }

    private static void showLoading(View v, boolean show) {
        int loadingVisable = show ? View.VISIBLE : View.GONE;
        int otherVisable = show ? View.GONE : View.VISIBLE;
        v.findViewById(R.id.icon).setVisibility(otherVisable);
        v.findViewById(R.id.message).setVisibility(otherVisable);
        v.findViewById(R.id.btnRetry).setVisibility(otherVisable);
        View loadingView = v.findViewById(R.id.loadingLayout);
        if (loadingView != null) {
            loadingView.setVisibility(loadingVisable);
        }
    }

}
