package net.coding.program.maopao;

import android.view.View;
import android.widget.TextView;

import net.coding.program.BaseActivity;
import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
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
    @ViewById
    View map_button;
    @Extra
    double latitude, longitude;
    @Extra
    String name, address, shareId;

    @AfterViews
    void afterViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nameText.setText(name);
        addressText.setText(address);
        if (address != null) {
            map_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocationMapActivity_.intent(LocationDetailActivity.this)
                            .latitude(latitude).longitude(longitude)
                            .name(name).address(address).start();
                }
            });
        }
//        BaiduLbsLoader.requestLocationObject(this, shareId, new BaiduLbsLoader.LocationDetailListner(){
//            @Override
//            public void onReceiveLocationObject(LocationObject locationObject) {
//                if(LocationDetailActivity.this.isFinishing()) return;
//                if(locationObject == null) return;
//                addressText.setText(address= locationObject.address);
//                latitude = locationObject.latitude;
//                longitude = locationObject.longitude;
//            }
//        });
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }
}
