package net.coding.program.setting.order;

import android.text.Spanned;
import android.text.SpannedString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.model.EnterpriseAccount;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.text.SimpleDateFormat;

@EActivity(R.layout.activity_order_main)
public class OrderMainActivity extends BackActivity {

    @Extra
    EnterpriseAccount account;

    @ViewById
    TextView topWarn, balanceTitle, balanceContent, balanceTip;

    @ViewById
    View topTip;

    @ViewById
    ViewGroup container;

    @ColorRes(R.color.font_orange)
    int fontOragne;

    @ColorRes(R.color.font_red)
    int fontRed;

    @AfterViews
    void initOrderMainActivity() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy 年 MM月 dd日");
        String timeString = df.format(Long.valueOf(account.estimateDate));
        SpannedString empty = new SpannedString("");

        boolean showTip = false;
        Spanned warnString = empty;
        Spanned balanceTitleString = empty;
        Spanned balanceContentString = empty;
        String balanceTipString = "";

        if (account.trial) { // 处于试用期
            if (account.remaindays > 5) {
                warnString = Global.createColorHtml("您正在试用 Coding 企业版，试用期剩余 ", String.valueOf(account.remaindays), " 天", fontOragne);
            } else {
                String content = String.format("您正在试用 Coding 企业版，试用期剩余 %s 天", account.remaindays);
                warnString = Global.createColorHtml(content, fontOragne);
            }
            String tipString = String.format("试用期至 %s，剩余 %s 天", timeString, account.remaindays);

        } else {
            if (account.payed) {
                if (account.remaindays > 0) {
                    showTip = false;
                    if (account.remaindays > 5) { // 付费期且未到期

                    } else {
                        warnString = Global.createColorHtml("您的账户余额不足，请尽快订购", fontRed);
                    }

                    balanceTitleString = new SpannedString("账户余额（元）");
                    balanceContentString = Global.createColorHtml(account.balance, fontOragne);
                    balanceTipString = String.format("余额预计可使用至 %s，剩余 %s 天", df.format(account.estimateDate), account.remaindays);

                } else { // 付费期已到期
                    showTip = true;
                    warnString = Global.createColorHtml("您的服务已过期，请订购后使用", fontRed);
                    balanceTitleString = new SpannedString("已透支金额（元）");
                    balanceContentString = Global.createColorHtml(account.balance, fontRed);

                    if (account.suspendedAt > 0) { // 已暂停
                        balanceTipString = String.format("服务已暂停 %s 天", account.suspendedToToday());
                    } else { // 处于超时使用阶段
                        balanceTipString = String.format("过期时间为 %s，已超时使用 %s 天", df.format(account.estimateDate), account.estimateDate());
                    }
                }
            } else { // 未付费而且试用期已过
                showTip = true;
                warnString = Global.createColorHtml("您的试用期已结束，请订购后使用", fontRed);
                int suspand = account.suspendedToToday();
                balanceTipString = String.format("服务已暂停 %s 天", suspand);
            }
        }

        setHeader(showTip, warnString, balanceTitleString, balanceContentString, new SpannedString(balanceTipString));

        if (!account.payed) {
            getLayoutInflater().inflate(R.layout.order_empty, container, true);
        } else {
            // add list
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
}

