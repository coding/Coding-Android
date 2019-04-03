package net.coding.program.common.module.maopao;

import android.net.Uri;

import net.coding.program.common.ImageInfo;

import java.io.File;

/**
 * Created by chenchao on 2017/11/24.
 */
public class PhotoData {
    public ImageInfo mImageinfo;
    public Uri uri = Uri.parse("");
    public String serviceUri = "";

    public PhotoData(File file, ImageInfo info) {
        uri = Uri.fromFile(file);
        mImageinfo = info;
    }

    public PhotoData(PhotoDataSerializable data) {
        uri = Uri.parse(data.uriString);
        serviceUri = data.serviceUri;
        mImageinfo = data.mImageInfo;
    }
}
