package net.coding.program.setting.order;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spanned;
import android.text.SpannedString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.Global;
import net.coding.program.common.event.EventRefresh;
import net.coding.program.common.model.EnterpriseAccount;
import net.coding.program.common.model.EnterpriseInfo;
import net.coding.program.common.model.payed.Billing;
import net.coding.program.common.model.payed.Order;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.third.WechatTab;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

@EActivity(R.layout.activity_order_main)
public class OrderMainActivity extends BackActivity {

    private static final String TAG_ORDERS = "TAG_ORDERS";
    private static final String TAG_BILLINGS = "TAG_BILLINGS";
    private static final String TAG_INFO = "TAG_INFO";

    @Extra
    EnterpriseAccount account;

    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;

    @ViewById
    AppBarLayout appbarLayout;

    @ViewById
    TextView topWarn, balanceTitle, balanceContent, balanceTip;

    @ViewById
    View topTip;

    @ViewById
    ViewGroup containerHeader;

    @ViewById
    ViewGroup rootLayout;

    @ViewById
    ViewPager viewPager;

    @ColorRes(R.color.font_orange)
    int fontOragne;

    @ColorRes(R.color.font_red)
    int fontRed;

    @ViewById
    ImageView closeTipButton;

    private ArrayList<Order> orderList = new ArrayList<>();
    private ArrayList<Billing> billingList = new ArrayList<>();

    AppBarLayout.OnOffsetChangedListener appbarOffsetChange = new AppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            Logger.d("offset " + verticalOffset);
            swipeRefreshLayout.setEnabled(verticalOffset >= 0);
        }
    };

    @AfterViews
    void initOrderMainActivity() {
        closeTipButton.setImageDrawable(Global.tintDrawable(
                getResources().getDrawable(R.drawable.delete_edit_login_black), CodingColor.fontYellow));

        initHeader();

        swipeRefreshLayout.setColorSchemeResources(R.color.font_green);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            String host = String.format("%s/enterprise/%s", Global.HOST_API, EnterpriseInfo.instance().getGlobalkey());
            getNetwork(host, TAG_INFO);

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        appbarLayout.addOnOffsetChangedListener(appbarOffsetChange);
    }

    @Override
    public void onPause() {
        super.onPause();
        appbarLayout.addOnOffsetChangedListener(appbarOffsetChange);
    }

    private void initHeader() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy 年 MM月 dd日");
        String timeString = df.format(Long.valueOf(account.estimateDate));
        SpannedString empty = new SpannedString("");

        boolean showTip = false;
        Spanned warnString = empty;
        Spanned balanceTitleString = empty;
        Spanned balanceContentString = empty;
        String balanceTipString = "";

        if (account.trial && !account.payed) { // 处于试用期
            warnString = Global.createColorHtml("您正在免费试用 CODING 企业服务", fontOragne);
            balanceTipString = String.format("预计可使用至 %s，剩余 %s 天", timeString, account.remaindays);

            if (account.payed) { // 试用期且付过费
                balanceTitleString = new SpannedString("账户余额（元）");
                balanceContentString = Global.createColorHtml(account.balance, fontOragne);
                showOrderAndRecord(true);
            } else {
                showOrderAndRecord(false);
            }

        } else {
            if (account.payed) {
                if (account.remaindays > 0) { // 付费期且未到期
                    showTip = false;
                    if (account.remaindays > 5) {
                    } else {
                        warnString = Global.createColorHtml("您的账户余额不足，请尽快订购", fontRed);
                    }

                    balanceTitleString = new SpannedString("账户余额（元）");
                    balanceContentString = Global.createColorHtml(account.balance, fontOragne);
                    balanceTipString = String.format("预计可使用至 %s，剩余 %s 天", df.format(account.estimateDate), account.remaindays);

                    showOrderAndRecord(true);

                } else { // 付费期已到期
                    showTip = true;
                    warnString = Global.createColorHtml("您的服务已过期，请订购后使用", fontRed);
                    balanceTitleString = new SpannedString("已透支金额（元）");
                    balanceContentString = Global.createColorHtml(account.balance, fontRed);

                    if (account.suspendedAt > 0) { // 已暂停
                        balanceTipString = String.format("服务已暂停 %s 天", account.suspendedToToday());
                        showOrderAndRecord(false);
                    } else { // 处于超时使用阶段
                        balanceTipString = String.format("过期时间为 %s，已超时使用 %s 天", df.format(account.estimateDate), account.estimateDate());
                        showOrderAndRecord(true);
                    }
                }
            } else { // 未付费而且试用期已过
                showTip = true;
                warnString = Global.createColorHtml("您的试用期已结束，请订购后使用", fontRed);
                int suspand = account.suspendedToToday();
                balanceTipString = String.format("服务已暂停 %s 天", suspand);

                showOrderAndRecord(false);
            }
        }

        setHeader(showTip, warnString, balanceTitleString, balanceContentString, new SpannedString(balanceTipString));
    }

    private void showOrderAndRecord(boolean showOrder) {
        String[] titles;
        if (showOrder) {
            titles = new String[]{MyDetailPagerAdapter.TITLE_ORDER};
        } else {
            titles = new String[]{MyDetailPagerAdapter.TITLE_EMPTY};
        }

        MyDetailPagerAdapter adpter = new MyDetailPagerAdapter(this, getSupportFragmentManager(), titles);
        viewPager.setAdapter(adpter);

        if (titles.length >= 2) {
            WechatTab tabs = (WechatTab) getLayoutInflater().inflate(R.layout.common_pager_tabs, containerHeader, false);
            containerHeader.addView(tabs);
            tabs.setViewPager(viewPager);
            ViewCompat.setElevation(tabs, 0);

        } else if (titles.length == 1) {
            if (titles[0].equals(MyDetailPagerAdapter.TITLE_ORDER)) {
                View tabs = getLayoutInflater().inflate(R.layout.order_section, containerHeader, false);
                containerHeader.addView(tabs);
            }
        }

        ViewCompat.setElevation(findViewById(R.id.appbarLayout), 0);

        if (showOrder) {
            loadOrderData();
        }

    }

    private void setHeader(boolean show, Spanned... spanneds) {
        topTip.setVisibility(show ? View.VISIBLE : View.GONE);
        TextView[] views = new TextView[]{
                topWarn, balanceTitle, balanceContent, balanceTip
        };
        for (int i = 0; i < views.length; ++i) {
            if (spanneds[i].length() == 0) {
                views[i].setVisibility(View.GONE);
            } else {
                views[i].setVisibility(View.VISIBLE);
                views[i].setText(spanneds[i]);
            }
        }
    }

    @Click
    void closeTipButton() {
        topTip.setVisibility(View.GONE);
    }

    private void loadOrderData() {
        String url = String.format("%s/enterprise/%s/orders", Global.HOST_API, EnterpriseInfo.instance().getGlobalkey());
        getNetwork(url, TAG_ORDERS);
    }

    private void loadBillings() {
        String url = String.format("%s/enterprise/%s/billings", Global.HOST_API, EnterpriseInfo.instance().getGlobalkey());
        getNetwork(url, TAG_BILLINGS);
    }

    public ArrayList<Order> getOrderList() {
        return orderList;
    }

    public ArrayList<Billing> getBillingList() {
        return billingList;
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(TAG_ORDERS)) {
            if (code == 0) {
                orderList.clear();
                JSONArray json = respanse.optJSONArray("data");
                for (int i = 0; i < json.length(); ++i) {
                    orderList.add(new Order(json.optJSONObject(i)));
                }

                EventBus.getDefault().post(new EventRefresh(true));
            } else {

            }
        } else if (tag.equals(TAG_BILLINGS)) {
            if (code == 0) {
                billingList.clear();
                JSONArray json = respanse.optJSONArray("data");
                for (int i = 0; i < json.length(); ++i) {
                    billingList.add(new Billing(json.optJSONObject(i)));
                }
                EventBus.getDefault().post(new EventRefresh(true));
            } else {

            }

        } else if (tag.equals(TAG_INFO)) {
            swipeRefreshLayout.setRefreshing(false);
            if (code == 0) {
                account = new EnterpriseAccount(respanse.optJSONObject("data"));
                initHeader();
            } else {
                showButtomToast("刷新失败");
            }
        }
    }

    private static class MyDetailPagerAdapter extends FragmentPagerAdapter {

        private static final String TITLE_ORDER = "充值订单";
        private static final String TITLE_BILLING = "账单流水";
        private static final String TITLE_EMPTY = "TITLE_EMPTY";
        String[] titles;

        Context context;

        public MyDetailPagerAdapter(Context context, FragmentManager fm, String[] titles) {
            super(fm);
            this.context = context;
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (titles[position].equals(TITLE_EMPTY)) {
                    return EmptyFragment_.builder().build();
                } else {
                    return OrderListFragment_.builder().build();
                }
            } else {
                return BillingListFragment_.builder().build();
            }
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}

