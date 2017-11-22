package net.coding.program;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.common.BlankViewDisplay;
import net.coding.program.common.Global;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.pickphoto.ImageInfo;
import net.coding.program.common.ui.BaseFragment;
import net.coding.program.common.util.FileUtil;
import net.coding.program.model.AttachmentFileObject;
import net.coding.program.project.detail.file.FileSaveHelp;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by chaochen on 2014-9-7.
 */
@EFragment(R.layout.activity_image_pager_item)
public class ImagePagerFragment extends BaseFragment {

    public static final int HTTP_CODE_FILE_NOT_EXIST = 1304;
    public static DisplayImageOptions optionsImage = new DisplayImageOptions
            .Builder()
            .showImageForEmptyUri(R.drawable.image_not_exist)
            .showImageOnFail(R.drawable.image_not_exist)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .cacheInMemory(false)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.NONE_SAFE)
            .build();
    private final View.OnClickListener onClickImageClose = v -> getActivity().onBackPressed();

    @ViewById
    DonutProgress circleLoading;
    @ViewById
    View imageLoadFail;
    @ViewById
    ViewGroup rootLayout;

    @ViewById
    View blankLayout;

    View image;
    HashMap<String, AttachmentFileObject> picCache;

    File mFile;
    @FragmentArg
    String uri;
    @FragmentArg
    String fileId;
    @FragmentArg
    int mProjectObjectId;

    // 是否允许使用自己的菜单
    @FragmentArg
    boolean customMenu = true;

    private String URL_FILES = "";
    private AsyncHttpClient client;

    public void setData(String uriString) {
        uri = uriString;
    }

    public void setData(String fileId, int mProjectObjectId) {
        this.fileId = fileId;
        this.mProjectObjectId = mProjectObjectId;
    }

    @AfterViews
    void init() {
        setHasOptionsMenu(customMenu);

        circleLoading.setVisibility(View.INVISIBLE);
        if (uri != null) {
            showPhoto();
        }
    }

    private void getPhotoFromNetwork() {
        getNetwork(URL_FILES, URL_FILES);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_empty, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Click
    protected final void rootLayout() {
        getActivity().onBackPressed();
    }

    @Override
    public void onDestroyView() {
        if (image instanceof GifImageView) {
            ((GifImageView) image).setImageURI(null);
        }

        super.onDestroyView();
    }

    private void showPhoto() {
        if (!isAdded()) {
            return;
        }

        ImageSize size = new ImageSize(10000, 10000);
        getImageLoad().imageLoader.loadImage(uri, size, optionsImage, new SimpleImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        circleLoading.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        if (!isAdded()) {
                            return;
                        }

                        circleLoading.setVisibility(View.GONE);
                        imageLoadFail.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) {
                        if (!isAdded()) {
                            return;
                        }

                        circleLoading.setVisibility(View.GONE);

                        File file;
                        if (ImageInfo.isLocalFile(uri)) {
                            file = ImageInfo.getLocalFile(uri);
                        } else {
                            file = getImageLoad().imageLoader.getDiskCache().get(imageUri);
                        }
                        if (Global.isGifByFile(file)) {
                            image = getActivity().getLayoutInflater().inflate(R.layout.imageview_gif, rootLayout, false);
                            rootLayout.addView(image);
                            image.setOnClickListener(onClickImageClose);
                        } else {
                            SubsamplingScaleImageView photoView = (SubsamplingScaleImageView) getActivity().getLayoutInflater().inflate(R.layout.imageview_touch, rootLayout, false);
                            photoView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
                            image = photoView;
                            rootLayout.addView(image);
                            photoView.setOnClickListener(v -> getActivity().onBackPressed());
                        }

                        image.setOnLongClickListener(v -> {
                            new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                                    .setItems(new String[]{"保存到手机"}, (dialog, which) -> {
                                        if (which == 0) {
                                            if (client == null) {
                                                client = MyAsyncHttpClient.createClient(getActivity());
                                                client.get(getActivity(), imageUri, new FileAsyncHttpResponseHandler(mFile) {

                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file1) {
                                                        if (!isResumed()) {
                                                            return;
                                                        }
                                                        client = null;
                                                        showButtomToast("保存失败");
                                                    }

                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, File file1) {
                                                        if (!isResumed()) {
                                                            return;
                                                        }
                                                        client = null;
                                                        showButtomToast("图片已保存到:" + file1.getPath());
                                                        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file1)));/**/
                                                    }
                                                });
                                            }

                                        }
                                    })
                                    .show();

                            return true;
                        });

                        try {
                            if (image instanceof GifImageView) {
                                Uri uri1 = Uri.fromFile(file);
                                ((GifImageView) image).setImageURI(uri1);
                            } else if (image instanceof SubsamplingScaleImageView) {
                                SubsamplingScaleImageView scaleImageView = (SubsamplingScaleImageView) ImagePagerFragment.this.image;
                                scaleImageView.setImage(ImageSource.uri(file.getAbsolutePath()));
                            }
                        } catch (Exception e) {
                            Global.errorLog(e);
                        }
                    }
                },
                (imageUri, view, current, total) -> {
                    if (!isAdded()) {
                        return;
                    }

                    int progress = current * 100 / total;
                    circleLoading.setProgress(progress);
                });

        FileSaveHelp fileSaveHelp = new FileSaveHelp(getActivity());
        mFile = FileUtil.getDestinationInExternalPublicDir(fileSaveHelp.getFileDownloadPath(), uri.replaceAll(".*/(.*?)", "$1"));
    }

    @Override
    public void parseJson(int code, JSONObject response, String tag, int pos, Object data) throws JSONException {
        if (tag.equals(URL_FILES)) {
            if (code == 0) {
                setHasOptionsMenu(false);
                getActivity().invalidateOptionsMenu();

                JSONObject file = response.getJSONObject("data").getJSONObject("file");
                AttachmentFileObject mFileObject = new AttachmentFileObject(file);
                if (picCache != null) {
                    picCache.put(mFileObject.file_id, mFileObject);
                }
                uri = mFileObject.preview;
                showPhoto();
            } else {
                setHasOptionsMenu(true);
                getActivity().invalidateOptionsMenu();
                showErrorMsg(code, response);
                if (code == HTTP_CODE_FILE_NOT_EXIST) {
                    BlankViewDisplay.setBlank(0, this, true, blankLayout, null);
                } else {
                    BlankViewDisplay.setBlank(0, this, false, blankLayout, v -> getPhotoFromNetwork());
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (client != null) {
            client.cancelRequests(getActivity(), true);
            client = null;
        }

        super.onDestroy();
    }
}
