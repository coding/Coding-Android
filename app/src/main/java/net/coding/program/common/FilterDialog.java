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
import net.coding.program.model.FilterModel;
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
    private int font2;
    private int green;

    public FilterDialog() {

    }

    public static FilterDialog getInstance() {
        return new FilterDialog();
    }

    public void show(Context context, FilterModel filterModel, FilterListener filterListener) {
        this.mContext = context;
        this.mFilterModel = filterModel;
        font2 = mContext.getResources().getColor(R.color.font_2);
        green = mContext.getResources().getColor(R.color.green);

        mDialog = new Dialog(context, R.style.FilterDialog);
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mDialog.setContentView(R.layout.dialog_task_filter);
        mDialog.getWindow().getAttributes().width = ViewGroup.LayoutParams.MATCH_PARENT;
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(true);


        View reset = mDialog.findViewById(R.id.tv_reset);
        reset.setOnClickListener(v -> {
            if (filterListener != null) {
                filterListener.callback(new FilterModel());
            }
            dismiss();
        });

        EditText etSearch = initKeyword(filterListener);

        iniStatus(filterListener, etSearch);
        iniLabels(filterListener, etSearch);
    }

    @NonNull
    private EditText initKeyword(FilterListener filterListener) {
        EditText etSearch = (EditText) mDialog.findViewById(R.id.et_search);

        if (mFilterModel != null && !TextUtils.isEmpty(mFilterModel.keyword)) {
            etSearch.setText(mFilterModel.keyword);
        }
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (filterListener != null) {
                    String keyword = etSearch.getText().toString().trim();
                    filterListener.callback(mFilterModel.status != 0 ?
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

    private void iniLabels(FilterListener filterListener, EditText etSearch) {

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
            String str = String.format("%s (%d/%d)", item.name, item.processing, item.all);
            labelItem.setText(str);
            setLeftDrawable(labelItem, item.color, item.name.equals(mFilterModel.label));
            labelItem.setOnClickListener(v -> {
                String keyword = etSearch.getText().toString();
                if (filterListener != null) {
                    filterListener.callback(new FilterModel(item.name, keyword));
                }
                dismiss();
            });

            llLabels.addView(labelItem);

        }
    }

    private void iniStatus(FilterListener filterListener, EditText etSearch) {
        String[] taskStr = {"进行中", "已完成"};
        int[] taskViews = {R.id.tv_task_doing, R.id.tv_task_done};

        for (int i = 0; i < taskStr.length; i++) {

            TextView taskView = (TextView) mDialog.findViewById(taskViews[i]);
            String txt = taskStr[i];
            if (i == 0) {
                if (mFilterModel != null && mFilterModel.statusTaskDoing > 0) {
                    txt += String.format(" (%d)", mFilterModel.statusTaskDoing);
                }
            } else {
                if (mFilterModel != null && mFilterModel.statusTaskDone > 0) {
                    txt += String.format(" (%d)", mFilterModel.statusTaskDone);
                }
            }
            taskView.setText(txt);

            int status = i + 1;
            taskView.setOnClickListener(v -> {
                String keyword = etSearch.getText().toString();
                if (filterListener != null) {
                    filterListener.callback(new FilterModel(status, keyword));
                }
                dismiss();
            });

            //初始化状态
            boolean isCheck = mFilterModel != null && mFilterModel.status == status;
            taskView.setTextColor(!isCheck ? font2 : green);
            if (isCheck) {
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

    public void setLeftDrawable(TextView textView, String color, boolean isChecked) {

        if (mContext == null) {
            return;
        }

        final Drawable originalBitmapDrawable = mContext.getResources().getDrawable(R.drawable.ic_project_topic_label).mutate();
        Drawable right = isChecked ? mContext.getResources().getDrawable(R.drawable.ic_task_status_list_check) : null;

        ColorStateList colorStateList = ColorStateList.valueOf(Color.parseColor(color));
        textView.setCompoundDrawablesWithIntrinsicBounds(tintDrawable(originalBitmapDrawable, colorStateList), null, right, null);
        textView.setTextColor(!isChecked ? font2 : green);
    }

    public Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

}
