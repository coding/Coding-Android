package net.coding.program.project.detail.merge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.FootUpdate;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.widget.DataAdapter;
import net.coding.program.model.Merge;
import net.coding.program.project.detail.TopicListFragment;

import java.util.ArrayList;

/**
 * Created by chenchao on 15/5/27.
 * merge 列表的 adpter
 */
public class MergeAdapter extends DataAdapter<Merge> {

    ImageLoadTool mImageLoadr;
    FootUpdate.LoadMore mLoadMore;

    public MergeAdapter(ArrayList<Merge> data, FootUpdate.LoadMore loadMore, ImageLoadTool imageLoader) {
        super(data);
        mLoadMore = loadMore;
        mImageLoadr = imageLoader;
    }

    class MergeItemHolder extends TopicListFragment.ViewHolder {
        TextView mergeId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MergeItemHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_project_merge_list_item, parent, false);
            holder = new MergeItemHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
//            holder.icon.setOnClickListener(onClickUser);

            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.time.setFocusable(false);

            holder.discuss = (TextView) convertView.findViewById(R.id.discuss);
            holder.discuss.setVisibility(View.GONE);

            holder.refId = (TextView) convertView.findViewById(R.id.referenceId);
            holder.refId.setVisibility(View.GONE);

            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.mergeId = (TextView) convertView.findViewById(R.id.mergeId);

            convertView.setTag(holder);

        } else {
            holder = (MergeItemHolder) convertView.getTag();
        }

        if (getCount() - 1 <= position) {
            mLoadMore.loadMore();
        }

        Merge data = (Merge) getItem(position);

        mImageLoadr.loadImage(holder.icon, data.getAuthor().avatar);
        holder.icon.setTag(data.getAuthor().global_key);

        holder.title.setText(data.getTitleSpannable());

        holder.name.setText(data.getAuthor().name);
        holder.time.setText(Global.dayToNowCreate(data.getCreatedAt()));
//        holder.discuss.setText(String.format("%d", data.child_count));
        holder.mergeId.setText(data.getTitleIId());

        return convertView;
    }

}
