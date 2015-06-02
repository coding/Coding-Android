package net.coding.program.maopao;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import net.coding.program.maopao.item.LocationCoord;
import net.coding.program.model.Maopao;

/**
 * Created by Neutra on 2015/3/14.
 */
public class MaopaoLocationArea {

    public static final String MAOPAO_LOCATION_DIVIDE = " · ";

    public static void bind(TextView locationView, final Maopao.MaopaoObject data) {
        if (TextUtils.isEmpty(data.location)) {
            locationView.setVisibility(View.GONE);
            locationView.setOnClickListener(null);
        } else {
            locationView.setText(data.location);
            locationView.setVisibility(View.VISIBLE);
            final LocationCoord locationCoord = LocationCoord.parse(data.coord);
            if (locationCoord != null) {
                locationView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (data == null || data.location == null || locationCoord == null) return;
                        // 根据是否存在特殊字符来判断一个位置是否只是城市
                        if (data.location.indexOf(MAOPAO_LOCATION_DIVIDE) >= 0) {
                            LocationDetailActivity_.intent(v.getContext())
                                    .name(data.location)
                                    .address(data.address)
                                    .latitude(locationCoord.latitude)
                                    .longitude(locationCoord.longitude)
                                    .isCustom(locationCoord.isCustom)
                                    .start();
                        } else {
                            // 城市直接进入地图而不经过详情页(因为无详细地址)
                            LocationMapActivity_.intent(v.getContext())
                                    .name(data.location)
                                    .address(data.address)
                                    .latitude(locationCoord.latitude)
                                    .longitude(locationCoord.longitude)
                                    .start();
                        }
                    }
                });
            } else {
                // 位置解析失败时，使点击无效
                locationView.setOnClickListener(null);
            }
        }
    }
}
