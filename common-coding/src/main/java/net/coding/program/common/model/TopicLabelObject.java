package net.coding.program.common.model;

import android.graphics.Color;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.coding.program.common.CodingColor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Neutra on 2015/4/23.
 * 标签的数据结构
 */
public class TopicLabelObject implements Serializable {

    private static final long serialVersionUID = 3388609672024998882L;

    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("count")
    @Expose
    public int count;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("color")
    @Expose
    public String colorString = "";

    private transient int color;

    public TopicLabelObject(JSONObject json) throws JSONException {
        id = json.optInt("id");
        name = json.optString("name", "");
        count = json.optInt("count", 0);
        colorString = json.optString("color", "");
        try {
            color = Color.parseColor(colorString);
        } catch (Exception e) {
            color = CodingColor.font1;
        }
    }

    public TopicLabelObject(int id, String name, int color, String colorString) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.colorString = colorString;
    }

    public TopicLabelObject(TopicLabelObject src) {
        id = src.id;
        name = src.name;
        count = src.count;
        color = src.getColorValue();
        colorString = src.colorString;
    }

    public int getColorValue() {
        if (color == 0 && !TextUtils.isEmpty(colorString)) {
            try {
                color = Color.parseColor(colorString);
            } catch (Exception e) {
                color = CodingColor.font1;
            }
        }
        return color;
    }

    public void setColorValue(int color) {
        this.color = color;
    }

    public void setColorValue(String colorString) {
        try {
            color = Color.parseColor(colorString);
        } catch (Exception e) {
            color = CodingColor.font1;
        }
    }

    public boolean isEmpty() {
        return name == null || name.isEmpty();
    }
}
