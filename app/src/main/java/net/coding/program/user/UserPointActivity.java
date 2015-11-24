package net.coding.program.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.WebActivity_;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.common.widget.DataAdapter;
import net.coding.program.model.PointObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

@EActivity(R.layout.activity_user_point)
//@OptionsMenu(R.menu.menu_user_point)
public class UserPointActivity extends BackActivity {

    private static final String TAG_HTTP_USER_POINT = "TAG_HTTP_USER_POINT";
    private static final String TAG_HTTP_ALL_POINTS = "TAG_HTTP_ALL_POINTS";
    @ViewById
    StickyListHeadersListView listView;

    TextView pointsAll;

    PointRecordAdapter adapter = new PointRecordAdapter();

    @AfterViews
    protected final void initUserPointActivity() {
        View head = mInflater.inflate(R.layout.user_point_list_head, null);
        pointsAll = (TextView) head.findViewById(R.id.pointAll);
        head.findViewById(R.id.itemShop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebActivity_.intent(UserPointActivity.this)
                        .url(Global.HOST + "/shop")
                        .start();
            }
        });
        listView.addHeaderView(head, null, false);

        View footShade = mInflater.inflate(R.layout.divide_shade_up, null);
        listView.addFooterView(footShade);

        getNetwork(PointObject.getHttpPointsAll(), TAG_HTTP_ALL_POINTS);
        listView.setAdapter(adapter);
        getNextPageNetwork(PointObject.getHttpRecord(), TAG_HTTP_USER_POINT);

        listView.setAreHeadersSticky(false);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_HTTP_USER_POINT)) {
            if (code == 0) {
                JSONArray json = respanse.optJSONObject("data").optJSONArray("list");
                ArrayList<PointObject> mData = new ArrayList<>();
                for (int i = 0; i < json.length(); ++i) {
                    mData.add(new PointObject(json.optJSONObject(i)));
                }
                adapter.appendDataUpdate(mData);
            } else {
                showErrorMsg(code, respanse);
            }
        } else if (tag.equals(TAG_HTTP_ALL_POINTS)) {
            if (code == 0) {
                double points = respanse.optJSONObject("data").optDouble("points_left");
                pointsAll.setText(String.format("%.2f", points));
            } else {
                showErrorMsg(code, respanse);
            }
        }
    }

    static class ViewHold {
        private TextView usage;
        private TextView pointsChange;
        private TextView create;
        private TextView pointsLeft;

        public ViewHold(View v) {
            usage = (TextView) v.findViewById(R.id.usage);
            pointsChange = (TextView) v.findViewById(R.id.pointsChange);
            create = (TextView) v.findViewById(R.id.create);
            pointsLeft = (TextView) v.findViewById(R.id.pointLeft);
        }

        public void bind(PointObject data) {
            usage.setText(Global.changeHyperlinkColor(data.getUsage(), 0xFF222222));
            double points_change = data.getPoints_change();
            String changeForamt;
            int textColor;
            if (data.isIncome()) {
                changeForamt = String.format("+%.2f", points_change);
                textColor = 0xff3bbd79;
            } else {
                changeForamt = String.format("-%.2f", points_change);
                textColor = 0xfffb8638;
            }
            pointsChange.setText(changeForamt);
            pointsChange.setTextColor(textColor);
            create.setText(Global.mDateYMDHH.format(data.getCreated_at()));
            pointsLeft.setText(String.format("余额: %.2f", data.getPoints_left()));
        }
    }

    private class PointRecordAdapter extends DataAdapter<PointObject> implements StickyListHeadersAdapter {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHold hold;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_point_list_item,
                        null);
                hold = new ViewHold(convertView);
                convertView.setTag(hold);
            } else {
                hold = (ViewHold) convertView.getTag();
            }

            hold.bind((PointObject) getItem(position));

            if (position == getCount() - 3) {
                getNextPageNetwork(PointObject.getHttpRecord(), TAG_HTTP_USER_POINT);
            }

            return convertView;
        }

        @Override
        public View getHeaderView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
            if (i == 0) {
                return layoutInflater.inflate(R.layout.divide_15_top_bottom, null);
            } else {
                return layoutInflater.inflate(R.layout.divide_0, null);
            }
        }

        @Override
        public long getHeaderId(int i) {
            if (i > 0) {
                return 1;
            }

            return i;
        }
    }
}