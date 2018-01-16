package net.coding.program.setting.order;


import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.quickAdapter.easyRegularAdapter;

import net.coding.program.R;
import net.coding.program.common.event.EventRefresh;
import net.coding.program.common.model.payed.Order;
import net.coding.program.common.ui.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

@EFragment(R.layout.common_ul_recyclerview)
public class OrderListFragment extends BaseFragment {

    @ViewById
    UltimateRecyclerView listView;

    MyAdapter adapter;

    @AfterViews
    void initOrderListFragment() {
        OrderMainActivity activity = (OrderMainActivity) getActivity();

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(manager);

        adapter = new MyAdapter(activity.getOrderList());
        listView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventRefresh(EventRefresh event) {
        if (event.refresh) {
            adapter.notifyDataSetChanged();
        }
    }

    protected class MyAdapter extends easyRegularAdapter<Order, OrderListFragment.ViewHolder> {

        public MyAdapter(List<Order> list) {
            super(list);
        }

        @Override
        protected int getNormalLayoutResId() {
            return R.layout.order_item;
        }

        @Override
        protected ViewHolder newViewHolder(View view) {
            return new ViewHolder(view);
        }

        @Override
        protected void withBindHolder(ViewHolder holder, Order data, int position) {

            holder.title.setText(data.getAction());
            holder.style.setText(data.statusString);
            holder.style.setTextColor(data.statusColor);
            holder.id.setText(data.number);
            holder.user.setText(data.creatorName);
            holder.time.setText(data.getTime());

            if (position == source.size() - 1) {
                holder.nextTopDivide.setVisibility(View.GONE);
            } else {
                holder.nextTopDivide.setVisibility(View.VISIBLE);
            }
        }
    }

    class ViewHolder extends UltimateRecyclerviewViewHolder {

        private TextView title;
        private TextView style;
        private View divideLine;
        private TextView orderId;
        private TextView orderUser;
        private TextView orderTime;
        private TextView id;
        private TextView user;
        private TextView time;

        private View nextTopDivide;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            style = (TextView) view.findViewById(R.id.style);
            divideLine = (View) view.findViewById(R.id.divideLine);
            orderId = (TextView) view.findViewById(R.id.orderId);
            orderUser = (TextView) view.findViewById(R.id.orderUser);
            orderTime = (TextView) view.findViewById(R.id.orderTime);
            id = (TextView) view.findViewById(R.id.id);
            user = (TextView) view.findViewById(R.id.user);
            time = (TextView) view.findViewById(R.id.time);
            nextTopDivide = view.findViewById(R.id.nextTopDivide);
        }
    }

}
