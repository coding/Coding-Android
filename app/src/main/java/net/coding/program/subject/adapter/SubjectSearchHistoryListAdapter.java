package net.coding.program.subject.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.SearchCache;

import java.util.List;

/**
 * Created by david on 15-7-20.
 */
public class SubjectSearchHistoryListAdapter extends BaseAdapter {

    private List<String> historyItems;
    private Context mContext;

    public SubjectSearchHistoryListAdapter(Context context, List<String> items) {
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
        ViewHolder viewHolder = null;
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.subject_search_history_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.desc = (TextView) convertView.findViewById(R.id.subject_search_list_item_name);
            viewHolder.delImage = (ImageView) convertView.findViewById(R.id.subject_search_list_item_del);
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

    private View.OnClickListener mDelHistoryItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = Integer.valueOf(v.getTag().toString());
            if (historyItems != null && position >= 0 && position < historyItems.size()) {
                String history = historyItems.get(position);
                historyItems.remove(history);
                SearchCache.getInstance(mContext).remove(history);
                notifyDataSetChanged();
            }
        }
    };


}
