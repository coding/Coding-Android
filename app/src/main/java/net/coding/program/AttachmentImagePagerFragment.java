package net.coding.program;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
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
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.project.detail.AttachmentsPicDetailActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 浏览项目文档里的图片用
 * Created by yangzhen on 14-12-4.
 */
@EFragment(R.layout.activity_image_pager_item)
public class AttachmentImagePagerFragment extends BaseFragment {

    private String URL_FILES_BASE = Global.HOST + "/api/project/%s/files/%s/view";
    private String URL_FILES = "";

    @ViewById
    PhotoView image;

    @ViewById
    ProgressBar loading;

    @FragmentArg
    String fileId;

    @FragmentArg
    String mProjectObjectId;

    AttachmentFileObject mFileObject;

    HashMap<String, AttachmentFileObject> picCache;

    File mFile;

    AttachmentsPicDetailActivity parentActivity;

    public void setData(String fileId, String mProjectObjectId) {
        this.fileId = fileId;
        this.mProjectObjectId = mProjectObjectId;
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
        image.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                getActivity().onBackPressed();
            }
        });

        image.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                getActivity().onBackPressed();
            }
        });

        parentActivity = (AttachmentsPicDetailActivity) getActivity();
        if (parentActivity != null) {
            //在AttachmentsPicDetailActivity中存放了缓存下来的结果
            picCache = parentActivity.getPicCache();
            if (picCache.containsKey(fileId)) {
                mFileObject = picCache.get(fileId);
                showPic(mFileObject.preview);
            } else {
                //如果之前没有缓存过，那么获取并在得到结果后存入
                URL_FILES = String.format(URL_FILES_BASE, mProjectObjectId, fileId);
                getNetwork(URL_FILES, URL_FILES);
            }
        }


    }

    /**
     * 获取文件的存放路径
     *
     * @return 文件的存放路径
     */
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

    @Override
    public void parseJson(int code, JSONObject response, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(URL_FILES)) {
            if (code == 0) {
                JSONObject file = response.getJSONObject("data").getJSONObject("file");
                mFileObject = new AttachmentFileObject(file);
                if (picCache != null) {
                    picCache.put(mFileObject.file_id, mFileObject);
                    parentActivity.setAttachmentFileObject(mFileObject);
                }
                showPic(mFileObject.preview);
            } else {
                showErrorMsg(code, response);
            }

        }
    }

    private void showPic(String uri) {
        getImageLoad().imageLoader.displayImage(uri, image, optionsImage, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                loading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = "未知错误";
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
                                            AsyncHttpClient client = MyAsyncHttpClient.createClient(getActivity());
                                            client.get(imageUri, new FileAsyncHttpResponseHandler(mFile) {

                                                @Override
                                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                                    showButtomToast("保存失败");
                                                }

                                                @Override
                                                public void onSuccess(int statusCode, Header[] headers, File file) {
                                                    showButtomToast("图片已保存到:" + file.getPath());
                                                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));/**/
                                                }
                                            });

                                        }
                                    }
                                })
                                .show();

                        return true;
                    }
                });
            }
        });

        mFile = FileUtil.getDestinationInExternalPublicDir(getFileDownloadPath(), uri.replaceAll(".*/(.*?)", "$1"));
    }

}
