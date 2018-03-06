package net.coding.program.mall;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.model.MallItemObject;
import net.coding.program.common.util.SingleToast;
import net.coding.program.setting.ValidePhoneActivity_;

import java.math.BigDecimal;
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

    public MyRecyclerAdapter(ArrayList<MallItemObject> mData, double userPoint,
                             ImageLoadTool imageLoader, Context context) {
        this.mDataList.addAll(mData);
        this.userPoint = userPoint;
        this.imageLoader = imageLoader;
        this.context = context;
    }

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

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MallItemObject object = mDataList.get(position);

        holder.name.setText(object.getName());
        holder.points_cost.setText(object.getShowPoints());
        holder.sales.setText("销量：" + String.valueOf(object.count));
        holder.rmbPrice.setText(GlobalCommon.mbToRmb(object.points_cost));

        String imgUrl = object.getImage();
        imageLoader.loadImageDefaultCoding(holder.image, imgUrl);

        if (object.points_cost.compareTo(new BigDecimal(userPoint)) > 0) {
            holder.exchangeable.setVisibility(View.INVISIBLE);
        } else {
            holder.exchangeable.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!GlobalData.sUserObject.phone.isEmpty()) {
                MallOrderSubmitActivity_.intent(context)
                        .mallItemObject(object)
                        .start();
            } else {
                SingleToast.showMiddleToast(context, "验证手机号才能下单");
                ValidePhoneActivity_.intent(context).start();
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mall_list_item, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        TextView points_cost;

        TextView rmbPrice;
        TextView sales;

        ImageView image;

        ViewGroup container;
        View exchangeable;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.mall_list_item_title);
            points_cost = (TextView) itemView.findViewById(R.id.mall_list_item_cost);
            image = (ImageView) itemView.findViewById(R.id.mall_list_item_img);
            container = (ViewGroup) itemView.findViewById(R.id.mall_list_item_container);

            rmbPrice = (TextView) itemView.findViewById(R.id.rmbPrice);
            sales = (TextView) itemView.findViewById(R.id.sales);
            exchangeable = itemView.findViewById(R.id.exchangeable);
        }
    }
}
