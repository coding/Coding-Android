package net.coding.program.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.RequestData;
import net.coding.program.common.model.TopicObject;
import net.coding.program.project.detail.merge.CommentActivity;
import net.coding.program.search.HoloUtils;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Vernon on 15/11/27.
 */
public class SearchTopicAdapter extends BaseAdapter {
    private List<TopicObject> mData;
    private Context context;
    private String key;


    public SearchTopicAdapter(List<TopicObject> mData, Context context, String key) {
        this.mData = mData;
        this.context = context;
        this.key = key;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.search_task_list, null);
            holder = new ViewHolder();
            holder.nameTask = (TextView) convertView.findViewById(R.id.nameTask);
            holder.iconTask = (ImageView) convertView.findViewById(R.id.iconTask);
//            holder.descTask = (TextView) convertView.findViewById(R.id.descTask);
            holder.bottomName = (TextView) convertView.findViewById(R.id.bottomName);
            holder.bottomTime = (TextView) convertView.findViewById(R.id.bottomTime);
            holder.bottomHeartCount = (TextView) convertView.findViewById(R.id.bottomHeartCount);
            holder.bottomCommentCount = (TextView) convertView.findViewById(R.id.bottomCommentCount);
            holder.bottomHeartImg = (ImageView) convertView.findViewById(R.id.bottomHeartImg);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        TopicObject bean = mData.get(position);
        HoloUtils.setHoloText(holder.nameTask, bean.title);
//        HoloUtils.setHoloText(holder.descTask, key, Html.fromHtml(bean.content).toString());
        holder.bottomName.setText(bean.owner.name);
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        holder.bottomTime.setText(format.format(bean.created_at));
        holder.bottomCommentCount.setText(bean.comment_count + "");
        holder.bottomHeartCount.setVisibility(View.GONE);
        holder.bottomHeartImg.setVisibility(View.GONE);
        ImageLoader.getInstance().displayImage(bean.owner.avatar, holder.iconTask, ImageLoadTool.optionsImage);
        return convertView;
    }

    private void setClickEvent(View view, TopicObject bean) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//               CommentActivity_.intent(context).mParam(param);

            }
        });

    }

    private CommentActivity.CommentParam createCommentParam(final TopicObject bean) {
        CommentActivity.CommentParam param = new CommentActivity.CommentParam() {
            @Override
            public RequestData getSendCommentParam(String input) {

                return null;
            }

            @Override
            public String getAtSome() {
                return null;
            }

            @Override
            public String getAtSomeUrl() {
                return null;
            }

            @Override
            public String getProjectPath() {
                return null;
            }

            @Override
            public boolean isPublicProject() {
                return false;
            }
        };
        return param;
    }

    static class ViewHolder {
        TextView nameTask;
        //        TextView descTask;
        TextView bottomName;
        TextView bottomTime;
        TextView bottomCommentCount;
        TextView bottomHeartCount;
        ImageView iconTask;
        ImageView bottomHeartImg;
    }

}

