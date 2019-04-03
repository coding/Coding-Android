package net.coding.program.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.ViewHolder;
import net.coding.program.common.model.UserObject;
import net.coding.program.message.MessageListActivity_;
import net.coding.program.search.HoloUtils;

import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Vernon on 15/11/30.
 */
public class SearchUserAdapter extends BaseAdapter {
    private List<UserObject> mData;
    private Context context;
    private String key;

    public SearchUserAdapter(List<UserObject> mData, String key, Context context) {
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
            convertView = View.inflate(context, R.layout.search_user_list, null);
        }
        TextView txtTitle = ViewHolder.get(convertView, R.id.txtTitle);
        CircleImageView personImg = ViewHolder.get(convertView, R.id.personImg);
        TextView txtContent = ViewHolder.get(convertView, R.id.txtContent);
        RelativeLayout btn_action = ViewHolder.get(convertView, R.id.btn_action);

        UserObject bean = mData.get(position);

        HoloUtils.setHoloText(txtTitle, bean.name);
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        txtContent.setText(format.format(bean.created_at) + "    加入coding");
        ImageLoader.getInstance().displayImage(bean.avatar, personImg, ImageLoadTool.options);
        setClickEvent(btn_action, bean);
        return convertView;
    }

    private void setClickEvent(View view, final UserObject bean) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageListActivity_.class);
                bean.global_key = bean.global_key.replace("<em>", "").replace("</em>", "");
                intent.putExtra("mUserObject", bean);
                context.startActivity(intent);
            }
        });
    }

}

