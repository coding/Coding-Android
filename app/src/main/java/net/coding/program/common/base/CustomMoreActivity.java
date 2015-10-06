package net.coding.program.common.base;

import net.coding.program.common.ui.BaseActivity;
import net.coding.program.common.Global;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by chenchao on 15/3/9.
 */

@EActivity
public abstract class CustomMoreActivity extends BaseActivity {

    //    protected abstract View getAnchorView();
    protected abstract String getLink();

//    @OptionsItem
//    protected void action_more() {
//        showRightTopPop();
//    }
//
//    private DialogUtil.RightTopPopupWindow mRightTopPopupWindow = null;

    @OptionsItem
    protected final void action_copy() {
        String link = getLink();
        if (link.isEmpty()) {
            showButtomToast("复制链接失败");
        } else {
            Global.copy(CustomMoreActivity.this, link);
            showButtomToast("已复制链接 " + link);
        }
    }

//    private void showRightTopPop() {
//        if (mRightTopPopupWindow == null) {
//            ArrayList<DialogUtil.RightTopPopupItem> popupItemArrayList = new ArrayList<>();
//            DialogUtil.RightTopPopupItem downloadItem = new DialogUtil.RightTopPopupItem(getString(R.string.copy_link), R.drawable.ic_menu_link);
//            popupItemArrayList.add(downloadItem);
//            mRightTopPopupWindow = DialogUtil.initRightTopPopupWindow(this, popupItemArrayList, onRightTopPopupItemClickListener);
//        }
//
//        mRightTopPopupWindow.adapter.notifyDataSetChanged();
//
//        Rect rectgle = new Rect();
//        Window window = getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
//        int contentViewTop =
//                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
//        mRightTopPopupWindow.adapter.notifyDataSetChanged();
//        mRightTopPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
//        mRightTopPopupWindow.showAtLocation(getAnchorView(), Gravity.TOP | Gravity.RIGHT, 0, contentViewTop);
//    }
//
//    private AdapterView.OnItemClickListener onRightTopPopupItemClickListener = new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//            switch (position) {
//                case 0:
//                    break;
//            }
//            mRightTopPopupWindow.dismiss();
//        }
//    };
}
