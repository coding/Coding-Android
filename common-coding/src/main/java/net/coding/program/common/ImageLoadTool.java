package net.coding.program.common;

import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.R;

/**
 * Created by chaochen on 14-9-22.
 */
public class ImageLoadTool {
    public static final DisplayImageOptions optionsRounded2 = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.ic_default_image)
            .showImageForEmptyUri(R.drawable.ic_default_image)
            .showImageOnFail(R.drawable.ic_default_image)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .displayer(new RoundedBitmapDisplayer(GlobalCommon.dpToPx((float) 2)))
            .build();

    public static DisplayImageOptions enterOptions = new DisplayImageOptions
            .Builder()
//            .showImageOnLoading(R.drawable.ic_default_user)
//            .showImageForEmptyUri(R.drawable.ic_default_user)
//            .showImageOnFail(R.drawable.ic_default_user)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();
    public static DisplayImageOptions options = new DisplayImageOptions
            .Builder()
            .showImageOnLoading(R.drawable.ic_default_user)
            .showImageForEmptyUri(R.drawable.ic_default_user)
            .showImageOnFail(R.drawable.ic_default_user)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();


    public static DisplayImageOptions bannerOptions = new DisplayImageOptions
            .Builder()
            .showImageOnLoading(R.drawable.ic_default_image)
            .showImageForEmptyUri(R.drawable.ic_default_image)
            .showImageOnFail(R.drawable.ic_default_image)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();
    public static DisplayImageOptions optionsImage = new DisplayImageOptions
            .Builder()
            .showImageOnLoading(R.drawable.ic_default_image)
            .showImageForEmptyUri(R.drawable.ic_default_image)
            .showImageOnFail(R.drawable.ic_default_image)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();
    public static DisplayImageOptions mallOptions = new DisplayImageOptions
            .Builder()
            .showImageOnLoading(R.drawable.ic_default_image)
            .showImageForEmptyUri(R.drawable.ic_default_image)
            .showImageOnFail(R.drawable.ic_default_image)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
            .build();

    public ImageLoader imageLoader = ImageLoader.getInstance();

    public ImageLoadTool() {
    }

    public static void loadUserImage(ImageView imageView, String url) {
        ImageLoader.getInstance().displayImage(url, imageView, options);
    }

    public static void loadFileImage(ImageView imageView, String url, DisplayImageOptions options) {
        ImageLoader.getInstance().displayImage(url, imageView, options);
    }

    public void loadImage(ImageView imageView, String url) {
        imageLoader.displayImage(Global.makeSmallUrl(imageView, url), imageView, options);
    }

    public void loadImageDefaultCoding(ImageView imageView, String url) {
        imageLoader.displayImage(Global.makeSmallUrl(imageView, url), imageView, optionsImage);
    }

    public void loadImage(ImageView imageView, String url, SimpleImageLoadingListener animate) {
        imageLoader.displayImage(Global.makeSmallUrl(imageView, url), imageView, options, animate);
    }

    public void loadImage(ImageView imageView, String url, DisplayImageOptions imageOptions) {
        imageLoader.displayImage(Global.makeSmallUrl(imageView, url), imageView, imageOptions);
    }

    public void loadImage(ImageView imageView, String url, DisplayImageOptions displayImageOptions, SimpleImageLoadingListener animate) {
        imageLoader.displayImage(url, imageView, displayImageOptions, animate);
    }

    public void loadImageFromUrl(ImageView imageView, String url) {
        imageLoader.displayImage(url, imageView, options);
    }

    public void loadImageFromUrl(ImageView imageView, String url, DisplayImageOptions displayImageOptions) {
        imageLoader.displayImage(url, imageView, displayImageOptions);
    }


}

