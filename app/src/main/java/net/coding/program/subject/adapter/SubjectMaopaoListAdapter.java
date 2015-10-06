package net.coding.program.subject.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.common.ui.BaseActivity;
import net.coding.program.maopao.ContentArea;
import net.coding.program.maopao.LikeUsersArea;
import net.coding.program.maopao.item.CommentArea;
import net.coding.program.model.Maopao;

import java.util.List;

/**
 * Created by david on 15-7-28.
 */
public class SubjectMaopaoListAdapter extends BaseAdapter {

    private BaseActivity mContext;
    private List<Maopao.MaopaoObject> mData;


    public SubjectMaopaoListAdapter(BaseActivity baseActivity, List<Maopao.MaopaoObject> list) {
        this.mData = list;
        this.mContext = baseActivity;
    }

    @Override
    public int getCount() {
        if (mData != null)
            return mData.size();
        return 0;
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

//        if (convertView == null) {
//            holder = new ViewHolder();
//            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_maopao_list_item, parent, false);
//
//            holder.maopaoItem = convertView.findViewById(R.id.MaopaoItem);
//            holder.maopaoItem.setOnClickListener(mOnClickMaopaoItem);
//
//            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
//            holder.icon.setOnClickListener(mOnClickUser);
//
//            holder.name = (TextView) convertView.findViewById(R.id.name);
//            holder.name.setOnClickListener(mOnClickUser);
//            holder.time = (TextView) convertView.findViewById(R.id.time);
//
//            holder.contentArea = new ContentArea(convertView, mOnClickMaopaoItem, onClickImage, myImageGetter, getImageLoad(), mPxImageWidth);
//
//            holder.commentLikeArea = convertView.findViewById(R.id.commentLikeArea);
//            holder.likeUsersArea = new LikeUsersArea(convertView, mContext, getImageLoad(), mOnClickUser);
//
//            holder.location = (TextView) convertView.findViewById(R.id.location);
//            holder.photoType = (TextView) convertView.findViewById(R.id.photoType);
//            holder.likeBtn = (CheckBox) convertView.findViewById(R.id.likeBtn);
//            holder.commentBtn = (CheckBox) convertView.findViewById(R.id.commentBtn);
//            holder.likeBtn.setTag(R.id.likeBtn, holder);
//            holder.likeAreaDivide = convertView.findViewById(R.id.likeAreaDivide);
//            holder.commentBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    popComment(v);
//                }
//            });
//
//            holder.maopaoDelete = convertView.findViewById(R.id.maopaoDelete);
//            holder.maopaoDelete.setOnClickListener(onClickDeleteMaopao);
//
//            holder.commentArea = new CommentArea(convertView, onClickComment, myImageGetter);
//
//
//            View[] divides = new View[commentsId.length];
//            for (int i = 0; i < commentsId.length; ++i) {
//                divides[i] = convertView.findViewById(commentsId[i]).findViewById(R.id.commentTopDivider);
//            }
//            holder.divides = divides;
//
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        final Maopao.MaopaoObject data = (Maopao.MaopaoObject) getItem(position);
//
//        holder.likeUsersArea.likeUsersLayout.setTag(TAG_MAOPAO, data);
//        holder.likeUsersArea.displayLikeUser();
//
//        if (data.likes > 0 || data.comments > 0) {
//            holder.commentLikeArea.setVisibility(View.VISIBLE);
//        } else {
//            holder.commentLikeArea.setVisibility(View.GONE);
//        }
//
//        MaopaoLocationArea.bind(holder.location, data);
//
//        String device = data.device;
//        if (!device.isEmpty()) {
//            final String format = "来自 %s";
//            device = String.format(format, device);
//            holder.photoType.setVisibility(View.VISIBLE);
//        } else {
//            holder.photoType.setVisibility(View.GONE);
//        }
//        holder.photoType.setText(device);
//
//        new ImageLoadTool().loadImage(holder.icon, data.owner.avatar);
//        holder.icon.setTag(data.owner.global_key);
//
//        holder.name.setText(data.owner.name);
//        holder.name.setTag(data.owner.global_key);
//
//        holder.maopaoItem.setTag(data);
//
//        holder.contentArea.setData(data);
//
//        holder.time.setText(Global.dayToNow(data.created_at));
//
//        holder.likeBtn.setOnCheckedChangeListener(null);
//        holder.likeBtn.setChecked(data.liked);
//        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String type = ((CheckBox) v).isChecked() ? "like" : "unlike";
//                String uri = String.format(HOST_GOOD, data.id, type);
//                v.setTag(data);
//
//                postNetwork(uri, new RequestParams(), HOST_GOOD, 0, data);
//            }
//        });
//
//        if (data.likes > 0) {
//            holder.likeAreaDivide.setVisibility(data.comments > 0 ? View.VISIBLE : View.INVISIBLE);
//        }
//
//        holder.commentBtn.setTag(data);
//
//        if (data.owner_id == (MyApp.sUserObject.id)) {
//            holder.maopaoDelete.setVisibility(View.VISIBLE);
//            holder.maopaoDelete.setTag(TAG_MAOPAO_ID, data.id);
//        } else {
//            holder.maopaoDelete.setVisibility(View.INVISIBLE);
//        }
//
//
//        holder.commentArea.displayContentData(data);
//
//        int commentCount = data.comment_list.size();
//        int needShow = commentCount - 1;
//        for (int i = 0; i < commentsId.length; ++i) {
//            if (i < needShow) {
//                holder.divides[i].setVisibility(View.VISIBLE);
//            } else {
//                holder.divides[i].setVisibility(View.INVISIBLE);
//            }
//        }
//        if (commentsId.length < data.comments) { // 评论数超过5时
//            holder.divides[commentsId.length - 1].setVisibility(View.VISIBLE);
//        }
//
//        if (mData.size() - position <= 1) {
//            if (!mNoMore) {
//                getNetwork(createUrl(), maopaoUrlFormat);
//            }
//        }

        return convertView;
    }

    static class ViewHolder {
        View maopaoItem;

        ImageView icon;
        TextView name;
        TextView time;
        ContentArea contentArea;

        View maopaoDelete;

        TextView photoType;
        CheckBox likeBtn;
        CheckBox commentBtn;

        LikeUsersArea likeUsersArea;
        View commentLikeArea;

        CommentArea commentArea;

        View[] divides;

        View likeAreaDivide;
        TextView location;
    }
}
