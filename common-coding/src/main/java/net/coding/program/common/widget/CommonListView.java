package net.coding.program.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import net.coding.program.R;
import net.coding.program.common.util.BlankViewHelp;

/**
 * Created by chenchao on 2017/5/15.
 * 包装了一下，免得要改全局样式
 */

public class CommonListView extends UltimateRecyclerView {

    public enum Style {
        success,
        loading,
        fail
    }

    public CommonListView(Context context) {
        super(context);
        init();
    }

    public CommonListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CommonListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setEmptyView(R.layout.view_exception, R.layout.view_exception);
        BlankViewHelp.setBlankLoading(getEmptyView(), true);
        mRecyclerView.setBackgroundResource(R.color.divide);
    }

    public void update(Context context, Style style) {
        int count = 0;
        if (getAdapter() != null) {
            count = ((UltimateViewAdapter) getAdapter()).getAdapterItemCount();
        }
        update(context, style, count);
    }

    public void update(Context context, Style style, int count) {
        View v = getEmptyView();
        if (style == Style.loading) {
            BlankViewHelp.setBlankLoading(v, count <= 0);
        } else if (style == Style.fail) {
            BlankViewHelp.setBlank(count, context, false, v, null); // network 应该是 listview 的一部分？
        } else {
            BlankViewHelp.setBlank(count, context, true, v, null);
        }
    }
}
