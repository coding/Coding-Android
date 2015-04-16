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

    protected abstract String getLink();

    @OptionsItem
    protected void action_copy() {
        String link = getLink();
        Global.copy(getActivity(), link);
        showButtomToast("已复制链接 " + link);
    }
}
