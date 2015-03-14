package net.coding.program.maopao;

import android.widget.TextView;

import net.coding.program.BaseActivity;
import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Neutra on 2015/3/14.
 */
@EActivity(R.layout.activity_location_detail)
public class LocationDetailActivity extends BaseActivity {
    @ViewById
    TextView nameText, addressText;
    @Extra
    double latitude, longitude;
    @Extra
    String name, address;

    @AfterViews
    void afterViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nameText.setText(name);
        addressText.setText(address);
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }

    @Click
    void map_button() {
        LocationMapActivity_.intent(this)
                .latitude(latitude).longitude(longitude)
                .name(name).address(address).start();
    }
}
