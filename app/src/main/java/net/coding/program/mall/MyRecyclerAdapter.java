package net.coding.program.mall;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.coding.program.MyApp;
import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.util.SingleToast;
import net.coding.program.model.MallItemObject;
import net.coding.program.setting.ValidePhoneActivity_;

import java.util.ArrayList;

/**
 * Created by libo on 2015/11/26.
 */
public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private ArrayList<MallItemObject> mDataList = new ArrayList<>();

    private double userPoint;

    private ImageLoadTool imageLoader;

    private Context context;

    private int lastPosition = -1;

    public void addAll(ArrayList<MallItemObject> data) {
        this.mDataList.addAll(data);
    }

    public void addData(MallItemObject data) {
        this.mDataList.add(data);
    }

    public void removeAll() {
        mDataList.clear();
    }

    public void setUserPoint(double userPoint) {
        this.userPoint = userPoint;
    }

    public MyRecyclerAdapter(ArrayList<MallItemObject> mData, double userPoint,
                             ImageLoadTool imageLoader, Context context) {
        this.mDataList.addAll(mData);
        this.userPoint = userPoint;
        this.imageLoader = imageLoader;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        TextView points_cost;

        ImageView image;

        ImageView exchange;

        LinearLayout container;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.mall_list_item_title);
            points_cost = (TextView) itemView.findViewById(R.id.mall_list_item_cost);
            image = (ImageView) itemView.findViewById(R.id.mall_list_item_img);
            exchange = (ImageView) itemView.findViewById(R.id.mall_list_item_exchange);
            container = (LinearLayout) itemView.findViewById(R.id.mall_list_item_container);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MallItemObject object = mDataList.get(position);
        holder.name.setText(object.getName());
        holder.points_cost.setText(object.getPoints_cost() + " 码币");

        String imgUrl = object.getImage();
        imageLoader.loadImageDefaultCoding(holder.image, imgUrl);

        double cost = object.getPoints_cost();
        if (userPoint < cost) {
            holder.exchange
                    .setImageDrawable(context.getResources().getDrawable(R.drawable.ic_unexchange));
        } else {
            holder.exchange
                    .setImageDrawable(context.getResources().getDrawable(R.drawable.ic_exchange));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (object.getPoints_cost() > userPoint) {
                    Toast.makeText(context, "您的码币不足！", Toast.LENGTH_SHORT).show();
                } else {
                    if (!MyApp.sUserObject.phone.isEmpty()) {
                        MallOrderSubmitActivity_.intent(context)
                                .mallItemObject(object)
                                .start();
                    } else {
                        SingleToast.showMiddleToast(context, "验证手机号才能下单");
                        ValidePhoneActivity_.intent(context).start();
                    }
                }
            }
        });

        setAnimation(holder.container, position);
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        holder.container.clearAnimation();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mall_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils
                    .loadAnimation(context, R.anim.item_bottom_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}
