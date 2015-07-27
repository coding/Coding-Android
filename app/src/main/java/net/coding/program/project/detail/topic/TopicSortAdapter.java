package net.coding.program.project.detail.topic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.coding.program.R;

/**
 * Created by chenchao on 15/7/24.
 */
public class TopicSortAdapter extends BaseAdapter {

    String[] data;

    public TopicSortAdapter(Context context) {
        data = context.getResources().getStringArray(R.array.comment_sort);
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.topic_comment_spinner_text,
                    parent, false);
        }

        ((TextView) convertView).setText((String) getItem(position));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.topic_comment_spinner_item_text,
                    parent, false);
        }

        ((TextView) convertView).setText((String) getItem(position));
        return convertView;

    }
}
