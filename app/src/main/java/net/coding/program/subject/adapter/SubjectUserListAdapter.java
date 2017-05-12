package net.coding.program.subject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.model.UserObject;

import java.util.List;

/**
 * Created by david on 15-7-20.
 */
public class SubjectUserListAdapter extends BaseAdapter {

    private List<UserObject> userItems;
    private Context mContext;

    private View.OnClickListener mFollowClickListener;

    public SubjectUserListAdapter(Context context, List<UserObject> items) {
        this.mContext = context;
        this.userItems = items;
    }

    @Override
    public int getCount() {
        if (userItems != null && userItems.size() > 0) {
            return userItems.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return userItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_users_list_item, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.mutual = (CheckBox) convertView.findViewById(R.id.followMutual);
            holder.divideTitle = (TextView) convertView.findViewById(R.id.divideTitle);
            holder.divideLine = convertView.findViewById(R.id.divide_line);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final UserObject data = userItems.get(position);

        holder.divideTitle.setVisibility(View.GONE);
        holder.divideLine.setVisibility(View.VISIBLE);

        holder.name.setText(data.name);
        new ImageLoadTool().loadImage(holder.icon, data.avatar);

        if (data.isMe()) {
            holder.mutual.setVisibility(View.INVISIBLE);
        } else {
            holder.mutual.setVisibility(View.VISIBLE);
            int drawableId = data.follow ? R.drawable.checkbox_fans : R.drawable.checkbox_follow;
            holder.mutual.setButtonDrawable(drawableId);
            holder.mutual.setTag(position);
            holder.mutual.setChecked(data.followed);
            if (mFollowClickListener != null) {
                holder.mutual.setOnClickListener(mFollowClickListener);
            }
        }

        return convertView;
    }

    public void setFollowClickListener(View.OnClickListener onClickListener) {
        this.mFollowClickListener = onClickListener;
    }

    public static class ViewHolder {
        ImageView icon;
        TextView name;
        CheckBox mutual;
        TextView divideTitle;
        View divideLine;
    }

}
