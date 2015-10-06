package net.coding.program.maopao;

import android.view.View;
import android.widget.TextView;

import net.coding.program.common.ui.BackActivity;
import net.coding.program.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Neutra on 2015/3/14.
 */
@EActivity(R.layout.activity_location_detail)
public class LocationDetailActivity extends BackActivity {

    @ViewById
    TextView nameText, addressText;

    @ViewById
    View map_button, customText;

    @Extra
    double latitude, longitude;

    @Extra
    String name, address;

    @Extra
    boolean isCustom;

    @AfterViews
    protected final void initLocationDetailActivity() {
        if (name == null) name = "";
        name = name.replaceFirst(".*" + MaopaoLocationArea.MAOPAO_LOCATION_DIVIDE, "");
        nameText.setText(name);
        addressText.setText(address);
        if (address == null || address.isEmpty()) {
            addressText.setText("未填写详细的地址");
        }

        customText.setVisibility(isCustom ? View.VISIBLE : View.GONE);
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
    }
}
