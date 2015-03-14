package net.coding.program.maopao;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import net.coding.program.BaseActivity;
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
public class LocationEditActivity extends BaseActivity {
    @ViewById
    EditText nameText, areaText, addressText;
    @Extra
    String name, area, address;
    @Extra
    double latitude, longitude;

    @AfterViews
    void afterViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nameText.setText(name);
        areaText.setText(area);
        addressText.setText(address);
        addressText.requestFocus();
    }


    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @OptionsItem
    void action_ok() {
        String customName = nameText.getText().toString().trim();
        if (TextUtils.isEmpty(customName)) {
            Toast.makeText(this, "请输入位置名称", Toast.LENGTH_SHORT).show();
            nameText.requestFocus();
            return;
        }
        String customAddress = addressText.getText().toString().trim();
        if (TextUtils.isEmpty(customAddress)) customAddress = areaText.getText().toString().trim();
        LocationObject data = LocationObject.custom(customName, customAddress, latitude, longitude);
        BaiduLbsLoader.store(getApplicationContext(), customName, customAddress, latitude, longitude);
        Intent intent = new Intent();
        intent.putExtra("location", data);
        setResult(RESULT_OK, intent);
        finish();
    }
}
