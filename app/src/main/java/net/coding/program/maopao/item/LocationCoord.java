package net.coding.program.maopao.item;

import android.text.TextUtils;
import android.util.Log;

import net.coding.program.model.LocationObject;

/**
 * Created by Neutra on 2015/3/14.
 */
public class LocationCoord {
    // 经纬度均使用百度经纬度坐标(bd09ll),
    public double latitude, longitude;
    public boolean isCustom = false;

    public static LocationCoord from(LocationObject locationObject) {
        LocationCoord locationCoord = new LocationCoord();
        locationCoord.latitude = locationObject.latitude;
        locationCoord.longitude = locationObject.longitude;
        locationCoord.isCustom = locationObject.type == LocationObject.Type.newCustom;
        return locationCoord;
    }

    public static LocationCoord parse(String coord) {
        if (!TextUtils.isEmpty(coord)) {
            String[] parts = coord.split(",", 3);
            if (parts != null && parts.length >= 2) {
                LocationCoord result = new LocationCoord();
                try {
                    result.latitude = Double.parseDouble(parts[0]);
                    result.longitude = Double.parseDouble(parts[1]);
                    result.isCustom = parts.length > 2 && !("0".equals(parts[2].trim()));
                } catch (Exception e) {
                    Log.e("LocationCoord", "invalid coord format", e);
                    return null;
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%f,%f,%d", latitude, longitude, (isCustom ? 1 : 0));
    }
}