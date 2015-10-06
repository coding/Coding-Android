package net.coding.program.maopao;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;
import net.coding.program.model.LocationObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Neutra on 2015/3/14.
 */
@EActivity(R.layout.activity_location_edit)
@OptionsMenu(R.menu.location_edit)
public class LocationEditActivity extends BackActivity {
    @ViewById
    EditText nameText, areaText, addressText;
    @Extra
    String name, city, area;
    @Extra
    double latitude, longitude;

    @AfterViews
    protected final void initLocationEditActivity() {
        nameText.setText(name);
        areaText.setText(area);
        addressText.requestFocus();
    }

    @OptionsItem
    void action_ok() {
        final String customName = nameText.getText().toString().trim();
        if (TextUtils.isEmpty(customName)) {
            Toast.makeText(this, "请输入位置名称", Toast.LENGTH_SHORT).show();
            nameText.requestFocus();
            return;
        }
        final String areaAddress = addressText.getText().toString().trim();
        final String customAddress = TextUtils.isEmpty(areaAddress) ? areaText.getText().toString().trim() : areaAddress;
        BaiduLbsLoader.store(getApplicationContext(), customName, customAddress, latitude, longitude, new BaiduLbsLoader.StorePoiListener() {
            @Override
            public void onStoreResult(boolean success, String id) {
                if (!success) {
                    Toast.makeText(LocationEditActivity.this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
                } else {
                    final LocationObject data = LocationObject.newCustom(id, customName, customAddress, latitude, longitude);
                    data.city = city;
                    Intent intent = new Intent();
                    intent.putExtra("location", data);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }
}
