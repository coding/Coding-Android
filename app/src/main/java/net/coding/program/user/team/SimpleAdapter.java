package net.coding.program.user.team;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by chenchao on 16/9/13.
 */
public abstract class SimpleAdapter<T, H> extends BaseAdapter {

    public ArrayList<T> listData;

    public SimpleAdapter() {
    }

    public SimpleAdapter(ArrayList<T> data) {
        listData = data;
    }

    public abstract void bindData(H h, T t, int position);

    public abstract int getItemlayoutId();

    public abstract H createViewHolder(View v);

    public void init(ArrayList<T> data) {
        listData = data;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public T getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        H holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(getItemlayoutId(), parent, false);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (H) convertView.getTag();
        }

        bindData(holder, getItem(position), position);
        return convertView;
    }
}
