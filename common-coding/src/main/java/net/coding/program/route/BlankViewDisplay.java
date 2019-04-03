package net.coding.program.route;

import android.view.View;

import net.coding.program.R;

/**
 * Created by chaochen on 14-10-24.
 * 内容为空白时显示的提示语
 */
public class BlankViewDisplay {

    public static final String MY_PROJECT_BLANK = "您还木有项目呢，赶快起来创建吧～";
    public static final String OTHER_PROJECT_BLANK = "这个人很懒，一个项目都木有～";
    public static final String MY_SUBJECT_BLANK = "您还没有参与过话题呢～";
    public static final String OTHER_SUBJECT_BLANK = "TA 还没有参与过话题呢～";
    public static final String OTHER_MALL_ORDER_BLANK = "还没有订单记录~";
    public static final String OTHER_MALL_ORDER_BLANK_UNSEND = "没有未发货的订单记录~";
    public static final String OTHER_MALL_ORDER_BLANK_ALREADYSEND = "没有已发货的订单记录~";
    public static final String OTHER_MALL_EXCHANGE_BLANK = "还没有可兑换的商品呢~";

    public static void setErrorBlank(View v, View.OnClickListener onClick) {
        View btn = v.findViewById(R.id.btnRetry);
        btn.setVisibility(View.VISIBLE);
        btn.setOnClickListener(onClick);

        callback.setBlank(0, null, false, v, "", 0);
    }

    public static void setBlank(int itemSize, Object fragment, boolean request, View v, View.OnClickListener onClick) {
        setBlank(itemSize, fragment, request, v, onClick, "");
    }

    public static void setBlank(int itemSize, Object fragment, boolean request, View v, View.OnClickListener onClick, String tipString) {
        setBlank(itemSize, fragment, request, v, onClick, tipString, 0);
    }

    public static void setBlank(int itemSize, Object fragment, boolean request, View v, View.OnClickListener onClick, String tipString, int iconId) {
        // 有些界面不需要显示blank状态
        if (v == null) {
            return;
        }

        View btn = v.findViewById(R.id.btnRetry);
        if (request) {
            btn.setVisibility(View.GONE);
        } else {
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(onClick);
        }

        setBlank(itemSize, fragment, request, v, tipString, iconId);
    }

    private static void setBlank(int itemSize, Object fragment, boolean request, View v, String tipString, int iconId) {
        callback.setBlank(itemSize, fragment, request, v, tipString, iconId);
    }

    public static BlankCallback callback;

    public interface BlankCallback {
        void setBlank(int itemSize, Object fragment, boolean request, View v, String tipString, int iconId);
    }
}
