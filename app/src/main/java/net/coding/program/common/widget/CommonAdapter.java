package net.coding.program.common.widget;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.quickAdapter.easyRegularAdapter;

import java.util.List;

/**
 * Created by chenchao on 2017/5/19.
 * 公共 adapter
 */

public abstract class CommonAdapter<T, BINDHOLDER extends UltimateRecyclerviewViewHolder> extends easyRegularAdapter<T, BINDHOLDER> {

    public CommonAdapter(List<T> list) {
        super(list);
    }

    public CommonAdapter(T... objects) {
        super(objects);
    }
}
