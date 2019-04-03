package net.coding.program.common.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.coding.program.R;
import net.coding.program.common.CodingColor;
import net.coding.program.common.DialogUtil;
import net.coding.program.common.FootUpdate;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalSetting;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.StartActivity;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.model.RequestData;
import net.coding.program.common.network.NetworkCallback;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.umeng.UmengActivity;
import net.coding.program.common.util.SingleToast;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cc191954 on 14-8-16.
 * 封装了图片下载并缓存
 */
public class BaseActivity extends UmengActivity implements NetworkCallback, StartActivity {

    protected LayoutInflater mInflater;
    protected FootUpdate mFootUpdate = new FootUpdate();

    SingleToast mSingleToast;

    private ImageLoadTool imageLoadTool = new ImageLoadTool();
    private ProgressDialog mProgressDialog;
    private NetworkImpl networkImpl;
    /**
     * 载入动画
     */
    private DialogUtil.LoadingPopupWindow mDialogProgressPopWindow = null;

    protected void listViewAddFootSection(ListView listView) {
        View listViewFooter = getLayoutInflater().inflate(R.layout.divide_bottom_15, listView, false);
        listView.addFooterView(listViewFooter, null, false);
    }

    // 每个列表第一列前留空白，最好保留，设计师总是变来变去。
    protected void listViewAddHeaderSection(ListView listView) {
//        View listViewHeader = getLayoutInflater().inflate(R.layout.divide_top_15, listView, false);
//        listView.addHeaderView(listViewHeader, null, false);
    }

    protected View.OnFocusChangeListener createEditLineFocus(View line) {
        return createEditLineFocus(line, CodingColor.font1);
    }

    protected View.OnFocusChangeListener createEditLineFocus(View line, int selectColor) {
        return (v, hasFocus) -> {
            line.setBackgroundColor(hasFocus ? selectColor : CodingColor.divideLine);
        };
    }

    protected void showProgressBar(boolean show) {
        showProgressBar(show, "");
    }

    public void showProgressBar(boolean show, String message) {
        if (show) {
            mProgressDialog.setCancelable(isProgressCannCancel());
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        } else {
            mProgressDialog.hide();
        }
    }

    protected void showProgressBar(boolean show, int message) {
        String s = getString(message);
        showProgressBar(show, s);
    }

    protected void showProgressBar(int messageId) {
        String message = getString(messageId);
        showProgressBar(true, message);
    }

    public void showErrorMsg(int code, JSONObject json) {
        if (code == NetworkImpl.NETWORK_ERROR) {
            showButtomToast(R.string.connect_service_fail);
        } else {
            String msg = Global.getErrorMsg(json);
            if (!msg.isEmpty()) {
                showButtomToast(msg);
            }
        }
    }

    public void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    protected void setActionBarTitle(int title) {
        String titleString = getString(title);
        setActionBarTitle(titleString);
    }

    public void showErrorMsgMiddle(int code, JSONObject json) {
        if (code == NetworkImpl.NETWORK_ERROR) {
            showMiddleToast(R.string.connect_service_fail);
        } else {
            String msg = Global.getErrorMsg(json);
            if (!msg.isEmpty()) {
                showMiddleToast(msg);
            }
        }
    }

    public ImageLoadTool getImageLoad() {
        return imageLoadTool;
    }

    protected boolean isLoadingFirstPage(String tag) {
        return networkImpl.isLoadingFirstPage(tag);
    }

    protected boolean isLoadingLastPage(String tag) {
        return networkImpl.isLoadingLastPage(tag);
    }

    protected boolean isProgressCannCancel() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSingleToast = new SingleToast(this);

        networkImpl = new NetworkImpl(this, this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(isProgressCannCancel());

        mInflater = getLayoutInflater();
        initSetting();

        UnreadNotify.update(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(GlobalUnit.ACTIONBAR_SHADOW);
        }

        if (userEventBus()) {
            EventBus.getDefault().register(this);
        }

    }

    protected boolean userEventBus() {
        return false;
    }

    protected void hideActionbarShade() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0);
        }
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        GlobalSetting.getInstance().removeMessageNoNotify();


        super.onDestroy();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    protected void initSetting() {
        networkImpl.initSetting();
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
    }

    protected void getNextPageNetwork(String url, final String tag) {
        networkImpl.getNextPageNetwork(url, tag);
    }

    protected void postNetwork(RequestData request, String tag) {
        postNetwork(request.url, request.params, tag);
    }

    protected void postNetwork(RequestData request, String tag, Object data) {
        postNetwork(request.url, request.params, tag, -1, data);
    }

    protected void postNetwork(String url, String tag) {
        postNetwork(url, new RequestParams(), tag);
    }

    protected void postNetwork(String url, RequestParams params, final String tag) {
        networkImpl.loadData(url, params, tag, -1, null, NetworkImpl.Request.Post);
    }

    protected void postNetwork(String url, RequestParams params, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, params, tag, dataPos, data, NetworkImpl.Request.Post);
    }

    @Override
    public void getNetwork(String url, final String tag) {
        networkImpl.loadData(url, null, tag, -1, null, NetworkImpl.Request.Get);
    }

    protected void getNetwork(String url, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, null, tag, dataPos, data, NetworkImpl.Request.Get);
    }

    protected void putNetwork(String url, RequestParams params, final String tag) {
        networkImpl.loadData(url, params, tag, -1, null, NetworkImpl.Request.Put);
    }

    protected void putNetwork(String url, RequestParams params, String tag, int pos, Object object) {
        networkImpl.loadData(url, params, tag, pos, object, NetworkImpl.Request.Put);
    }

    protected void putNetwork(String url, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, null, tag, dataPos, data, NetworkImpl.Request.Put);
    }

    protected void deleteNetwork(String url, final String tag) {
        networkImpl.loadData(url, null, tag, -1, null, NetworkImpl.Request.Delete);
    }

    protected void deleteNetwork(String url, RequestParams params, final String tag) {
        networkImpl.loadData(url, params, tag, -1, null, NetworkImpl.Request.Delete);
    }

    protected void deleteNetwork(String url, final String tag, Object id) {
        networkImpl.loadData(url, null, tag, -1, id, NetworkImpl.Request.Delete);
    }

    protected void deleteNetwork(String url, final String tag, int dataPos, Object id) {
        networkImpl.loadData(url, null, tag, dataPos, id, NetworkImpl.Request.Delete);
    }

    protected void showDialog(String title, String msg, DialogInterface.OnClickListener clickOk) {
        showDialog(title, msg, clickOk, null);
    }

    protected void showDialog(String msg, DialogInterface.OnClickListener clickOk) {
        showDialog("", msg, clickOk, null);
    }

//    protected void showListDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
//        AlertDialog dialog = builder.setItems()
//    }

    protected void showDialog(String title, String msg, DialogInterface.OnClickListener clickOk,
                              DialogInterface.OnClickListener clickCancel) {
        showDialog(title, msg, clickOk, clickCancel, "确定", "取消");
    }

    protected void showDialog(@StringRes int messageId, DialogInterface.OnClickListener clickOk) {
        showDialog("", getString(messageId), clickOk, null);
    }


    protected void showDialog(String title, String msg, DialogInterface.OnClickListener clickOk,
                              DialogInterface.OnClickListener clickCancel,
                              CharSequence okButton,
                              CharSequence cancelButton) {
        showDialog(title, msg, clickOk, clickCancel, null, okButton, cancelButton, "");
    }


    protected void showWarnDialog(String title, String msg, DialogInterface.OnClickListener clickOk,
                                  DialogInterface.OnClickListener clickCancel,
                                  CharSequence okButton,
                                  CharSequence cancelButton) {
        showDialog(title, msg, clickOk, clickCancel, null, okButton, cancelButton, "", R.style.CodingWarnDialog);
    }


    protected void showDialog(String title, String msg, DialogInterface.OnClickListener clickOk,
                              DialogInterface.OnClickListener clickCancel,
                              DialogInterface.OnClickListener clickNeutral,
                              CharSequence okButton,
                              CharSequence cancelButton,
                              CharSequence neutralButton) {
        showDialog(title, msg, clickOk, clickCancel, clickNeutral, okButton, cancelButton, neutralButton, R.style.MyAlertDialogStyle);
    }

    private void showDialog(String title, String msg, DialogInterface.OnClickListener clickOk, DialogInterface.OnClickListener clickCancel, DialogInterface.OnClickListener clickNeutral, CharSequence okButton, CharSequence cancelButton, CharSequence neutralButton, int style) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, style);
        builder.setMessage(msg);

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        if (okButton != null) {
            builder.setPositiveButton(okButton, clickOk);
        }

        if (cancelButton != null) {
            builder.setNegativeButton(cancelButton, clickCancel);
        }

        if (clickNeutral != null && !TextUtils.isEmpty(neutralButton)) {
            builder.setNeutralButton(neutralButton, clickNeutral);
        }

        builder.show();
    }

    public void showButtomToast(String msg) {
        mSingleToast.showButtomToast(msg);
    }

    public void showMiddleToast(int id) {
        mSingleToast.showMiddleToast(id);
    }

    public void showMiddleToast(String msg) {
        mSingleToast.showMiddleToast(msg);
    }

    public void showMiddleToastLong(String msg) {
        mSingleToast.showMiddleToastLong(msg);
    }

    public void showButtomToast(int messageId) {
        mSingleToast.showButtomToast(messageId);
    }

    protected void iconfromNetwork(ImageView view, String url) {
        url = Global.translateStaticIcon(url);
        imageLoadTool.loadImage(view, Global.makeSmallUrl(view, url));
    }

    protected void iconfromNetwork(ImageView view, String url, SimpleImageLoadingListener animate) {
        url = Global.translateStaticIcon(url);
        imageLoadTool.loadImage(view, Global.makeSmallUrl(view, url), animate);
    }

    protected void iconfromNetwork(ImageView view, String url, DisplayImageOptions options) {
        url = Global.translateStaticIcon(url);
        imageLoadTool.loadImage(view, Global.makeSmallUrl(view, url), options);
    }

    protected void imagefromNetwork(ImageView view, String url) {
        url = Global.translateStaticIcon(url);
        imageLoadTool.loadImageFromUrl(view, url);
    }

    protected void imagefromNetwork(ImageView view, String url, DisplayImageOptions options) {
        url = Global.translateStaticIcon(url);
        imageLoadTool.loadImageFromUrl(view, url, options);
    }

    public void initDialogLoading() {
        if (mDialogProgressPopWindow == null) {
            mDialogProgressPopWindow = DialogUtil.initProgressDialog(this, this::hideProgressDialog);
        }
    }

    protected void projectIconfromNetwork(ImageView view, String url) {
        iconfromNetwork(view, url, ImageLoadTool.optionsRounded2);
    }

    public void showDialogLoading() {
        initDialogLoading();
        DialogUtil.showProgressDialog(this, mDialogProgressPopWindow);
    }

    public void hideProgressDialog() {
        if (mDialogProgressPopWindow != null) {
            DialogUtil.hideDialog(mDialogProgressPopWindow);
        }
    }



}
