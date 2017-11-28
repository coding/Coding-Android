package net.coding.program.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class HttpResult<T> extends BaseHttpResult implements Serializable {

    private static final long serialVersionUID = -6843431901532382029L;

    @SerializedName("data")
    @Expose
    public T data;

}
