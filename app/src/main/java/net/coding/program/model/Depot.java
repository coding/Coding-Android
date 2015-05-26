package net.coding.program.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/3/26.
 */
public class Depot extends BaseDepot {

    private Parent parent;

    public Depot(JSONObject json) {
        super(json);
        parent = new Parent(json.optJSONObject("parent"));
    }

    private static class Parent implements Serializable {

        private boolean hasCommits;//: false,
        private int size; //: 0

        public Parent(JSONObject json) {
            hasCommits = json.optBoolean("hasCommits");
            size = json.optInt("size");
        }
    }

//    private     private     languages: {
//    private     private Java: 100
//    private     },
}

