package net.coding.program.common;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by afs on 2016/12/12.
 */

public class FilterDialog {

    private Dialog mDialog;
    private Context mContext;

    public FilterDialog() {

    }

    public static FilterDialog getInstance() {
        return new FilterDialog();
    }

    public void show(Context context, FilterModel filterModel, SearchListener searchListener) {
        this.mContext = context;
        mDialog = new Dialog(context, R.style.notitleDialog);
        //adjustPan|stateAlwaysVisible|adjustUnspecified|stateHidden
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mDialog.setContentView(R.layout.dialog_task_filter);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        mDialog.show();

        View reset = mDialog.findViewById(R.id.tv_reset);

        EditText etSearch = (EditText) mDialog.findViewById(R.id.et_search);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mDialog.dismiss();
            }
            return false;
        });

        TextView taskDoing = (TextView) mDialog.findViewById(R.id.tv_task_doing);
        TextView taskDone = (TextView) mDialog.findViewById(R.id.tv_task_done);

        //setLeftDrawable(bug, false);

    }


    /**
     * 非左即右
     *
     * @param textView
     * @param resId
     */
    public void setRightDrawable(TextView textView, int resId) {
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, resId, 0);
    }

    public void setLeftDrawable(TextView textView, String color, boolean checked) {

        if (mContext == null) {
            return;
        }

        final Drawable originalBitmapDrawable = mContext.getResources().getDrawable(R.drawable.ic_project_topic_label).mutate();
        Drawable right = checked ? mContext.getResources().getDrawable(R.drawable.ic_task_status_list_check) : null;

        ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(color));
        textView.setCompoundDrawablesWithIntrinsicBounds(tintDrawable(originalBitmapDrawable, colorStateList), null, right, null);
    }

    public Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    public interface SearchListener {
        void callback(FilterModel filterModel);
    }

    public static class FilterModel {
        public int status;//任务状态，进行中的为1，已完成的为2
        public String label;//任务标签
        public String keyword;//根据关键字筛选任务

        public int statusTaskDoing;
        public int statusTaskDone;
    }
}
