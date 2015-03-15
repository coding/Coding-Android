package net.coding.program.maopao.item;

import android.text.TextUtils;
import android.util.Log;

import net.coding.program.model.LocationObject;

/**
 * Created by Neutra on 2015/3/14.
 */
public class LocationCoord {
    // coord格式: format("%f,%f,%s,%s", latitude, longitude, FORMAT_VERSION, address);
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
            Log.e("coord", coord);
            String[] parts = coord.split(",", 4);
            if (parts != null && parts.length == 4 && FORMAT_VERSION.equals(parts[2])) {
                LocationCoord result = new LocationCoord();
                try {
                    result.latitude = Double.parseDouble(parts[0]);
                    result.longitude = Double.parseDouble(parts[1]);
                    result.address = parts[3];
                } catch (Throwable e) {
                    Log.e("LocationCoord", "invalid LocationCoord format: coord", e);
                    return null;
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%f,%f,%s,%s", latitude, longitude, FORMAT_VERSION, address);
    }
}