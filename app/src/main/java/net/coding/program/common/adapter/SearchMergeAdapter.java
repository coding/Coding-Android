package net.coding.program.common.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.ViewHolder;
import net.coding.program.common.widget.CircleImageView;
import net.coding.program.model.MergeObject;
import net.coding.program.search.HoloUtils;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Vernon on 15/11/30.
 */
public class SearchMergeAdapter extends BaseAdapter {
    private List<MergeObject> mData;
    private Context context;
    private String key;

    public SearchMergeAdapter(List<MergeObject> mData, String key, Context context) {
        this.mData = mData;
        this.key = key;
        this.context = context;
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
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.search_merge_list, null);
        }
        TextView txtTitle = ViewHolder.get(convertView, R.id.txtTitle);
        CircleImageView personImg = ViewHolder.get(convertView, R.id.personImg);
        TextView txtMergeName = ViewHolder.get(convertView, R.id.txtMergeName);
        TextView txtMergeBranch = ViewHolder.get(convertView, R.id.txtMergeBranch);
        TextView txtContent = ViewHolder.get(convertView, R.id.txtContent);
        TextView txtBottomName = ViewHolder.get(convertView, R.id.txtBottomName);
        TextView bottomTime = ViewHolder.get(convertView, R.id.bottomTime);
        TextView bottomStatus = ViewHolder.get(convertView, R.id.bottomStatus);


        MergeObject bean = mData.get(position);

        HoloUtils.setHoloText(txtTitle, key, bean.getTitle());
        txtMergeName.setText(bean.getSrcBranch());

        txtMergeBranch.setText(bean.getDesBranch());
        HoloUtils.setHoloText(txtContent, key, bean.getBody());
        txtBottomName.setText(bean.getTitleIId() + "  " + bean.getAuthor().name);
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        bottomTime.setText(format.format(bean.getCreated_at()));
        bottomStatus.setText(bean.getMergeStatus());
        bottomStatus.setTextColor(Color.parseColor(bean.getColor()));
        ImageLoader.getInstance().displayImage(bean.getAuthor().avatar, personImg, ImageLoadTool.options);
        return convertView;
    }

}

