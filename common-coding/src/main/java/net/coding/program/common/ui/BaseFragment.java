package net.coding.program.common.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import net.coding.program.R;
import net.coding.program.common.FootUpdate;
import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.ImageLoadTool;
import net.coding.program.common.LoadMore;
import net.coding.program.common.StartActivity;
import net.coding.program.common.network.NetworkCallback;
import net.coding.program.common.network.NetworkImpl;
import net.coding.program.common.network.UmengFragment;
import net.coding.program.common.util.PermissionUtil;
import net.coding.program.common.util.SingleToast;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cc191954 on 14-8-11.
 * 封装了图片下载
 * 封装了网络请求
 */
public class BaseFragment extends UmengFragment implements NetworkCallback, LoadMore, StartActivity {

    protected NetworkImpl networkImpl;
    protected LayoutInflater mInflater;
    protected FootUpdate mFootUpdate = new FootUpdate();

    SingleToast mSingleToast;
    private ImageLoadTool imageLoadTool = new ImageLoadTool();
    private ProgressDialog mProgressDialog;

    protected void showProgressBar(boolean show) {
        showProgressBar(show, "");
    }

    protected void setProgressBarProgress() {
        if (mProgressDialog == null) {
            return;
        }

        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgress(30);
    }

    protected void setToolbar(String title, int toolbarId) {
        try {
            View rootLayout = getView();
            if (rootLayout != null) {
                Toolbar toolbar = (Toolbar) rootLayout.findViewById(toolbarId);
                BaseActivity activity = (BaseActivity) getActivity();
                activity.setSupportActionBar(toolbar);
                activity.setActionBarTitle("");
                TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
                toolbarTitle.setText(title);

                View actionBarCompShadow = rootLayout.findViewById(R.id.actionBarCompShadow);
                AppBarLayout appbarLayout = (AppBarLayout) rootLayout.findViewById(R.id.appbarLayout);
                // android 5.0 以下系统阴影太浓，设置为没有阴影
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    actionBarCompShadow.setVisibility(View.VISIBLE);
                } else {
                    actionBarCompShadow.setVisibility(View.GONE);
                    ViewCompat.setElevation(appbarLayout, GlobalUnit.ACTIONBAR_SHADOW);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void listViewAddHeaderSection(ListView listView) {
//        View listViewHeader = LayoutInflater.from(getContext()).inflate(R.layout.divide_top_15, listView, false);
//        listView.addHeaderView(listViewHeader, null, false);
    }

    protected void listViewAddFooterSection(ListView listView) {
        View listViewFooter = mInflater.inflate(R.layout.divide_bottom_15, listView, false);
        listView.addFooterView(listViewFooter, null, false);
    }

    protected ActionBar getActionBar() {
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            return actionBar;
        }

        return null;
    }

    protected void setActionBar(String name) {
    }

    protected void setActionBarShadow(int dp) {
        ActionBar actionBar = getActionBarActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(GlobalCommon.dpToPx(dp));
        }
    }

    protected void showProgressBar(boolean show, String message) {
        if (mProgressDialog == null) {
            return;
        }

        if (show) {
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        } else {
            mProgressDialog.hide();
        }
    }

    public AppCompatActivity getActionBarActivity() {
        return (AppCompatActivity) getActivity();
    }

    protected void showProgressBar(int messageId) {
        String message = getString(messageId);
        showProgressBar(true, message);
    }

    protected boolean progressBarIsShowing() {
        return mProgressDialog.isShowing();
    }

    protected ImageLoadTool getImageLoad() {
        return imageLoadTool;
    }

    @Override
    public void loadMore() {

    }

    protected void initSetting() {
        networkImpl.initSetting();
    }

    public boolean isLoadingFirstPage(String tag) {
        return networkImpl.isLoadingFirstPage(tag);
    }

    public boolean isLoadingLastPage(String tag) {
        return networkImpl.isLoadingLastPage(tag);
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        mInflater = LayoutInflater.from(getActivity());
        networkImpl = new NetworkImpl(getActivity(), this);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        mSingleToast = new SingleToast(getActivity());

        if (useEventBus()) {
            EventBus.getDefault().register(this);
        }
    }

    protected boolean useEventBus() {
        return false;
    }

    @Override
    public void onDestroy() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        super.onDestroy();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initSetting();
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    protected boolean checkPermission(int result, String[] permission, String tipString) {
        List<String> needApply = new ArrayList<>();
        for (String item : permission) {
            if (ActivityCompat.checkSelfPermission(getActivity(), item) != PackageManager.PERMISSION_GRANTED) {
                needApply.add(item);
            }
        }

        if (needApply.isEmpty()) {
            return true;
        }

        String[] applys = new String[needApply.size()];
        applys = needApply.toArray(applys);

        if (!TextUtils.isEmpty(tipString)) {
            Toast.makeText(getActivity(), tipString, Toast.LENGTH_SHORT).show();
        }
        requestPermissions(
                applys,
                result
        );

        return false;
    }

    @Override
    public void parseJson(int code, JSONObject respanse, String tag, int pos, Object data) throws JSONException {
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

//    protected void putNetwork(String url, RequestParams params, final String tag, Object extra) {
//        networkImpl.loadData(url, params, tag, -1, extra, NetworkImpl.Request.Put);
//    }

    protected void putNetwork(String url, RequestParams params, final String tag) {
        networkImpl.loadData(url, params, tag, -1, null, NetworkImpl.Request.Put);
    }

    public void getNetwork(String url) {
        networkImpl.loadData(url, null, url, -1, null, NetworkImpl.Request.Get);
    }

    protected void getNetwork(String url, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, null, tag, dataPos, data, NetworkImpl.Request.Get);
    }

    public void putNetwork(String url, final String tag) {
        networkImpl.loadData(url, null, tag, -1, null, NetworkImpl.Request.Put);
    }

    protected void showDialog(String title, String msg, DialogInterface.OnClickListener clickOk) {
        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle).setTitle(title)
                .setMessage(msg)
                .setPositiveButton("确定", clickOk)
                .setNegativeButton("取消", null)
                .show();
    }

    protected void showDialog(@StringRes int messageId, DialogInterface.OnClickListener clickOk) {
        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                .setMessage(messageId)
                .setPositiveButton("确定", clickOk)
                .setNegativeButton("取消", null)
                .show();
    }

    protected void showSingleDialog(@StringRes int messageId) {
        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                .setMessage(messageId)
                .setPositiveButton("确定", null)
                .show();
    }

    protected void showDialog(String message, DialogInterface.OnClickListener clickOk) {
        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
                .setMessage(message)
                .setPositiveButton("确定", clickOk)
                .setNegativeButton("取消", null)
                .show();
    }

    protected void showDialog(String[] titles, DialogInterface.OnClickListener clickOk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setItems(titles, clickOk).show();
    }

    public void putNetwork(String url, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, null, tag, dataPos, data, NetworkImpl.Request.Put);
    }

    public void putNetwork(String url, RequestParams params, final String tag, Object data) {
        networkImpl.loadData(url, params, tag, -1, data, NetworkImpl.Request.Put);
    }

    public void deleteNetwork(String url, final String tag) {
        networkImpl.loadData(url, null, tag, -1, null, NetworkImpl.Request.Delete);
    }

    public void deleteNetwork(String url, final String tag, int dataPos, Object data) {
        networkImpl.loadData(url, null, tag, dataPos, data, NetworkImpl.Request.Delete);
    }

    public void deleteNetwork(String url, final String tag, Object data) {
        networkImpl.loadData(url, null, tag, -1, data, NetworkImpl.Request.Delete);
    }

    protected void getNextPageNetwork(String url, final String tag) {
        networkImpl.getNextPageNetwork(url, tag);
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

    public void showButtomToast(String msg) {
        if (!isResumed() || mSingleToast == null) {
            return;
        }

        mSingleToast.showButtomToast(msg);
    }

    public void showMiddleToast(int id) {
        if (!isResumed() || mSingleToast == null) {
            return;
        }
        mSingleToast.showMiddleToast(id);
    }

    public void showMiddleToast(String msg) {
        if (!isResumed() || mSingleToast == null) {
            return;
        }
        mSingleToast.showMiddleToast(msg);
    }

    public void showButtomToast(int messageId) {
        if (!isResumed() || mSingleToast == null) {
            return;
        }

        mSingleToast.showButtomToast(messageId);
    }

    protected void iconfromNetwork(ImageView view, String url) {
        url = Global.translateStaticIcon(url);
        imageLoadTool.loadImage(view, Global.makeSmallUrl(view, url));
    }

    protected void iconfromNetwork(ImageView view, String url, DisplayImageOptions options) {
        url = Global.translateStaticIcon(url);
        imageLoadTool.loadImage(view, Global.makeSmallUrl(view, url), options);
    }

    protected void showDialogLoading() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showDialogLoading();
        }
    }

    protected void hideDialogLoading() {
        if (getActivity() instanceof BaseActivity) {
            showProgressBar(false);
            ((BaseActivity) getActivity()).hideProgressDialog();
        }
    }
}
