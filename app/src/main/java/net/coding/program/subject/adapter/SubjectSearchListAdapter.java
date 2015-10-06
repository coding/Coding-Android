package net.coding.program.subject.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.model.Maopao;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by david on 15-7-20.
 */
public class SubjectSearchListAdapter extends BaseAdapter {

    private List<Maopao.MaopaoObject> maopaoObjectItems;
    private Context mContext;

    private ImageLoadTool mImageLoadTool = new ImageLoadTool();

    private Html.ImageGetter mImageGetter;

    public SubjectSearchListAdapter(Context context, List<Maopao.MaopaoObject> items, Html.ImageGetter imageGetter) {
        this.mContext = context;
        this.maopaoObjectItems = items;
        this.mImageGetter = imageGetter;
    }


    @Override
    public int getCount() {
        if (maopaoObjectItems != null && maopaoObjectItems.size() > 0) {
            return maopaoObjectItems.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return maopaoObjectItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.subject_search_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.content = (TextView) convertView.findViewById(R.id.maopao_content);
            viewHolder.likeCountView = (TextView) convertView.findViewById(R.id.maopao_item_like_count);
            viewHolder.timeView = (TextView) convertView.findViewById(R.id.maopao_item_time);
            viewHolder.commentView = (TextView) convertView.findViewById(R.id.maopao_item_comment);
            viewHolder.userNameView = (TextView) convertView.findViewById(R.id.maopao_item_user_name);
            viewHolder.userIconView = (CircleImageView) convertView.findViewById(R.id.maopao_user_icon);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (maopaoObjectItems != null && position >= 0 && position < maopaoObjectItems.size()) {
            Maopao.MaopaoObject maopaoObject = maopaoObjectItems.get(position);
            if (maopaoObject != null) {
                Global.MessageParse parse = HtmlContent.parseMessage(maopaoObject.content);
                viewHolder.content.setText(Global.changeHyperlinkColor(HtmlContent.parseReplacePhotoMonkey(parse.text), mImageGetter, Global.tagHandler));
                viewHolder.likeCountView.setText(String.valueOf(maopaoObject.likes));
                if (maopaoObject.owner != null && !TextUtils.isEmpty(maopaoObject.owner.avatar))
                    mImageLoadTool.loadImage(viewHolder.userIconView, maopaoObject.owner.avatar);
                viewHolder.timeView.setText(Global.dayToNow(maopaoObject.created_at));
                if (maopaoObject.owner != null && !TextUtils.isEmpty(maopaoObject.owner.avatar))
                    viewHolder.userNameView.setText(String.valueOf(maopaoObject.owner.name));
                viewHolder.commentView.setText(String.valueOf(maopaoObject.comments));

            }
        }
        return convertView;
    }

    public static class ViewHolder {
        public TextView content;
        public CircleImageView userIconView;
        private TextView timeView;
        private TextView userNameView;
        private TextView likeCountView;
        private TextView commentView;
    }


}
