package net.coding.program.common.module.maopao;

import net.coding.program.common.ImageInfo;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/11/24.
 */ // 因为PhotoData包含Uri，不能直接序列化，所以有了这个类
public class PhotoDataSerializable implements Serializable {
    public String uriString = "";
    public String serviceUri = "";
    public ImageInfo mImageInfo;

    public PhotoDataSerializable(PhotoData data) {
        uriString = data.uri.toString();
        serviceUri = data.serviceUri;
        mImageInfo = data.mImageinfo;
    }
}
