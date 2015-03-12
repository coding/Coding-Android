package net.coding.program.common.base;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;

import net.coding.program.R;
import net.coding.program.common.DialogUtil;
import net.coding.program.common.Global;
import net.coding.program.common.network.RefreshBaseFragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/3/9.
 */
@EFragment
public abstract class CustomMoreFragment extends RefreshBaseFragment {

    protected abstract View getAnchorView();
    protected abstract String getLink();

    @OptionsItem
    protected void action_more() {
        showRightTopPop();
    }


    private DialogUtil.RightTopPopupWindow mRightTopPopupWindow = null;


    private void showRightTopPop() {
        if (mRightTopPopupWindow == null) {
            ArrayList<DialogUtil.RightTopPopupItem> popupItemArrayList = new ArrayList();
            DialogUtil.RightTopPopupItem downloadItem = new DialogUtil.RightTopPopupItem(getString(R.string.copy_link), R.drawable.ic_menu_link);
            popupItemArrayList.add(downloadItem);
            mRightTopPopupWindow = DialogUtil.initRightTopPopupWindow(getActivity(), popupItemArrayList, onRightTopPopupItemClickListener);
        }

        mRightTopPopupWindow.adapter.notifyDataSetChanged();

        Rect rectgle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        mRightTopPopupWindow.adapter.notifyDataSetChanged();
        mRightTopPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mRightTopPopupWindow.showAtLocation(getAnchorView(), Gravity.TOP | Gravity.RIGHT, 0, contentViewTop);
    }

    private AdapterView.OnItemClickListener onRightTopPopupItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    String link = getLink();
                    Global.copy(getActivity(), link);
                    showButtomToast("已复制链接 " + link);
                    break;
            }
            mRightTopPopupWindow.dismiss();
        }
    };
}
