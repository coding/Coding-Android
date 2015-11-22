package net.coding.program.mall;

import net.coding.program.R;
import net.coding.program.common.ui.BackActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by libo on 2015/11/22.
 */

@EActivity(R.layout.activity_mall_order_submit)
public class MallOrderSubmitActivity extends BackActivity {

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


    @Extra
    String title;

    @Extra
    double point;

    @Extra
    String imgUrl;

    @AfterViews
    void initView(){
        mall_order_title.setText(title);
        mall_order_point.setText(point+ " 码币");
        getImageLoad().loadImage(mall_order_img,imgUrl);
    }

}
