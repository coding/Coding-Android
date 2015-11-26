package net.coding.program.mall;

import com.loopj.android.http.RequestParams;

import net.coding.program.R;
import net.coding.program.common.Global;
import net.coding.program.common.SimpleSHA1;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import me.drakeet.materialdialog.MaterialDialog;


/**
 * Created by libo on 2015/11/22.
 */

@EActivity(R.layout.activity_mall_order_submit)
public class MallOrderSubmitActivity extends BackActivity {

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
        showDialog();
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

    @AfterViews
    void initView() {
        mall_order_title.setText(title);
        mall_order_point.setText(point + " 码币");
        mall_order_desc.setText(desc);
        getImageLoad().loadImage(mall_order_img, imgUrl);
    }

    void showDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View entryView = inflater.inflate(R.layout.dialog_mall_order_submit, null);
        final EditText editText = (EditText) entryView.findViewById(R.id.edit_text);

        final MaterialDialog dialog = new MaterialDialog(this);
        dialog.setView(entryView)
                .setPositiveButton("确认",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                parsePassword(editText.getText().toString().trim());
                            }
                        })

                .setNegativeButton("取消",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
        dialog.setTitle(getResources().getString(R.string.mall_exchange_title));
        dialog.show();
    }

    private void parsePassword(String submitPassword) {
        if (submitPassword.length() < 6 || submitPassword.length() > 64 || TextUtils
                .isEmpty(submitPassword)) {
            showMiddleToast("密码格式有误，请重新输入");
        }
        postSubmit();

    }

    private void postSubmit() {
        //todo lll fix params
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
        params.put("receiverAddress",address);
        params.put("giftId",giftId);
        putNetwork(host, params, host);
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data)
            throws JSONException {
        if(tag.equals(host)){
            if (code == 0 ){
                showButtomToast("恭喜您，订单提交成功！");
            }else {
                showButtomToast("很抱歉，订单提交失败！");
            }
        }
    }
}
