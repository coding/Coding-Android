package net.coding.program.common;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.model.TaskLabelModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by afs on 2016/12/12.
 */

public class FilterDialog {

    private Dialog mDialog;
    private Context mContext;
    private FilterModel mFilterModel;

    public FilterDialog() {

    }

    public static FilterDialog getInstance() {
        return new FilterDialog();
    }

    public void show(Context context, FilterModel filterModel, SearchListener searchListener) {
        this.mContext = context;
        this.mFilterModel = filterModel;
        mDialog = new Dialog(context, R.style.notitleDialog);
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mDialog.setContentView(R.layout.dialog_task_filter);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        mDialog.show();

        View reset = mDialog.findViewById(R.id.tv_reset);
        reset.setOnClickListener(v -> {
            if (searchListener != null) {
                searchListener.callback(new FilterModel());
            }
            dismiss();
        });

        EditText etSearch = initKeyword(searchListener);

        iniStatus(searchListener, etSearch);
        iniLabels(searchListener, etSearch);
    }

    @NonNull
    private EditText initKeyword(SearchListener searchListener) {
        EditText etSearch = (EditText) mDialog.findViewById(R.id.et_search);

        if (mFilterModel != null && !TextUtils.isEmpty(mFilterModel.keyword)) {
            etSearch.setText(mFilterModel.keyword);
        }
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (searchListener != null) {
                    String keyword = etSearch.getText().toString().trim();
                    searchListener.callback(mFilterModel.status != 0 ?
                            new FilterModel(mFilterModel.status, keyword) :
                            new FilterModel(mFilterModel.label, keyword));
                }
                dismiss();
            }
            return false;
        });
        return etSearch;
    }

    private void dismiss() {
        if (mDialog == null) return;

        mDialog.dismiss();
    }

    private void iniLabels(SearchListener searchListener, EditText etSearch) {

        if (mFilterModel == null || mFilterModel.labelModels == null || mFilterModel.labelModels.size() == 0) {
            return;
        }

        LinearLayout llLabels = (LinearLayout) mDialog.findViewById(R.id.ll_labels);

        List<String> labelModels = new ArrayList<>();

        int len = mFilterModel.labelModels.size();
        for (int i = 0; i < len; i++) {
            final TaskLabelModel item = mFilterModel.labelModels.get(i);
            if (labelModels.contains(item.name)) {
                continue;
            }
            labelModels.add(item.name);

            TextView labelItem = (TextView) mDialog.getLayoutInflater().inflate(R.layout.dialog_task_filter_label_item, null);
            labelItem.setText(item.name);
            setLeftDrawable(labelItem, item.color, item.name.equals(mFilterModel.label));
            labelItem.setOnClickListener(v -> {
                String keyword = etSearch.getText().toString();
                if (searchListener != null) {
                    searchListener.callback(new FilterModel(item.name, keyword));
                }
                dismiss();
            });

            llLabels.addView(labelItem);

        }
    }

    private void iniStatus(SearchListener searchListener, EditText etSearch) {
        String[] taskStr = {"进行中", "已完成"};
        int[] taskViews = {R.id.tv_task_doing, R.id.tv_task_done};

        for (int i = 0; i < taskStr.length; i++) {

            TextView taskView = (TextView) mDialog.findViewById(taskViews[i]);
            taskView.setText(taskStr[i]);
            int status = i + 1;
            taskView.setOnClickListener(v -> {
                String keyword = etSearch.getText().toString();
                if (searchListener != null) {
                    searchListener.callback(new FilterModel(status, keyword));
                }
                dismiss();
            });

            //初始化状态
            if (mFilterModel != null && mFilterModel.status == status) {
                setRightDrawable(taskView, R.drawable.ic_task_status_list_check);
            } else {
                setRightDrawable(taskView, 0);
            }
        }
    }


    /**
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
        public FilterModel() {
            status = 0;
            label = null;
            keyword = null;
        }

        public FilterModel(int status, String keyword) {
            this.status = status;
            this.label = null;
            this.keyword = keyword;
        }

        public FilterModel(String label, String keyword) {
            this.label = label;
            this.status = 0;
            this.keyword = keyword;
        }

        public int status;//任务状态，进行中的为1，已完成的为2
        public String label;//任务标签
        public String keyword;//根据关键字筛选任务

        public FilterModel(List<TaskLabelModel> labelModels) {
            this.labelModels = labelModels;
        }

        public boolean isFilter() {
            return status != 0 || label != null && keyword != null;
        }

        public int statusTaskDoing;
        public int statusTaskDone;
        public List<TaskLabelModel> labelModels;
    }
}
