package net.coding.program.project.maopao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.LoadMore;
import net.coding.program.common.MyImageGetter;
import net.coding.program.common.model.Maopao;
import net.coding.program.maopao.ContentArea;
import net.coding.program.pickphoto.ClickSmallImage;

import java.util.List;

/**
 * Created by chenchao on 16/7/21.
 * 项目公告的 adapter
 */
class EnterpriseProjectMaopaoAdapter extends BaseAdapter {

    List<Maopao.MaopaoObject> listData;
    LoadMore loadMore;
    View.OnClickListener clickDelete;
    View.OnClickListener clickListItem;

    ClickSmallImage onClickImage;
    MyImageGetter myImageGetter;

    ImageLoadTool imageLoadTool;
    int mPxImageWidth;

    public EnterpriseProjectMaopaoAdapter(List<Maopao.MaopaoObject> listData, ProjectMaopaoActivity activity,
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.enterprise_list_item_project_maopao, parent, false);
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


        holder.contentArea.setData(data);

        if (position == getCount() - 1) {
            loadMore.loadMore();
        }

        return convertView;
    }

    class ViewHolder {

        public ViewHolder(View v) {
            icon = (ImageView) v.findViewById(R.id.icon);
            name = (TextView) v.findViewById(R.id.name);
            time = (TextView) v.findViewById(R.id.time);
            content = (TextView) v.findViewById(R.id.content);
            contentArea = new ContentArea(v, clickListItem, onClickImage, myImageGetter, imageLoadTool, mPxImageWidth);
        }

        ImageView icon;
        TextView name;
        TextView time;
        TextView content;

        ContentArea contentArea;
    }

}
