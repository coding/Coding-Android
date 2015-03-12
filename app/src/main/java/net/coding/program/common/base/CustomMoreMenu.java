package net.coding.program.common.base;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.DialogUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by chenchao on 15/3/9.
 */
public class CustomMoreMenu {

//    WeakReference<Context> mParent;
//    String[] mTitles;
//    int[] mIcons;
//    String[] mClicks;
//
//    private DialogUtil.RightTopPopupWindow mRightTopPopupWindow;
//
//    public CustomMoreMenu(WeakReference<Context> mParent, String[] mTitles, int[] mIcons, String[] mClicks) {
//        this.mParent = mParent;
//        this.mTitles = mTitles;
//        this.mIcons = mIcons;
//        this.mClicks = mClicks;
//    }
//
//    public CustomMoreMenu(WeakReference<Context> mParent, int[] mTitles, int[] mIcons, String[] mClicks) {
//        int count = 0;
//        for (int item : mTitles) {
//            if (item != 0) {
//                ++count;
//            }
//        }
//
//        for (int i = 0; i < count; ++i) {
//
//        this.mParent = mParent;
//        this.mTitles = mTitles;
//        this.mIcons = mIcons;
//        this.mClicks = mClicks;
//    }
//
//    public void show() {
//        if (mParent.get() == null) {
//            return;
//        }
//
//        if (mRightTopPopupWindow == null) {
//            ArrayList<DialogUtil.RightTopPopupItem> popupItemArrayList = new ArrayList();
//            DialogUtil.RightTopPopupItem downloadItem = new DialogUtil.RightTopPopupItem(getString(R.string.copy_link), R.drawable.ic_menu_link);
//            popupItemArrayList.add(downloadItem);
//            if (owerGlobar.equals(MyApp.sUserObject.global_key)) {
//                DialogUtil.RightTopPopupItem deleteItem = new DialogUtil.RightTopPopupItem();
//                popupItemArrayList.add(deleteItem);
//            }
//            mRightTopPopupWindow = DialogUtil.initRightTopPopupWindow(this, popupItemArrayList, onRightTopPopupItemClickListener);
//        }
//
//        mRightTopPopupWindow.adapter.notifyDataSetChanged();
//
//        Rect rectgle = new Rect();
//        Window window = getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
//        int StatusBarHeight = rectgle.top;
//        int contentViewTop =
//                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
//        //int TitleBarHeight= contentViewTop - StatusBarHeight;
//        mRightTopPopupWindow.adapter.notifyDataSetChanged();
//        mRightTopPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
//        mRightTopPopupWindow.showAtLocation(listView, Gravity.TOP | Gravity.RIGHT, 0, contentViewTop);
//
//    }
//
//    private AdapterView.OnItemClickListener onRightTopPopupItemClickListener = new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//            try {
//                Context parent = mParent.get();
//                if (parent != null) {
//                    String methodName = mClicks[position];
//                    Method method = parent.getClass().getDeclaredMethod(methodName);
//                    method.setAccessible(true);
//                    method.invoke(parent);
//                }
//            } catch (Exception e) {
//            } finally {
//                mRightTopPopupWindow.dismiss();
//            }
//        }
//    };
//
//    public static class Param {
//        public String title;
//        public int icon;
//        public String method;
//
//        public Param(String title, int icon, String method) {
//            this.title = title;
//            this.icon = icon;
//            this.method = method;
//        }
//    }
}
