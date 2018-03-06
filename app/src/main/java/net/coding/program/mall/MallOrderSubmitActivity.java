package net.coding.program.mall;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.base.MyJsonResponse;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.model.MallItemObject;
import net.coding.program.common.model.MallOrderObject;
import net.coding.program.common.model.UserObject;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * Created by libo on 2015/11/22.
 */

@EActivity(R.layout.activity_mall_order_submit)
public class MallOrderSubmitActivity extends BackActivity {

    private static final int RESULT_LOCAL = 1;
    @Extra
    static MallItemObject mallItemObject = new MallItemObject();
    @ViewById
    EditText mall_order_edit_username;
    @ViewById
    EditText mall_order_edit_address;
    @ViewById
    EditText mall_order_edit_phone;
    @ViewById
    EditText mall_order_edit_note;
    @ViewById
    ImageView mall_order_img;
    @ViewById
    TextView mall_order_title;
    @ViewById
    TextView mall_order_point;
    @ViewById
    TextView mall_order_desc;
    @ViewById
    TextView mall_order_edit_city;
    @ViewById
    View mall_order_title_options, mall_order_divide_options;
    @ViewById
    TextView mall_order_edit_options, mallOrderRmb;

    @ViewById(R.id.discountLayout)
    View discountLayout;
    @ViewById(R.id.discountDivide)
    View discountDivide;
    @ViewById(R.id.discountTextView)
    TextView discountTextView;
    @ViewById(R.id.paymentAmount)
    TextView paymentAmountTextView;
    @ViewById(R.id.switchPointDiscount)
    SwitchCompat switchDiscount;

    BigDecimal paymentAmout = BigDecimal.ZERO;
    BigDecimal pointDiscount = BigDecimal.ZERO;

    @Click(R.id.switchPointDiscount)
    void clickPointDiscount(SwitchCompat button) {
        bindUIPay(button.isChecked());
    }

    private void bindUIPay(boolean discount) {
        BigDecimal mabiPrice = mallItemObject.points_cost;
        BigDecimal userMabi = GlobalData.sUserObject.points_left;

        if (userMabi.compareTo(BigDecimal.ZERO) == 0) {
            discountLayout.setVisibility(View.GONE);
            discountDivide.setVisibility(View.GONE);
        }

        if (discount) {
            if (userMabi.compareTo(mabiPrice) >= 0) {
                paymentAmout = BigDecimal.ZERO;
                pointDiscount = mabiPrice;
            } else {
                paymentAmout = mabiPrice.subtract(userMabi).multiply(new BigDecimal(50));
                pointDiscount = userMabi;
            }

            discountTextView.setText(String.format("可用 %.2f 码币抵扣 %.2f 元", pointDiscount,
                    pointDiscount.multiply(new BigDecimal(50))));
            paymentAmountTextView.setText(String.format("¥%.2f", paymentAmout));

        } else {
            paymentAmout = mabiPrice.multiply(new BigDecimal(50));
            pointDiscount = BigDecimal.ZERO;

            BigDecimal pointCanDiscount;
            if (userMabi.compareTo(mabiPrice) >= 0) {
                pointCanDiscount = mabiPrice;
            } else {
                pointCanDiscount = userMabi;
            }
            discountTextView.setText(String.format("可用 %.2f 码币抵扣 %.2f 元", pointCanDiscount,
                    pointCanDiscount.multiply(new BigDecimal(50))));
            paymentAmountTextView.setText(String.format("¥%.2f", paymentAmout));
        }
    }

    MallItemObject.Option option;
    String submitPassword = "";
    Local local = new Local("", 0, "", 0, "", 0);
    private View.OnFocusChangeListener foucusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Global.popSoftkeyboard(MallOrderSubmitActivity.this, v, hasFocus);
        }
    };

    @Click
    void mall_order_button_submit() {
        parseParams();
    }

    @AfterViews
    void initView() {
        UserObject user = AccountInfo.loadAccount(this);
        String userName = user.name;
        mall_order_edit_username.setHint(userName);

        mall_order_title.setText(mallItemObject.getName());
        mall_order_point.setText(mallItemObject.getShowPoints());
        mall_order_desc.setText(mallItemObject.getDescription().replaceAll(" ?<br> ?", ""));
        mallOrderRmb.setText(GlobalCommon.mbToRmb(mallItemObject.points_cost));
        getImageLoad().loadImage(mall_order_img, mallItemObject.getImage(), ImageLoadTool.mallOptions);

        mall_order_edit_username.setOnFocusChangeListener(foucusChangeListener);
        mall_order_edit_address.setOnFocusChangeListener(foucusChangeListener);
        mall_order_edit_phone.setOnFocusChangeListener(foucusChangeListener);
        mall_order_edit_note.setOnFocusChangeListener(foucusChangeListener);

        if (mallItemObject.getOptions().isEmpty()) {
            mall_order_title_options.setVisibility(View.GONE);
            mall_order_edit_options.setVisibility(View.GONE);
            mall_order_divide_options.setVisibility(View.GONE);
        } else {
            option = mallItemObject.getOptions().get(0);
            uiBindData();
        }

        switchDiscount.performClick();
    }

    @Click
    void mall_order_edit_options() {
        ArrayList<MallItemObject.Option> options = mallItemObject.getOptions();
        String[] optionNames = new String[options.size()];
        for (int i = 0; i < options.size(); ++i) {
            optionNames[i] = options.get(i).getName();
        }

        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setItems(optionNames, (dialog, which) -> {
                    option = options.get(which);
                    uiBindData();
                })
                .show();
    }

    private void uiBindData() {
        mall_order_edit_options.setText(option.getName());
    }

    void showDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View entryView = inflater.inflate(R.layout.dialog_mall_order_submit, null);
        final EditText editText = (EditText) entryView.findViewById(R.id.edit_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        builder.setView(entryView)
                .setTitle("确认订单")
                .setPositiveButton("确认", (dialog, which) -> {
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @OnActivityResult(RESULT_LOCAL)
    void onResultLocal(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            local = (Local) data.getSerializableExtra("result");
            mall_order_edit_city.setText(local.getLocalString());
        }
    }

    private void parseParams() {
        if (mall_order_edit_username.getText().toString().trim().length() == 0) {
            showMiddleToast("姓名不能为空");
            return;
        }
        if (mall_order_edit_address.getText().toString().trim().length() == 0) {
            showMiddleToast("街道地址不能为空");
            return;
        }

        if (mall_order_edit_city.getText().length() == 0) {
            showMiddleToast("省，市，县不能为空");
            return;
        }

        int phoneLength = mall_order_edit_phone.getText().toString().trim().length();
        if (phoneLength < 8 || phoneLength > 16) {
            showMiddleToast("电话号码格式不正确");
            return;
        }

        postSubmit();
    }

    private void postSubmit() {
        String userName = mall_order_edit_username.getText().toString().trim();
        String address = mall_order_edit_address.getText().toString().trim();
        String phone = mall_order_edit_phone.getText().toString().trim();
        String note = mall_order_edit_note.getText().toString().trim();

        RequestParams params = new RequestParams();
//        params.put("password", password);
//        params.put("pay_method", PaymentActivity.payMethod);
//        params.put("payment_amount", paymentAmout);
        params.put("point_discount", pointDiscount);

        params.put("receiverName", userName);
        params.put("receiverPhone", phone);
        params.put("remark", note);
        params.put("receiverAddress", address);
        params.put("giftId", mallItemObject.getId());
        if (option != null) {
            params.put("option_id", option.getId());
        }

        int priovicen = local.getProvicen().getId();
        if (priovicen != 0) {
            params.put("province", priovicen);

            int city = local.getCity().getId();
            if (city != 0) {
                params.put("city", city);

                int district = local.getDistrict().getId();
                if (district != 0) {
                    params.put("district", district);
                }
            }
        }

        showProgressBar(true, "正在提交订单...");

        String url = Global.HOST_API + "/gifts/orders";
        MyAsyncHttpClient.post(this, url, params, new MyJsonResponse(MallOrderSubmitActivity.this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                MallOrderObject item = new MallOrderObject(response.optJSONObject("data"));
                if (item.getStatus() == MallOrderObject.STATUS_NO_PAY) {
                    MallOrderDetailActivity_.intent(MallOrderSubmitActivity.this).start();
                    PaymentActivity_.intent(MallOrderSubmitActivity.this).order(item).start();
                    finish();
                } else {
                    showButtomToast("恭喜您，订单提交成功！");
                    MallOrderDetailActivity_.intent(MallOrderSubmitActivity.this).start();
                    finish();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                showProgressBar(false);
            }
        });
    }

    @Click
    void mall_order_edit_city() {
        AsyncHttpClient client = MyAsyncHttpClient.createClient(MallOrderSubmitActivity.this);
        String host = Global.HOST_API + "/region?parent=0&level=1";
        client.get(host, new MyJsonResponse(MallOrderSubmitActivity.this) {
            @Override
            public void onMySuccess(JSONObject response) {
                super.onMySuccess(response);
                ArrayList<City> citys = PickLocalActivity.citysFromJson(response);

                Intent intent = new Intent(MallOrderSubmitActivity.this, PickLocalActivity_.class);
                intent.putExtra(PickLocalActivity.EXTRA_LOCAL_POS, 1);
                intent.putExtra(PickLocalActivity.EXTRA_LIST_DATA, citys);
                intent.putExtra(PickLocalActivity.EXTRA_LOCAL, local);
                startActivityForResult(intent, RESULT_LOCAL);

                showProgressBar(false, "");
            }

            @Override
            public void onMyFailure(JSONObject response) {
                super.onMyFailure(response);
                showProgressBar(false, "");
            }
        });

        showProgressBar(true, "");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                Global.popSoftkeyboard(MallOrderSubmitActivity.this, v, false);
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    static public class City implements Serializable {
        int id;
        String name;

        public City(JSONObject json) {
            id = json.optInt("id");
            name = json.optString("name");
        }

        public City(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void clear() {
            id = 0;
            name = "";
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof City)) return false;

            City city = (City) o;

            if (id != city.id) return false;
            return name.equals(city.name);

        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    static public class Local implements Serializable {
        City provicen;
        City city;
        City district;

        public Local(String provicen, int proviceId, String city, int cityId, String district, int districtId) {
            this.provicen = new City(proviceId, provicen);
            this.city = new City(cityId, city);
            this.district = new City(districtId, district);
        }

        public String getLocalString() {
            return String.format("%s %s %s", provicen.getName(), city.getName(), district.getName());
        }

        public City getProvicen() {
            return provicen;
        }

        public City getCity() {
            return city;
        }

        public City getDistrict() {
            return district;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Local)) return false;

            Local local = (Local) o;

            if (!provicen.equals(local.provicen)) return false;
            if (!city.equals(local.city)) return false;
            return district.equals(local.district);

        }

        @Override
        public int hashCode() {
            int result = provicen.hashCode();
            result = 31 * result + city.hashCode();
            result = 31 * result + district.hashCode();
            return result;
        }
    }

}
