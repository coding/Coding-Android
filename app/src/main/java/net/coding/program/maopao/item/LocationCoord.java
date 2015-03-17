package net.coding.program.maopao.item;

import android.text.TextUtils;
import android.util.Log;

import net.coding.program.model.LocationObject;

/**
 * Created by Neutra on 2015/3/14.
 */
public class LocationCoord {
    // 经纬度均使用百度经纬度坐标(bd09ll),
    public String address;
    public double latitude, longitude;
    public static final String FORMAT_VERSION = "1";

    public static LocationCoord from(LocationObject locationObject) {
        LocationCoord locationCoord = new LocationCoord();
        locationCoord.latitude = locationObject.latitude;
        locationCoord.longitude = locationObject.longitude;
        locationCoord.address = locationObject.address;
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
                    result.address = parts.length > 2 ? parts[2] : "不详";
                } catch (Throwable e) {
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
        return String.format("%.4f,%.4f", latitude, longitude);
//        String output = String.format("%f,%f", latitude, longitude);
//        return output;
    }
}