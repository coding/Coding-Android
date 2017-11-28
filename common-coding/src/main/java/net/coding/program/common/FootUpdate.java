package net.coding.program.common;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.lang.reflect.Method;

/**
 * Created by chaochen on 14-10-22.
 * todo delete
 * 上拉加载更多
 */
public class FootUpdate {

    View mLayout;
    View mClick;
    View mLoading;

    public FootUpdate() {
    }

    public int getHigh() {
        if (mLayout == null) {
            return 0;
        }

        return mLayout.getHeight();
    }

    public void initToHead(Object listView, LayoutInflater inflater, final LoadMore loadMore) {
        init(listView, inflater, loadMore, "addHeaderView");
    }

    public void init(Object listView, LayoutInflater inflater, final LoadMore loadMore) {
        init(listView, inflater, loadMore, "addFooterView");
    }

    public void initToRecycler(Object listView, LayoutInflater inflater, final LoadMore loadMore) {
        init(listView, inflater, loadMore, null);
    }

    private void init(Object listView, LayoutInflater inflater, final LoadMore loadMore, String callMethod) {
        ListView parent = null;
        if (listView instanceof ListView) {
            parent = (ListView) listView;
        }
        View v = inflater.inflate(net.coding.program.R.layout.listview_foot, parent, false);

        // 为了防止触发listview的onListItemClick事件
        mLayout = v.findViewById(net.coding.program.R.id.layout);
        mLayout.setOnClickListener(v1 -> {
        });

        mClick = v.findViewById(net.coding.program.R.id.textView);
        mClick.setOnClickListener(v12 -> {
            loadMore.loadMore();
            showLoading();
        });

        mLoading = v.findViewById(net.coding.program.R.id.progressBar);

        if (!TextUtils.isEmpty(callMethod)) {
            try {
                Method method = listView.getClass().getMethod(callMethod, View.class);
                method.invoke(listView, v);
            } catch (Exception e) {
                Global.errorLog(e);
            }
        }

        mLayout.setVisibility(View.GONE);
    }

    public View getView() {
        return mLayout;
    }

    public void showLoading() {
        show(true, true);
    }

    public void showFail() {
        show(true, false);
    }

    public void dismiss() {
        show(false, true);
    }

    private void show(boolean show, boolean loading) {
        if (mLayout == null) {
            return;
        }

        if (show) {
            mLayout.setVisibility(View.VISIBLE);
            mLayout.setPadding(0, 0, 0, 0);
            if (loading) {
                mClick.setVisibility(View.INVISIBLE);
                mLoading.setVisibility(View.VISIBLE);
            } else {
                mClick.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.INVISIBLE);
            }
        } else {
            mLayout.setVisibility(View.INVISIBLE);
            mLayout.setPadding(0, -mLayout.getHeight(), 0, 0);
        }
    }

    public void updateState(int code, boolean isLastPage, int locatedSize) {
        if (code == 0) {
            if (isLastPage) {
                dismiss();
            } else {
                showLoading();
            }
        } else {
            if (locatedSize > 0) {
                showFail();
            } else {
                dismiss(); // 显示猴子照片
            }
        }
    }

}
