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
import net.coding.program.common.model.payed.Billing;
import net.coding.program.common.ui.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

@EFragment(R.layout.common_ul_recyclerview)
public class BillingListFragment extends BaseFragment {

    @ViewById
    UltimateRecyclerView listView;

    MyAdapter myAdapter;

    @AfterViews
    void initBillingListFragment() {
        OrderMainActivity activity = (OrderMainActivity) getActivity();

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(manager);
        myAdapter = new MyAdapter(activity.getBillingList());
        listView.setAdapter(myAdapter);
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
            myAdapter.notifyDataSetChanged();
        }
    }

    protected class MyAdapter extends easyRegularAdapter<Billing, BillingListFragment.ViewHolder> {

        public MyAdapter(List<Billing> list) {
            super(list);
        }

        @Override
        protected int getNormalLayoutResId() {
            return R.layout.billing_item;
        }

        @Override
        protected BillingListFragment.ViewHolder newViewHolder(View view) {
            return new BillingListFragment.ViewHolder(view);
        }

        @Override
        protected void withBindHolder(BillingListFragment.ViewHolder holder, Billing data, int position) {
            holder.title.setText(data.getTitle());
            holder.count.setText(String.format("%s 人", data.userCount));
            holder.price.setText(String.format("¥ %s", data.price));
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
        private TextView count;
        private TextView price;
        private TextView time;
        private View nextTopDivide;

        public ViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.count);
            price = (TextView) view.findViewById(R.id.price);
            time = (TextView) view.findViewById(R.id.time);
            nextTopDivide = view.findViewById(R.id.nextTopDivide);
        }
    }


}
