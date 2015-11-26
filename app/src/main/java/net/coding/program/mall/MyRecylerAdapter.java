package net.coding.program.mall;

import net.coding.program.R;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.model.MallItemObject;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by libo on 2015/11/26.
 */
public class MyRecylerAdapter extends RecyclerView.Adapter<MyRecylerAdapter.ViewHolder> {

    private ArrayList<MallItemObject> mDataList = new ArrayList<>();

    private double userPoint;

    private ImageLoadTool imageLoader;

    private Context context;

    public void addAll(ArrayList<MallItemObject> data) {
        this.mDataList.addAll(data);
    }

    public void addData(MallItemObject data) {
        this.mDataList.add(data);
    }

    public void removeAll() {
        mDataList.clear();
    }

    public MyRecylerAdapter(ArrayList<MallItemObject> mData, double userPoint,
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


        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.mall_list_item_title);
            points_cost = (TextView) itemView.findViewById(R.id.mall_list_item_cost);
            image = (ImageView) itemView.findViewById(R.id.mall_list_item_img);
            exchange = (ImageView) itemView.findViewById(R.id.mall_list_item_exchange);
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
        holder.points_cost.setText(object.getPoints_cost() + "");

        String imgUrl = object.getImage();
        imageLoader.loadImage(holder.image, imgUrl);

        double cost = object.getPoints_cost();
        if (userPoint < cost) {
            holder.exchange
                    .setImageDrawable(context.getResources().getDrawable(R.drawable.ic_unexchange));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (object.getPoints_cost() > userPoint) {
                    Toast.makeText(context, "您的码币不足！", Toast.LENGTH_SHORT).show();
                } else {
                    MallOrderSubmitActivity_.intent(context)
                            .giftId(object.getId())
                            .desc(object.getDescription())
                            .imgUrl(object.getImage())
                            .point(object.getPoints_cost())
                            .title(object.getName())
                            .start();
                }
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mall_list_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }
}
