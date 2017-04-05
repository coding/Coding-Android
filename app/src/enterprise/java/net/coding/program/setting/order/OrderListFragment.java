package net.coding.program.setting.order;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.event.EventRefresh;
import net.coding.program.model.payed.Order;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

@EFragment(R.layout.common_listview)
public class OrderListFragment extends BaseFragment {

    ArrayList<Order> orderList;

    @ViewById
    ListView listView;

    @AfterViews
    void initOrderListFragment() {
        OrderMainActivity activity = (OrderMainActivity) getActivity();
        orderList = activity.getOrderList();
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

    BaseAdapter adapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return orderList.size();
        }

        @Override
        public Order getItem(int position) {
            return orderList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }

            initializeViews(position, getItem(position), (ViewHolder) convertView.getTag());
            return convertView;
        }

        private void initializeViews(int position, Order data, ViewHolder holder) {
            holder.title.setText(data.getAction());
            holder.style.setText(data.statusString);
            holder.style.setTextColor(data.statusColor);
            holder.id.setText(data.number);
            holder.user.setText(data.creatorName);
            holder.time.setText(data.getTime());

            if (position == getCount() - 1) {
                holder.nextTopDivide.setVisibility(View.GONE);
            } else {
                holder.nextTopDivide.setVisibility(View.VISIBLE);
            }
        }

        class ViewHolder {
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
    };

}
