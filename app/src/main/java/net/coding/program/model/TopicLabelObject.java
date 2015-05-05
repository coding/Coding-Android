package net.coding.program.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Neutra on 2015/4/23.
 */
public class TopicLabelObject implements Parcelable, Serializable {
    public int id;
    public int count;
    public String name;

    public TopicLabelObject(JSONObject json) throws JSONException {
        id = json.optInt("id");
        name = json.optString("name", "");
        count = json.optInt("count", 0);
    }

    public TopicLabelObject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    private TopicLabelObject() {
    }

    public TopicLabelObject(TopicLabelObject src) {
        id = src.id;
        name = src.name;
        count = src.count;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(count);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<TopicLabelObject> CREATOR = new Creator() {
        @Override
        public TopicLabelObject createFromParcel(Parcel source) {
            TopicLabelObject dest = new TopicLabelObject();
            dest.id = source.readInt();
            dest.count = source.readInt();
            dest.name = source.readString();
            return dest;
        }

        @Override
        public TopicLabelObject[] newArray(int size) {
            return new TopicLabelObject[size];
        }
    };
}
