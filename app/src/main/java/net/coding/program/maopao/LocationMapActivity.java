package net.coding.program.maopao;

import android.text.TextUtils;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

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
@EActivity(R.layout.location_map)
public class LocationMapActivity extends BaseActivity {
    @ViewById
    MapView mapView;

    @Extra
    double latitude, longitude;
    @Extra
    String name, address;

    @AfterViews
    void afterViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LatLng position = new LatLng(latitude, longitude);
        BaiduMap map = mapView.getMap();
        map.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        map.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(position, 17));
        String label = TextUtils.isEmpty(address) ? name : (name + ":" + address);
        map.addOverlay(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_active))
                .title(label)
                .draggable(false));
    }

    @OptionsItem(android.R.id.home)
    void close() {
        onBackPressed();
    }
}
