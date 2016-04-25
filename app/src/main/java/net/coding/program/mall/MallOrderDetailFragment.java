package net.coding.program.mall;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.network.RefreshBaseFragment;
import net.coding.program.model.MallOrderObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by libo on 2015/11/22.
 */
@EFragment(R.layout.fragment_mall_order_detail)
public class MallOrderDetailFragment extends RefreshBaseFragment {

    @ViewById
    ListView listView;

    @ViewById
    View blankLayout;

    @ViewById
    View list_footer;

    @FragmentArg
    Type mType;

    private AtomicBoolean footerAdded = new AtomicBoolean(false);

    ArrayList<MallOrderObject> mData = new ArrayList<>();

    String mUrl;

    View.OnClickListener onClickRetry = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };

    BaseAdapter mAdapter = new BaseAdapter() {
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
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = LayoutInflater.from(getActivity())
                        .inflate(R.layout.mall_order_detail_item, parent, false);
                holder = new ViewHolder();
                holder.orderNo = (TextView) view.findViewById(R.id.order_no);
                holder.goodImg = (ImageView) view.findViewById(R.id.order_img);
                holder.goodTitle = (TextView) view.findViewById(R.id.order_title);
                holder.count = (TextView) view.findViewById(R.id.order_point);
                holder.pointCost = (TextView) view.findViewById(R.id.order_point);
                holder.note = (TextView) view.findViewById(R.id.order_note);
                holder.receiverName = (TextView) view.findViewById(R.id.order_receiver_name);
                holder.receiverPhone = (TextView) view.findViewById(R.id.order_receiver_phone);
                holder.status = (TextView) view.findViewById(R.id.order_status);
                holder.express = (TextView) view.findViewById(R.id.order_express);
                holder.time = (TextView) view.findViewById(R.id.order_time);
                holder.receiverAddress = (TextView) view.findViewById(R.id.order_addr);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            MallOrderObject item = (MallOrderObject) getItem(position);

            holder.orderNo.setText(item.getOrderNo());

            long timeLong = item.getCreatedAt();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(new Date(timeLong));
            holder.time.setText(time);

            String titleString;
            if (item.getOptionName().isEmpty()) {
                titleString = item.getName();
            } else {
                String titleFormat = "%s <font color=#666666><small>%s</small></font>";
                titleString = String.format(titleFormat, item.getName(), item.getOptionName());
            }
            holder.goodTitle.setText(Html.fromHtml(titleString));
            getImageLoad().loadImage(holder.goodImg, item.getGiftImage());
            holder.pointCost.setText(item.getPointsCost() + " 码币");

            if (item.getRemark().equals("")) {
                holder.note.setText("暂无");
            } else {
                holder.note.setText(item.getRemark());
            }

            holder.receiverName.setText(item.getReceiverName());
            holder.receiverPhone.setText(item.getReceiverPhone());

            int status = item.getStatus();
            switch (status) {
                case 0:
                    holder.status.setText("未发货");
                    break;
                case 1:
                    holder.status.setText("已发货");
                    break;
                case 2:
                    holder.status.setText("已完成");
                    break;
            }

            String express = item.getExpressNo();
            if (express.equals("")) {
                holder.express.setText("暂无");
            } else {
                holder.express.setText(item.getExpressNo());
            }
            holder.receiverAddress.setText(item.getReceiverAddress());

            if (position == (getCount() - 1)) {
                loadMore();
            }

            return view;
        }
    };

    @AfterViews
    protected final void init() {
        initRefreshLayout();
//        mFootUpdate.init(listView, mInflater, this);
        list_footer.setVisibility(View.GONE);

        listView.setAdapter(mAdapter);
        switch (mType) {
            case un_send:
                mUrl = Global.HOST_API + "/gifts/orders?";
                break;
            case already_send:
                mUrl = Global.HOST_API + "/gifts/orders?";
                break;
            default: // 0
                mUrl = Global.HOST_API + "/gifts/orders?";
                break;
        }

        loadMore();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data)
            throws JSONException {
        setRefreshing(false);
        hideProgressDialog();
        if (tag.equals(mUrl)) {
            if (code == 0) {
                if (isLoadingFirstPage(tag)) {
                    mData.clear();
                }

                JSONArray jsonArray = respanse.optJSONObject("data").optJSONArray("list");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    MallOrderObject orderObject = new MallOrderObject(json);
                    if (mType == Type.all_order) {
                        mData.add(orderObject);
                    } else if (mType == Type.already_send) {
                        if (orderObject.getStatus() == 1) {
                            mData.add(orderObject);
                        }
                    } else {
                        if (orderObject.getStatus() == 0) {
                            mData.add(orderObject);
                        }
                    }
                }

//                MallOrderObject orderObject = new MallOrderObject();
//                orderObject.setCreatedAt(1448174663000L);
//                orderObject.setExpressNo("expressNo");
//                orderObject.setId(1);
//                orderObject.setName("name");
//                orderObject.setOrderNo("12312421");
//                orderObject.setPointsCost(2.2);
//                orderObject.setReceiverAddress("address");
//                orderObject.setReceiverName("receiverName");
//                orderObject.setReceiverPhone("123532424");
//                orderObject.setRemark("remark");
//                orderObject.setStatus(0);
//                orderObject.setUserId(23424);
//
//                mData.add(orderObject);

                if (mData.size() != 0 && footerAdded.compareAndSet(false, true)) {

                    View footerView = LayoutInflater.from(getActivity()).inflate(
                            R.layout.mall_detail_list_footer, null);
                    listView.addFooterView(footerView, null, false);
                }

//                mFootUpdate.updateState(code, isLoadingLastPage(tag), mData.size());
                String tip = BlankViewDisplay.OTHER_MALL_ORDER_BLANK;
                if (mType == Type.already_send) {
                    tip = BlankViewDisplay.OTHER_MALL_ORDER_BLANK_ALREADYSEND;
                } else if (mType == Type.un_send) {
                    tip = BlankViewDisplay.OTHER_MALL_ORDER_BLANK_ALREADYSEND;
                }
                BlankViewDisplay.setBlank(mData.size(), this, true, blankLayout, onClickRetry, tip);

                mAdapter.notifyDataSetChanged();
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    @Override
    public void loadMore() {
        if (!isLoadingFirstPage(mUrl) && !isLoadingLastPage(mUrl)) {
            showDialogLoading();
        }
        getNextPageNetwork(mUrl, mUrl);

    }

    @Override
    public void onRefresh() {
        initSetting();
        loadMore();
    }

    public enum Type {
        all_order,
        un_send,
        already_send
    }

    class ViewHolder {

        ImageView goodImg;

        TextView orderNo;

        TextView goodTitle;

        TextView count;

        TextView note;

        TextView pointCost;

        TextView receiverName;

        TextView receiverPhone;

        TextView status;

        TextView express;

        TextView time;

        TextView receiverAddress;

    }

}
