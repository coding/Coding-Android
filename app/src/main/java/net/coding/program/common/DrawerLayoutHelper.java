package net.coding.program.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.model.FilterModel;
import net.coding.program.common.model.TaskLabelModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by afs on 2016/12/12.
 */

public class DrawerLayoutHelper {

    private DrawerLayout drawerLayout;
    private Context mContext;
    private FilterModel mFilterModel;
    private int font2;
    private int green;
    private boolean showLabelCount;

    public DrawerLayoutHelper() {

    }

    public static DrawerLayoutHelper getInstance() {
        return new DrawerLayoutHelper();
    }

    /**
     * @param context
     * @param drawerLayout
     * @param filterModel
     * @param filterListener
     */
    public void initData(Context context, DrawerLayout drawerLayout, FilterModel filterModel, FilterListener filterListener) {
        this.mContext = context;
        this.mFilterModel = filterModel;
        this.showLabelCount = false;
        font2 = mContext.getResources().getColor(R.color.font_1);
        green = mContext.getResources().getColor(R.color.font_green);

        this.drawerLayout = drawerLayout;
        this.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Global.hideSoftKeyboard((Activity) mContext);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        View reset = drawerLayout.findViewById(R.id.tv_reset);


        reset.setOnClickListener(v -> {
            //假如有搜索条件才重置
            if (filterListener != null && (!TextUtils.isEmpty(filterModel.keyword) || !TextUtils.isEmpty(filterModel.label) || filterModel.status != 0)) {
                filterListener.callback(new FilterModel());
            }
            dismiss();
        });

        EditText etSearch = initKeyword(filterListener);

        iniStatus(filterListener, etSearch);
        iniLabels(filterListener, etSearch);
    }

    private void setActionDown(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Global.hideSoftKeyboard(((Activity) mContext));
                }
                return false;
            }
        });
    }

    @NonNull
    private EditText initKeyword(FilterListener filterListener) {
        EditText etSearch = (EditText) drawerLayout.findViewById(R.id.et_search);

//        if (mFilterModel != null && !TextUtils.isEmpty(mFilterModel.keyword)) {
//            mFilterModel.keyword = "";
//        }
        etSearch.setText("");
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
        etSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Global.hideSoftKeyboard(((Activity) mContext));
                }
            }
        });
        return etSearch;
    }

    private void dismiss() {
        if (drawerLayout == null) return;

        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }

        Global.hideSoftKeyboard(((Activity) mContext));
    }

    private void iniLabels(FilterListener filterListener, EditText etSearch) {
        if (mFilterModel == null || mFilterModel.labelModels == null) {
            return;
        }

        LinearLayout llLabels = (LinearLayout) drawerLayout.findViewById(R.id.ll_labels);
        llLabels.removeAllViews();
        List<String> labelModels = new ArrayList<>();

        int len = mFilterModel.labelModels.size();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        for (int i = 0; i < len; i++) {
            final TaskLabelModel item = mFilterModel.labelModels.get(i);
            if (labelModels.contains(item.name)) {
                continue;
            }
            labelModels.add(item.name);
            //兼容新接口
            if (item.all == 0) {
                item.all = item.count;
            }

            TextView labelItem = (TextView) layoutInflater.inflate(R.layout.dialog_task_filter_label_item, null);
            String str = showLabelCount ? String.format("%s (%s/%s)", item.name, item.processing, item.all) : item.name;
            labelItem.setText(str);
            setLeftDrawable(labelItem, item.color, item.name.equals(mFilterModel.label));
            labelItem.setOnClickListener(v -> {
                String keyword = etSearch.getText().toString();
                if (filterListener != null) {
                    filterListener.callback(new FilterModel(item.name, keyword));
                }
                dismiss();
            });
            setActionDown(labelItem);
            llLabels.addView(labelItem);

        }
    }

    private void iniStatus(FilterListener filterListener, EditText etSearch) {
        String[] taskStr = {"进行中", "已完成"};
        int[] taskViews = {R.id.tv_task_doing, R.id.tv_task_done};

        for (int i = 0; i < taskStr.length; i++) {

            TextView taskView = (TextView) drawerLayout.findViewById(taskViews[i]);
            String txt = taskStr[i];
            if (i == 0) {

                if (mFilterModel != null && mFilterModel.statusTaskDoing > 0) {
                    txt += String.format(" (%s)", mFilterModel.statusTaskDoing);
                }
            } else {
                if (mFilterModel != null && mFilterModel.statusTaskDone > 0) {
                    txt += String.format(" (%s)", mFilterModel.statusTaskDone);
                }
            }
            taskView.setText(txt);
            setActionDown(taskView);
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

        final Drawable originalBitmapDrawable = mContext.getResources().getDrawable(R.drawable.ic_project_topic_label_light).mutate();
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
