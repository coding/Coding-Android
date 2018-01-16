package net.coding.program.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.HtmlContent;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.ViewHolder;
import net.coding.program.common.model.Maopao;
import net.coding.program.maopao.MaopaoDetailActivity_;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Vernon on 15/11/28.
 */
public class SearchMaopaoAdapter extends BaseAdapter {
    private List<Maopao.MaopaoObject> mData;
    private Context context;
    private Html.ImageGetter mImageGetter;

    public SearchMaopaoAdapter(List<Maopao.MaopaoObject> mData, Html.ImageGetter imageGetter, Context context) {
        this.mData = mData;
        this.context = context;
        this.mImageGetter = imageGetter;
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
            convertView = View.inflate(context, R.layout.search_maopao_list, null);
        }
        TextView descMaopao = ViewHolder.get(convertView, R.id.descMaopao);
        ImageView personImg = ViewHolder.get(convertView, R.id.personImg);
        TextView bottomName = ViewHolder.get(convertView, R.id.bottomName);
        TextView bottomTime = ViewHolder.get(convertView, R.id.bottomTime);
        TextView bottomHeartCount = ViewHolder.get(convertView, R.id.bottomHeartCount);
        TextView bottomCommentCount = ViewHolder.get(convertView, R.id.bottomCommentCount);

        Maopao.MaopaoObject bean = mData.get(position);

        descMaopao.setText(GlobalCommon.changeHyperlinkColor(HtmlContent.parseReplacePhotoMonkey(bean.content), mImageGetter, Global.tagHandler));
        bottomName.setText(bean.owner.name);
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        bottomTime.setText(format.format(bean.created_at));
        bottomHeartCount.setText(bean.likes + "");
        bottomCommentCount.setText(bean.comments + "");
        ImageLoader.getInstance().displayImage(bean.owner.avatar, personImg, ImageLoadTool.options);
        setClickEvent(convertView, bean);
        return convertView;
    }

    private void setClickEvent(View view, final Maopao.MaopaoObject bean) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaopaoDetailActivity_.intent(context).mMaopaoObject(bean).start();
            }
        });
    }
}
