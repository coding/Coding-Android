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
    public static void bind(TextView topView, TextView bottomView, final Maopao.MaopaoObject data) {
        if (TextUtils.isEmpty(data.location)) {
            topView.setVisibility(View.GONE);
            bottomView.setVisibility(View.GONE);
            topView.setOnClickListener(null);
            bottomView.setOnClickListener(null);
        } else {
            TextView displayView, hiddenView;
            if (TextUtils.isEmpty(data.device)) {
                hiddenView = topView;
                displayView = bottomView;
            } else {
                hiddenView = bottomView;
                displayView = topView;
            }
            displayView.setText(data.location);
            displayView.setVisibility(View.VISIBLE);
            hiddenView.setVisibility(View.GONE);
            final LocationCoord locationCoord = LocationCoord.parse(data.coord);
            if (data.coord != null) {
                displayView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LocationDetailActivity_.intent(v.getContext())
                                .name(data.location)
                                .address(locationCoord.address)
                                .latitude(locationCoord.latitude)
                                .longitude(locationCoord.longitude)
                                .start();
                    }
                });
            } else {
                // 位置解析失败时，使点击无效
                displayView.setOnClickListener(null);
            }
        }
    }
}
