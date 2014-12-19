package net.coding.program;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.common.FileUtil;
import net.coding.program.common.network.BaseFragment;
import net.coding.program.common.network.MyAsyncHttpClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;

import java.io.File;

import pl.droidsonroids.gif.GifImageView;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by chaochen on 14-9-7.
 */
@EFragment(R.layout.activity_image_pager_item)
public class ImagePagerFragment extends BaseFragment {

    @FragmentArg
    String uri;

    @ViewById
    ProgressBar loading;

    @ViewById
    ViewGroup rootLayout;

    ImageView image;

    File mFile;

    public void setData(String uriString) {
        uri = uriString;
    }

    public static DisplayImageOptions optionsImage = new DisplayImageOptions
            .Builder()
            .showImageForEmptyUri(R.drawable.ic_default_image)
            .showImageOnFail(R.drawable.ic_default_image)
            .cacheOnDisk(true)
            .cacheInMemory(false)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .build();

    @AfterViews
    void init() {
        if (isGif(uri)) {
            GifImageView gifView = (GifImageView) getActivity().getLayoutInflater().inflate(R.layout.imageview_gif, null);
            image = gifView;

            rootLayout.addView(image);

        } else {
            PhotoView photoView = (PhotoView) getActivity().getLayoutInflater().inflate(R.layout.imageview_touch, null);
            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    getActivity().onBackPressed();
                }
            });

            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    getActivity().onBackPressed();
                }
            });

            image = photoView;
            rootLayout.addView(image);
        }

            getImageLoad().imageLoader.displayImage(uri, image, optionsImage, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    loading.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    String message;
                    switch (failReason.getType()) {
                        case IO_ERROR:
                            message = "IO错误";
                            break;
                        case DECODING_ERROR:
                            message = "图片编码错误";
                            break;
                        case NETWORK_DENIED:
                            message = "载入图片超时";
                            break;
                        case OUT_OF_MEMORY:
                            message = "内存不足";
                            break;
                        case UNKNOWN:
                            message = "未知错误";
                            break;
                        default:
                            message = "未知错误";
                            break;
                    }
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                    loading.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) {
                    loading.setVisibility(View.GONE);

                    image.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            new AlertDialog.Builder(getActivity())
                                    .setItems(new String[]{"保存到手机"}, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                if (client == null) {
                                                    client = MyAsyncHttpClient.createClient(getActivity());
                                                    client.get(getActivity(), imageUri, new FileAsyncHttpResponseHandler(mFile) {

                                                        @Override
                                                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                                            Log.d("", "ddd ff");
                                                            client = null;
                                                            showButtomToast("保存失败");
                                                        }

                                                        @Override
                                                        public void onSuccess(int statusCode, Header[] headers, File file) {
                                                            Log.d("", "ddd ss");
                                                            client = null;
                                                            showButtomToast("图片已保存到:" + file.getPath());
                                                            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));/**/
                                                        }
                                                    });
                                                }

                                            }
                                        }
                                    })
                                    .show();

                            return true;
                        }
                    });

                    if (image instanceof GifImageView) {
                        File file = getImageLoad().imageLoader.getDiskCache().get(imageUri);
                        image.setImageURI(Uri.fromFile(file));

//                        new Handler() {
//                            @Override
//                            public void handleMessage(Message msg) {
//                                File file = getImageLoad().imageLoader.getDiskCache().get(imageUri);
//                                image.setImageURI(Uri.parse(file.getAbsolutePath()));
//                            }
//                        }.sendEmptyMessageDelayed(0, 1000);
                    }

                }


            });

        mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), uri.replaceAll(".*/(.*?)", "$1"));
    }

    private boolean isGif(String uri) {
        return uri.toLowerCase().endsWith(".gif");
    }

    private AsyncHttpClient client;

    @Override
    public void onDestroy() {
        if (client != null) {
            client.cancelRequests(getActivity(), true);
            client = null;
        }

        super.onDestroy();
    }

    public String getFileDownloadPath() {
        String path;
        String defaultPath = Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER;
        SharedPreferences share = getActivity().getSharedPreferences(FileUtil.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        if (share.contains(FileUtil.DOWNLOAD_PATH)) {
            path = share.getString(FileUtil.DOWNLOAD_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + FileUtil.DOWNLOAD_FOLDER);
        } else {
            path = defaultPath;
        }
        return path;
    }

}
