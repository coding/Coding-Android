package net.coding.program.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by libo on 2015/11/20.
 */
public class MallItemObject implements Serializable {

    /**
     * "id": 1,
     * "name": "萌萌哒洋葱猴抱枕 ¥99",
     * "image": "https://dn-coding-net-production-pp.qbox.me/b888b183-9af6-45d3-b964-74a2dcfeea78.png",
     * "description": "超柔短毛绒表面/优质三维pp棉 <br/>号称东半球最大IT抱枕商的经典吉祥物<br/> 情人节告白卖萌首选<br/>通宵加班必备暖心之物",
     * "points_cost": 2,
     * "updated_at": 1444300656000
     */
    private int id;
    private String name = "";
    private String image = "";
    private String description = "";
    private double points_cost;
    private long updated_at;
    private ArrayList<Option> options = new ArrayList<>();

//    protected MallItemObject(Parcel in) {
//        id = in.readInt();
//        name = in.readString();
//        image = in.readString();
//        description = in.readString();
//        points_cost = in.readDouble();
//        updated_at = in.readLong();
//        options = in.reArrLi
//    }

//    public static final Creator<MallItemObject> CREATOR = new Creator<MallItemObject>() {
//        @Override
//        public MallItemObject createFromParcel(Parcel in) {
//            return new MallItemObject(in);
//        }
//
//        @Override
//        public MallItemObject[] newArray(int size) {
//            return new MallItemObject[size];
//        }
//    };

//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeInt(id);
//        dest.writeString(name);
//        dest.writeString(image);
//        dest.writeString(description);
//        dest.writeDouble(points_cost);
//        dest.writeLong(updated_at);
//    }

    public MallItemObject(JSONObject json) {
        id = json.optInt("id");
        name = json.optString("name");
        image = json.optString("image");
        description = json.optString("description");
        points_cost = json.optDouble("points_cost");
        updated_at = json.optLong("updated_at");

        JSONArray jsonOptions = json.optJSONArray("options");
        if (jsonOptions != null) {
            for (int i = 0; i < jsonOptions.length(); ++i) {
                options.add(new Option(jsonOptions.optJSONObject(i)));
            }
        }
    }

    public MallItemObject() {

    }

    public static class Option implements Serializable {

        int gift_id; //: 3,
        String name; //: "S 码（165-50kg）",
        int status;//: 1,
        int size; //: 100,
        long created_at; //: 1449832758000,
        long updated_at; //: 1449832758000,
        int id; // : 1

        Option(JSONObject json) {
            gift_id = json.optInt("gift_id");
            name = json.optString("name", "");
            status = json.optInt("status");
            size = json.optInt("size");
            created_at = json.optLong("created_at");
            updated_at = json.optLong("updated_at");
            id = json.optInt("id");
        }

        public int getGift_id() {
            return gift_id;
        }

        public String getName() {
            return name;
        }

        public int getStatus() {
            return status;
        }

        public int getSize() {
            return size;
        }

        public long getCreated_at() {
            return created_at;
        }

        public long getUpdated_at() {
            return updated_at;
        }

        public int getId() {
            return id;
        }
    }

    public MallItemObject(int id, String name, String image, String description, double points_cost,
                          long updated_at) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.points_cost = points_cost;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Option> getOptions() {
        return options;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPoints_cost() {
        return points_cost;
    }

    public void setPoints_cost(double points_cost) {
        this.points_cost = points_cost;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }


}
