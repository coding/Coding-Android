package net.coding.program.common.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/5/25.
 */
public class SourceDepot extends BaseDepot implements Serializable {
    public SourceDepot(JSONObject json) {
        super(json);
    }
}
