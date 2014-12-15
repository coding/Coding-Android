package net.coding.program.user;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import net.coding.program.R;

import org.json.JSONArray;

import java.util.HashSet;

/**
 * Created by yangzhen on 2014/10/15.
 */
public class UserTagAdapter extends BaseAdapter {
    HashSet hashSet = new HashSet();
    private Context context;
    private LayoutInflater mInflater;
    JSONArray tagJSONArray;

    static class ViewHolder {
        CheckedTextView tag;
    }

    public UserTagAdapter(Context context, String tags, JSONArray tagJSONArray) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.tagJSONArray = tagJSONArray;
        setSelected(tags);
    }

    public void setTagJSONArray(JSONArray tagJSONArray) {
        this.tagJSONArray = tagJSONArray;
    }

    @Override
    public int getCount() {
        return tagJSONArray.length();
    }

    @Override
    public Object getItem(int position) {
        return tagJSONArray.optJSONObject(position);
    }

    public String getName(int position) {
        return tagJSONArray.optJSONObject(position).optString("name");
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_user_tags_list_item, parent, false);
            holder = new ViewHolder();
            holder.tag = (CheckedTextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tag.setText(tagJSONArray.optJSONObject(position).optString("name"));
        if (hashSet.contains(Integer.valueOf(position + 1))) {
            holder.tag.setChecked(true);
        } else {
            holder.tag.setChecked(false);
        }
        return convertView;
    }

    public void setSelected(String tags) {
        hashSet.clear();
        String[] selectedTagIdsStr = tags.trim().split(",");
        for (String s : selectedTagIdsStr) {
            if (!s.trim().isEmpty())
                hashSet.add(Integer.parseInt(s));
        }
        notifyDataSetChanged();
    }

    public String getSelected() {
        return TextUtils.join(",", hashSet);
    }

    /*public String getSelectedStr(){
        ArrayList<String> tagsStr = new ArrayList<String>();
        Iterator it = hashSet.iterator();
        while (it.hasNext()) {
            Integer key = (Integer) it.next();
            tagsStr.add(getName(key));
        }
        return TextUtils.join(",", tagsStr);
    }*/

    public void setSelectedTag(int position) {

        if (!hashSet.contains(Integer.valueOf(position + 1))) {
            hashSet.add(Integer.valueOf(position + 1));
        } else {
            hashSet.remove(Integer.valueOf(position + 1));
        }
        notifyDataSetChanged();
    }
}
