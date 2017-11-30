package net.coding.program.mall;

import com.alipay.sdk.app.PayTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.coding.program.AllThirdKeys;
import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.MallOrderObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;
import net.coding.program.network.model.HttpResult;
import net.coding.program.network.model.point.OrderObject;

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
 *
 */

@EActivity(R.layout.activity_payment_coding)
public class PaymentActivity extends BackActivity {

    private static final String TYPE_ZHIFUBAO = "Alipay";
    private static final String TYPE_WEIXIN = "Weixin";

    @Extra("order")
    MallOrderObject order;

    @AfterViews
    void initPaymentActivity() {
    }

    @Click(R.id.zhifubao)
    void clickZhifubao() {
        createPayOrder(order, TYPE_ZHIFUBAO);
    }

    @Click(R.id.weixin)
    void clickWeixin() {
        createPayOrder(order, TYPE_WEIXIN);
    }

    private void createPayOrder(MallOrderObject data, String payMethod) {
        String url = Global.HOST_API + "/gifts/pay/" + data.getOrderNo();
        RequestParams params = new RequestParams();
        params.put("pay_method", payMethod);
        MyAsyncHttpClient.post(this, url, params, new MyJsonResponse(this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);

                HttpResult<OrderObject> order = new Gson().fromJson(response.toString(), new TypeToken<HttpResult<OrderObject>>() {
                }.getType());
                payOrder(order.data.url);

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

    private void payOrder(String url) {
        payByClient(url);
    }
//
//    private void paybyWeixinClient(WeixinRecharge recharge) {
//        IWXAPI api = WXAPIFactory.createWXAPI(this, AllThirdKeys.WX_APP_ID, false);
//        PayReq request = new PayReq();
//
//        WeixinAppResult weixinAppResult = recharge.weixinAppResult;
//        request.packageValue = weixinAppResult._package;
//        request.appId = weixinAppResult.appId;
//        request.sign = weixinAppResult.sign;
//        request.partnerId = weixinAppResult.partnerId;
//        request.prepayId = weixinAppResult.prepayId;
//        request.nonceStr = weixinAppResult.nonceStr;
//        request.timeStamp = weixinAppResult.timestamp;
//        api.sendReq(request);
//
//        // 微信在5.0或以下的手机上不发通知, 干脆在这里关掉
//        mpayLayout.postDelayed(() -> showSending(false), 2 * 1000);
//    }


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
        EventBus.getDefault().post(new EventCheckResult());
        finish();
    }
}
