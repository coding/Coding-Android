package net.coding.program.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Neutra on 2015/3/11.
 */
public class LocationObject implements Serializable {
    public static enum Type {Undefined, City, Normal}

    public final Type type;
    public final String id, name, address;
    public double latitude, longitude;

    public LocationObject(JSONObject json) throws JSONException {
        type = Type.Normal;
        name = json.getString("name");
        address = json.has("address") ? json.getString("name") : "";
        id = json.optString("id");
    }

    public LocationObject(String id, String name, String address) {
        this(Type.Normal, id, name, address);
    }

    private LocationObject(Type type, String id, String name, String address) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public static LocationObject undefined() {
        return new LocationObject(Type.Undefined, null, "", null);
    }

    public static LocationObject city(String city) {
        return new LocationObject(Type.City, null, city, null);
    }
}
