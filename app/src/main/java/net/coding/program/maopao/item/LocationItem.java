package net.coding.program.maopao.item;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.model.LocationObject;

/**
 * Created by Neutra on 2015/3/11.
 */
public class LocationItem {
    private TextView primary, secondary;
    private View checkbox;

    private LocationItem() {
    }

    public static LocationItem from(View view) {
        Object tag = view.getTag();
        if (tag instanceof LocationItem) {
            return (LocationItem) tag;
        }
        LocationItem locationItem = new LocationItem();
        locationItem.primary = (TextView) view.findViewById(R.id.primary);
        locationItem.secondary = (TextView) view.findViewById(R.id.secondary);
        locationItem.checkbox = view.findViewById(R.id.checkbox);
        view.setTag(locationItem);
        return locationItem;
    }

    public void bind(LocationObject data, boolean checked) {
        primary.setText(data.name);
        secondary.setText(data.address);
        secondary.setVisibility(TextUtils.isEmpty(data.address) ? View.GONE : View.VISIBLE);
        checkbox.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
    }
}
