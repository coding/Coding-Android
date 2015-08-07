package net.coding.program.maopao.banner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.model.BannerObject;
import net.coding.program.third.salvage.RecyclingPagerAdapter;

import java.util.List;

/**
 * Created by chenchao on 15/7/30.
 * 广告 Fragment，用于左右滑动
 */
public class BannerAdapter extends RecyclingPagerAdapter {

    private Context context;
    private List<BannerObject> imageIdList;
    private ImageLoadTool imageLoadTool;

    private int size;

    public BannerAdapter(Context context, List<BannerObject> imageIdList, ImageLoadTool imageLoadTool) {
        this.context = context;
        this.imageIdList = imageIdList;
        this.size = imageIdList.size();
        this.imageLoadTool = imageLoadTool;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    public int translatePosition(int position) {
        return position % size;
    }

    @Override
    public View getView(int position, View view, ViewGroup container) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.fragment_banner_item, null);
            holder.imageView = (ImageView) view.findViewById(R.id.image);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BannerObject bannerObject = (BannerObject) v.getTag();
                    URLSpanNoUnderline.openActivityByUri(context, bannerObject.getLink(), false);
                }
            });
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        BannerObject data = imageIdList.get(translatePosition(position));

        String imageUrl = data.getImage();
        imageLoadTool.loadImage(holder.imageView, imageUrl, ImageLoadTool.bannerOptions);
        holder.imageView.setTag(data);

        return view;
    }

    public int getStartPos() {
        return Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2 % imageIdList.size();
    }

    private static class ViewHolder {
        ImageView imageView;
    }

}
