package net.coding.program.project.maopao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.model.Maopao;

import java.util.List;

/**
 * Created by chenchao on 16/7/21.
 */
public class ProjectMaopaoAdapter extends BaseAdapter {

    List<Maopao.MaopaoObject> listData;

    public ProjectMaopaoAdapter(List<Maopao.MaopaoObject> listData) {
        this.listData = listData;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_project_maopao, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Maopao.MaopaoObject data = listData.get(position);
        ImageLoadTool.loadUserImage(holder.icon, data.owner.avatar);
        holder.name.setText(data.owner.name);
        holder.time.setText(Global.dayFromTime(data.created_at));
        holder.content.setText(Global.changeHyperlinkColor(data.content.replace("<p>", "").replace("</p>", "").replace("</blockquote>", "").replace("<blockquote>", "")));
        holder.comment.setText(String.format("%s条评论", data.comments));

        return convertView;
    }

    class ViewHolder {

        public ViewHolder(View v) {
            icon = (ImageView) v.findViewById(R.id.icon);
            name = (TextView) v.findViewById(R.id.name);
            time = (TextView) v.findViewById(R.id.time);
            content = (TextView) v.findViewById(R.id.content);
            comment = (TextView) v.findViewById(R.id.comment);
        }

        ImageView icon;
        TextView name;
        TextView time;
        TextView content;
        TextView comment;
    }
}
