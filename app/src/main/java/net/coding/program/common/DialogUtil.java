package net.coding.program.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.coding.program.R;

import java.util.ArrayList;


/**
 * 弹出框工具类
 *
 * @author yangzhen
 */
public class DialogUtil {
    private static final String TAG = DialogUtil.class.getSimpleName();

    private static Animation loadingLogoAnimation;
    private static Animation loadingRoundAnimation;

    /**
     * 初始化进度条dialog
     *
     * @param activity
     * @return
     */
    public static LoadingPopupWindow initProgressDialog(Activity activity, OnDismissListener onDismissListener) {
        if (activity == null || activity.isFinishing()) {
            return null;
        }

        // 获得背景（6个图片形成的动画）
        //AnimationDrawable animDance = (AnimationDrawable) imgDance.getBackground();

        //final PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        final LoadingPopupWindow popupWindow = new LoadingPopupWindow(activity);
        ColorDrawable cd = new ColorDrawable(-0000);
        popupWindow.setBackgroundDrawable(cd);
        popupWindow.setTouchable(true);
        popupWindow.setOnDismissListener(onDismissListener);

        popupWindow.setFocusable(true);
        //animDance.start();
        return popupWindow;
    }

    /**
     * 显示进度条对话框
     *
     * @param activity
     * @param popupWindow
     * @param title
     */
    public static void showProgressDialog(Activity activity, LoadingPopupWindow popupWindow, String title) {
        if ((activity == null || activity.isFinishing()) || (popupWindow == null)) {
            return;
        }

        final LoadingPopupWindow tmpPopupWindow = popupWindow;
        View popupView = popupWindow.getContentView();
        if (popupView != null) {
            TextView tvTitlename = (TextView) popupView.findViewById(R.id.tv_titlename);
            if (tvTitlename != null && !title.isEmpty()) {
                tvTitlename.setText(title);
            }
        }

        if (popupWindow != null && !popupWindow.isShowing()) {
            final View rootView1 = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView1.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        tmpPopupWindow.showAtLocation(rootView1, Gravity.CENTER, 0, 0);
                        tmpPopupWindow.startAnimation();
                    } catch (Exception e) {
                        Global.errorLog(e);
                    }
                }
            });

        }
    }

    /**
     * 隐藏对话框
     *
     * @param popupWindow
     */
    public static void hideDialog(final PopupWindow popupWindow) {
        if (popupWindow != null) {
            popupWindow.getContentView().post(new Runnable() {
                @Override
                public void run() {
                    if (popupWindow != null && popupWindow.isShowing())
                        try {
                            popupWindow.dismiss();
                        } catch (Exception e) {
                        }
                }
            });
        }
    }

    /**
     * 立即关闭对话框， 在对话框是用来确认是否关闭某个Activity的时候上面的方法有概率会报错
     *
     * @param popupWindow
     */
    public static void hideDialogNow(PopupWindow popupWindow) {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public static class LoadingPopupWindow extends PopupWindow {

        ImageView loadingLogo;
        ImageView loadingRound;

        public LoadingPopupWindow(Activity activity) {
            super(activity.getLayoutInflater().inflate(R.layout.common_loading, null), RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, false);
            this.loadingLogo = (ImageView) getContentView().findViewById(R.id.loading_logo);
            this.loadingRound = (ImageView) getContentView().findViewById(R.id.loading_round);

            if (loadingLogoAnimation == null) {
                loadingLogoAnimation = AnimationUtils.loadAnimation(activity, R.anim.loading_alpha);
            }
            if (loadingRoundAnimation == null) {
                loadingRoundAnimation = AnimationUtils.loadAnimation(activity, R.anim.loading_rotate);
            }
        }

        public void startAnimation() {
            loadingRoundAnimation.setStartTime(500L);//不然会跳帧
            loadingRound.setAnimation(loadingRoundAnimation);
            loadingLogo.startAnimation(loadingLogoAnimation);
        }
    }

    public static class BottomPopupWindow extends PopupWindow {
        public TextView tvTitle;
        public ListView listView;
        public BottomPopupAdapter adapter;

        public BottomPopupWindow(Activity activity) {
            super(activity.getLayoutInflater().inflate(R.layout.popup_attachment, null), RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            getContentView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            tvTitle = (TextView) getContentView().findViewById(R.id.title);
            listView = (ListView) getContentView().findViewById(R.id.listView);
            adapter = new BottomPopupAdapter(activity);

            listView.setAdapter(adapter);

        }
    }

    public static BottomPopupWindow initBottomPopupWindow(final Activity mActivity, String title, ArrayList<BottomPopupItem> popupItemArrayList, AdapterView.OnItemClickListener onItemClickListener) {

        final BottomPopupWindow mAttachmentPopupWindow = new BottomPopupWindow(mActivity);
        mAttachmentPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAttachmentPopupWindow.setOutsideTouchable(true);
        //mAttachmentPopupWindow.setAnimationStyle(R.style.PopupReversalAnimation);// android.R.style.Animation_Dialog
        mAttachmentPopupWindow.setTouchable(true);
        mAttachmentPopupWindow.setFocusable(true);

        mAttachmentPopupWindow.tvTitle.setText(title);
        mAttachmentPopupWindow.adapter.addAll(popupItemArrayList);
        mAttachmentPopupWindow.listView.setOnItemClickListener(onItemClickListener);
        return mAttachmentPopupWindow;
    }

    public static class BottomPopupItem {
        public String title;
        public int iconRes;
        public boolean enabled = true;

        public BottomPopupItem(String title, int iconRes) {
            this.title = title;
            this.iconRes = iconRes;
        }
    }

    public static class BottomPopupAdapter extends ArrayAdapter<BottomPopupItem> {

        public BottomPopupAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.popup_item, null);
            }
            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(getItem(position).title);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageResource(getItem(position).iconRes);
            //title.setCompoundDrawablesWithIntrinsicBounds(getItem(position).iconRes, 0, 0, 0);

            title.setEnabled(getItem(position).enabled);
            icon.setEnabled(getItem(position).enabled);
            return convertView;
        }

    }

    public static class RightTopPopupWindow extends PopupWindow {
        public ListView listView;
        public RightTopPopupAdapter adapter;

        public RightTopPopupWindow(Activity activity) {
            super(activity.getLayoutInflater().inflate(R.layout.popup_top_right, null), RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            listView = (ListView) getContentView().findViewById(R.id.listView);
            adapter = new RightTopPopupAdapter(activity);
            listView.setAdapter(adapter);

        }
    }

    public static RightTopPopupWindow initRightTopPopupWindow(final Activity mActivity, ArrayList<RightTopPopupItem> popupItemArrayList, AdapterView.OnItemClickListener onItemClickListener) {

        final RightTopPopupWindow mRightTopPopupWindow = new RightTopPopupWindow(mActivity);
        mRightTopPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mRightTopPopupWindow.setOutsideTouchable(true);
        //mRightTopPopupWindow.setAnimationStyle(R.style.PopupReversalAnimation);// android.R.style.Animation_Dialog
        mRightTopPopupWindow.setTouchable(true);
        mRightTopPopupWindow.setFocusable(true);

        mRightTopPopupWindow.adapter.addAll(popupItemArrayList);
        mRightTopPopupWindow.listView.setOnItemClickListener(onItemClickListener);
        return mRightTopPopupWindow;
    }

    public static class RightTopPopupItem {
        public String title;
        public int iconRes;
        public boolean enabled = true;

        public RightTopPopupItem(String title, int iconRes) {
            this.title = title;
            this.iconRes = iconRes;
        }
    }

    public static class RightTopPopupAdapter extends ArrayAdapter<RightTopPopupItem> {

        public RightTopPopupAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.popup_top_right_item, null);
            }
            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(getItem(position).title);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageResource(getItem(position).iconRes);
            title.setEnabled(getItem(position).enabled);
            icon.setEnabled(getItem(position).enabled);
            return convertView;
        }

    }

}
