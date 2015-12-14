package net.coding.program.mall;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.ui.BaseAppCompatActivity;
import net.coding.program.model.AccountInfo;
import net.coding.program.model.UserObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by libo on 2015/11/22.
 */

@EActivity(R.layout.activity_mall_order_submit)
public class MallOrderSubmitActivity extends BaseAppCompatActivity {

    final String host = Global.HOST_API + "/gifts/exchange";

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

    @Click
    void mall_order_button_submit() {
        parseParams();
    }

    @Extra
    int giftId;

    @Extra
    String desc;

    @Extra
    String title;

    @Extra
    double point;

    @Extra
    String imgUrl;

    String submitPassword = "";

    private View.OnFocusChangeListener foucusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Global.popSoftkeyboard(MallOrderSubmitActivity.this, v, hasFocus);
        }
    };

    @AfterViews
    void initView() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        UserObject user = AccountInfo.loadAccount(this);
        String userName = user.name;
        mall_order_edit_username.setHint(userName);

        mall_order_title.setText(title);
        mall_order_point.setText(point + " 码币");
        mall_order_desc.setText(desc.replaceAll(" ?<br> ?", ""));
        getImageLoad().loadImage(mall_order_img, imgUrl, ImageLoadTool.mallOptions);

        mall_order_edit_username.setOnFocusChangeListener(foucusChangeListener);
        mall_order_edit_address.setOnFocusChangeListener(foucusChangeListener);
        mall_order_edit_phone.setOnFocusChangeListener(foucusChangeListener);
        mall_order_edit_note.setOnFocusChangeListener(foucusChangeListener);
    }

    void showDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View entryView = inflater.inflate(R.layout.dialog_mall_order_submit, null);
        final EditText editText = (EditText) entryView.findViewById(R.id.edit_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(entryView)
                .setTitle("确认订单")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        parsePassword(editText.getText().toString().trim());
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void parseParams() {
        if (mall_order_edit_username.getText().toString().trim().length() == 0) {
            showMiddleToast("姓名不能为空");
            return;
        }
        if (mall_order_edit_address.getText().toString().trim().length() == 0) {
            showMiddleToast("地址不能为空");
            return;
        }
        int phoneLength = mall_order_edit_phone.getText().toString().trim().length();
        if (phoneLength < 8 || phoneLength > 16) {
            showMiddleToast("电话号码格式不正确");
            return;
        }
        showDialog();
    }

    private void parsePassword(String submitPassword) {
        if (submitPassword.length() < 6 || submitPassword.length() > 64 || TextUtils
                .isEmpty(submitPassword)) {
            showMiddleToast("密码格式有误，请重新输入");
            return;
        }
        this.submitPassword = submitPassword;

        postSubmit();

    }

    private void postSubmit() {
        String password = SimpleSHA1.sha1(submitPassword);
        String userName = mall_order_edit_username.getText().toString().trim();
        String address = mall_order_edit_address.getText().toString().trim();
        String phone = mall_order_edit_phone.getText().toString().trim();
        String note = mall_order_edit_note.getText().toString().trim();

        RequestParams params = new RequestParams();
        params.put("password", password);
        params.put("receiverName", userName);
        params.put("receiverPhone", phone);
        params.put("remark", note);
        params.put("receiverAddress", address);
        params.put("giftId", giftId);
        showProgressBar(true, "正在提交订单...");
        postNetwork(host, params, host);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data)
            throws JSONException {
        showProgressBar(false);
        if (tag.equals(host)) {
            if (code == 0) {
                showButtomToast("恭喜您，订单提交成功！");
                MallOrderDetailActivity_.intent(this).start();
                finish();
            } else {
                showButtomToast("很抱歉，订单提交失败！");
            }
        }
    }

    @OptionsItem(android.R.id.home)
    protected final void annotaionClose() {
        onBackPressed();
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
}
