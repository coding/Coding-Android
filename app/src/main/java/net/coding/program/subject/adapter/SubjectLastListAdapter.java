package net.coding.program.subject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.SearchCache;
import net.coding.program.model.Subject;
import net.coding.program.subject.service.ISubjectRecommendObject;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by david on 15-7-20.
 */
public class SubjectLastListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private List<ISubjectRecommendObject> items;
    private Context mContext;

    public SubjectLastListAdapter(Context context, List<ISubjectRecommendObject> items) {
        this.mContext = context;
        this.items = items;
    }


    @Override
    public int getCount() {
        if (items != null && items.size() > 0) {
            return items.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.subject_recommend_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.subject_recommend_list_item_name);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (items != null && position >= 0 && position < items.size()) {
            ISubjectRecommendObject recommendObject = items.get(position);
            if (recommendObject != null) {
                viewHolder.name.setText("#" + recommendObject.getName() + "#");
            }
        }
        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.subject_recommend_list_item_header, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.subject_recommend_list_item_header_name);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (items != null && position >= 0 && position < items.size()) {
            ISubjectRecommendObject recommendObject = items.get(position);
            if (recommendObject != null) {
                if (recommendObject.getType() == 1)
                    viewHolder.name.setText("热门推荐");
                else
                    viewHolder.name.setText("最近使用");
            }
        }
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            ISubjectRecommendObject recommendObject = items.get(position);
            return recommendObject.getType();
        }
        return 0;
    }

    public static class ViewHolder {
        public TextView name;
    }


}
