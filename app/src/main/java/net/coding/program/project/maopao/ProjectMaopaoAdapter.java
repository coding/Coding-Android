package net.coding.program.project.maopao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.GlobalData;
import net.coding.program.R;
import net.coding.program.common.ClickSmallImage;
import net.coding.program.common.Global;
import net.coding.program.route.GlobalCommon;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.LoadMore;
import net.coding.program.common.MyImageGetter;
import net.coding.program.maopao.ContentArea;
import net.coding.program.model.Maopao;

import java.util.List;

/**
 * Created by chenchao on 16/7/21.
 * 项目内冒泡的 adapter
 */
class ProjectMaopaoAdapter extends BaseAdapter {

    List<Maopao.MaopaoObject> listData;
    LoadMore loadMore;
    View.OnClickListener clickDelete;
    View.OnClickListener clickListItem;

    ClickSmallImage onClickImage;
    MyImageGetter myImageGetter;
    ImageLoadTool imageLoadTool;

    int mPxImageWidth;

    public ProjectMaopaoAdapter(List<Maopao.MaopaoObject> listData, ProjectMaopaoActivity activity,
                                View.OnClickListener clickDelete, View.OnClickListener clickListItem) {
        this.listData = listData;
        this.loadMore = activity;
        this.clickDelete = clickDelete;
        this.clickListItem = clickListItem;

        onClickImage = new ClickSmallImage(activity);
        myImageGetter = new MyImageGetter(activity);
        imageLoadTool = activity.getImageLoad();
        mPxImageWidth = GlobalCommon.dpToPx(GlobalData.sWidthDp - 12 - 40 - 10 - 10 - 3 * 2) / 3;
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
        holder.time.setText(Global.getTimeDetail(data.created_at));
        holder.content.setText(GlobalCommon.changeHyperlinkColor(data.content.replace("<p>", "").replace("</p>", "").replace("</blockquote>", "").replace("<blockquote>", "")));
        holder.comment.setText(String.format("%s条评论", data.comments));
        holder.delete.setTag(data);
        if (data.owner_id == GlobalData.sUserObject.id) {
            holder.delete.setVisibility(View.VISIBLE);
        } else {
            holder.delete.setVisibility(View.INVISIBLE);
        }

        holder.contentArea.setData(data);

        if (position == getCount() - 1) {
            loadMore.loadMore();
        }

        return convertView;
    }

    class ViewHolder {

        ImageView icon;
        TextView name;
        TextView time;
        TextView content;
        TextView comment;
        View delete;
        ContentArea contentArea;

        public ViewHolder(View v) {
            icon = (ImageView) v.findViewById(R.id.icon);
            name = (TextView) v.findViewById(R.id.name);
            time = (TextView) v.findViewById(R.id.time);
            content = (TextView) v.findViewById(R.id.content);
            comment = (TextView) v.findViewById(R.id.comment);
            delete = v.findViewById(R.id.delete);
            delete.setOnClickListener(clickDelete);
            contentArea = new ContentArea(v, clickListItem, onClickImage, myImageGetter, imageLoadTool, mPxImageWidth);
        }
    }

}
