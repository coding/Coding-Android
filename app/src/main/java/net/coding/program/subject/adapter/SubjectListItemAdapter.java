package net.coding.program.subject.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.model.Subject;

import java.util.List;

/**
 * Created by david on 15-7-20.
 */
public class SubjectListItemAdapter extends BaseAdapter {

    private List<Subject.SubjectDescObject> subjectItems;
    private Context mContext;

    public SubjectListItemAdapter(Context context, List<Subject.SubjectDescObject> items) {
        this.mContext = context;
        this.subjectItems = items;
    }


    @Override
    public int getCount() {
        if (subjectItems != null && subjectItems.size() > 0) {
            return subjectItems.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return subjectItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_subject_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.desc = (TextView) convertView.findViewById(R.id.subject_list_item_content);
            viewHolder.title = (TextView) convertView.findViewById(R.id.subject_list_item_name);
            viewHolder.peopleCount = (TextView) convertView.findViewById(R.id.subject_list_item_pepole_count);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (subjectItems != null && position >= 0 && position < subjectItems.size()) {
            Subject.SubjectDescObject subjectDescObject = subjectItems.get(position);
            if (subjectDescObject != null) {
                viewHolder.title.setText(String.valueOf(subjectDescObject.name));
                viewHolder.desc.setText(Html.fromHtml(subjectDescObject.hot_tweet.content));
                viewHolder.peopleCount.setText(subjectDescObject.speackers + "人参与");
            }
        }
        return convertView;
    }

    public static class ViewHolder {
        public TextView title;
        public TextView desc;
        public TextView peopleCount;
    }


}
