package net.coding.program.user;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.Toast;

import net.coding.program.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;

/**
 * Created by yangzhen on 2014/10/15.
 */
public class UserTagAdapter extends BaseAdapter {
    public static final int MAX_TAG_COUNT = 10;

    HashSet mHashSet = new HashSet();
    JSONArray tagJSONArray;
    private Context context;
    private LayoutInflater mInflater;

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

    private boolean isSelectFill() {
        return mHashSet.size() >= MAX_TAG_COUNT;
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

        TagObject data = new TagObject((JSONObject) getItem(position));
        holder.tag.setText(data.getmTagName());
        if (mHashSet.contains(data.getmTagId())) {
            holder.tag.setChecked(true);
        } else {
            holder.tag.setChecked(false);
        }
        return convertView;
    }

    public String getSelected() {
        return TextUtils.join(",", mHashSet);
    }

    public void setSelected(String tags) {
        mHashSet.clear();
        String[] selectedTagIdsStr = tags.trim().split(",");
        for (String s : selectedTagIdsStr) {
            if (!s.trim().isEmpty())
                mHashSet.add(Integer.parseInt(s));
        }
        notifyDataSetChanged();
    }

    public void setSelectedTag(int position) {
        TagObject data = new TagObject(tagJSONArray.optJSONObject(position));
        int tagId = data.getmTagId();
        if (!mHashSet.contains(tagId)) {
            if (!isSelectFill()) {
                mHashSet.add(tagId);
            } else {
                Toast.makeText(context, String.format("最多只能选择%d个标签", UserTagAdapter.MAX_TAG_COUNT),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            mHashSet.remove(tagId);
        }

        notifyDataSetChanged();
    }

    static class ViewHolder {
        CheckedTextView tag;
    }

    private static class TagObject {
        private String mTagName;
        private int mTagId;

        public TagObject(JSONObject json) {
            mTagName = json.optString("name");
            mTagId = json.optInt("id");
        }

        public String getmTagName() {
            return mTagName;
        }

        public int getmTagId() {
            return mTagId;
        }
    }
}
