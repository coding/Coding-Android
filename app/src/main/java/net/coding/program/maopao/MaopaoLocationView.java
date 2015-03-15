package net.coding.program.maopao;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import net.coding.program.maopao.item.LocationCoord;
import net.coding.program.model.Maopao;

import org.w3c.dom.Text;

/**
 * Created by Neutra on 2015/3/14.
 */
public class MaopaoLocationView extends TextView {
    public MaopaoLocationView(Context context) {
        super(context);
    }

    public MaopaoLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaopaoLocationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaopaoLocationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void bind(final Maopao.MaopaoObject data) {
        if (!TextUtils.isEmpty(data.location)) {
            this.setText(data.location);
            this.setVisibility(View.VISIBLE);
            final LocationCoord locationCoord = LocationCoord.parse(data.coord);
            if (data.coord != null) {
                this.setOnClickListener(new View.OnClickListener() {
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
                // 位置解析失败时，点击无效
                this.setOnClickListener(null);
            }
            return;
        }

        this.setOnClickListener(null);
        this.setVisibility(View.GONE);
    }

}
