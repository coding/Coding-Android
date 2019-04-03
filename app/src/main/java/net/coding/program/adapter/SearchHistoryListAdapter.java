package net.coding.program.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.model.AccountInfo;

import java.util.ArrayList;

/**
 * Created by Vernon on 15/12/1.
 */
public class SearchHistoryListAdapter extends BaseAdapter {
    private ArrayList<String> historyItems;
    private Context mContext;
    private View.OnClickListener mDelHistoryItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = Integer.valueOf(v.getTag().toString());
            if (historyItems != null && position >= 0 && position < historyItems.size()) {
                String history = historyItems.get(position);
                historyItems.remove(history);
                AccountInfo.saveSearchProjectHistory(v.getContext(), historyItems);
                notifyDataSetChanged();
            }
        }
    };


    public SearchHistoryListAdapter(Context context, ArrayList<String> items) {
        this.mContext = context;
        this.historyItems = items;
    }

    @Override
    public int getCount() {
        if (historyItems != null && historyItems.size() > 0) {
            return historyItems.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return historyItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.search_history_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.desc = convertView.findViewById(R.id.subject_search_list_item_name);
            viewHolder.delImage = convertView.findViewById(R.id.subject_search_list_item_del);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.delImage.setTag(position);
        viewHolder.delImage.setOnClickListener(mDelHistoryItemClickListener);
        if (historyItems != null && position >= 0 && position < historyItems.size()) {
            String history = historyItems.get(position);
            if (history != null) {
                viewHolder.desc.setText(history);
            }
        }
        return convertView;
    }

    public static class ViewHolder {
        public TextView desc;
        public ImageView delImage;
    }


}