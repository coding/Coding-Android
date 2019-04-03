package net.coding.program.common.module.maopao;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chenchao on 2017/11/24.
 */
public class MaopaoDraft implements Serializable {
    private String input = "";

    private LocationObject locationObject = LocationObject.undefined();

    private ArrayList<PhotoDataSerializable> photos = new ArrayList<>();

    public MaopaoDraft() {
    }

    public MaopaoDraft(String input, ArrayList<PhotoData> photos, LocationObject locationObject) {
        this.input = input;
        this.photos = new ArrayList<>();
        for (PhotoData item : photos) {
            this.photos.add(new PhotoDataSerializable(item));
        }
        this.locationObject = locationObject;
    }

    public boolean isEmpty() {
        return input.isEmpty() && photos.isEmpty();
    }

    public String getInput() {
        return input;
    }

    public LocationObject getLocation() {
        return locationObject;
    }

    public ArrayList<PhotoData> getPhotos() {
        ArrayList<PhotoData> data = new ArrayList<>();
        for (PhotoDataSerializable item : photos) {
            data.add(new PhotoData(item));
        }

        return data;
    }
}
