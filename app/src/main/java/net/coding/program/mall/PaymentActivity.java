package net.coding.program.mall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.alipay.sdk.app.PayTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.event.EventUpdateOrderList;
import net.coding.program.common.model.MallOrderObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.point.OrderObject;
import net.coding.program.network.model.point.WeixinOrder;
import net.coding.program.pay.PayBroadcast;
import net.coding.program.pay.PayKeys;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by chenchao on 2017/11/29.
 */

@EActivity(R.layout.activity_payment_coding)
public class PaymentActivity extends BackActivity {

    private enum Type {
        Alipay, Weixin
    }

    @Extra("order")
    MallOrderObject order;

    @AfterViews
    void initPaymentActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(PayBroadcast.WEIXIN_PAY_INTENT);
        registerReceiver(weixinPayReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(weixinPayReceiver);
    }

    @Click(R.id.zhifubao)
    void clickZhifubao() {
        createPayOrder(order, Type.Alipay);
    }

    @Click(R.id.weixin)
    void clickWeixin() {
        createPayOrder(order, Type.Weixin);
    }

    private void createPayOrder(MallOrderObject data, Type payMethod) {
        String url = Global.HOST_API + "/gifts/pay/" + data.getOrderNo();
        RequestParams params = new RequestParams();
        params.put("pay_method", payMethod.name());
        MyAsyncHttpClient.post(this, url, params, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);

                String jsonString = response.toString();
                Logger.d("lllllllll " + jsonString);

                if (payMethod == Type.Alipay) {
                    HttpResult<OrderObject> order = new Gson().fromJson(jsonString,
                            new TypeToken<HttpResult<OrderObject>>() {
                            }.getType());
                    payByClient(order.data.url);
                } else {
                    HttpResult<WeixinOrder> order = new Gson().fromJson(jsonString,
                            new TypeToken<HttpResult<WeixinOrder>>() {
                            }.getType());
                    paybyWeixinClient(order.data);
                }


                showProgressBar(false);
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                showProgressBar(false);
            }
        });

        showProgressBar(true);
    }

    private void paybyWeixinClient(WeixinOrder weixinAppResult) {
        IWXAPI api = WXAPIFactory.createWXAPI(this, PayKeys.WX_APP_ID, false);
        PayReq request = new PayReq();

//        WeixinAppResult weixinAppResult = recharge.weixinAppResult;
        request.packageValue = weixinAppResult._package;
        request.appId = weixinAppResult.appId;
        request.sign = weixinAppResult.sign;
        request.partnerId = weixinAppResult.partnerId;
        request.prepayId = weixinAppResult.prepayId;
        request.nonceStr = weixinAppResult.nonceStr;
        request.timeStamp = weixinAppResult.timestamp;
        api.sendReq(request);

        // 微信在5.0或以下的手机上不发通知, 干脆在这里关掉
        closeProgressbar();
    }

    @UiThread(delay = 2000)
    void closeProgressbar() {
        showProgressBar(false);
    }

    @Background
    void payByClient(String payInfo) {
        // 构造PayTask 对象
        PayTask alipay = new PayTask(this);
        // 调用支付接口，获取支付结果
        Map<String, String> result = alipay.payV2(payInfo, false);
        Gson gson = new Gson();
        Logger.d(gson.toJson(result));

        checkPayResult();
    }

    @UiThread
    void checkPayResult() {
        EventBus.getDefault().post(new EventUpdateOrderList());
        finish();
    }


    BroadcastReceiver weixinPayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PaymentActivity.this.isFinishing()) {
                return;
            }

            int result = intent.getIntExtra(PayBroadcast.WEIXIN_PAY_PARAM, 1000);
            if (result == BaseResp.ErrCode.ERR_OK) {
                checkPayResult();
            }
        }
    };
}
