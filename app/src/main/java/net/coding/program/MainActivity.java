package net.coding.program;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.roughike.bottombar.BottomBar;
import com.tbruyelle.rxpermissions2.RxPermissions;

import net.coding.program.common.Global;
import net.coding.program.common.GlobalCommon;
import net.coding.program.common.GlobalData;
import net.coding.program.common.GlobalVar_;
import net.coding.program.common.Unread;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.event.EventMessage;
import net.coding.program.common.event.EventNotifyBottomBar;
import net.coding.program.common.event.EventShowBottom;
import net.coding.program.common.model.AccountInfo;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.util.Login;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.maopao.MainMaopaoFragment_;
import net.coding.program.message.UsersListFragment_;
import net.coding.program.network.BaseHttpObserver;
import net.coding.program.network.Network;
import net.coding.program.pay.WXPay;
import net.coding.program.project.MainProjectFragment;
import net.coding.program.push.CodingPush;
import net.coding.program.push.xiaomi.EventPushToken;
import net.coding.program.push.xiaomi.EventUnbindToken;
import net.coding.program.setting.MainSettingFragment_;
import net.coding.program.task.MainTaskFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.api.builder.FragmentBuilder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.coding.net.checknetwork.CheckNetworkIntentService;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EActivity(R.layout.activity_main_parent)
public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";
    private static boolean sNeedWarnEmailNoValidLogin = false;
    private static boolean sNeedWarnEmailNoValidRegister = false;
    @ViewById
    BottomBar bottomBar;
    @ViewById
    ViewGroup container;

    @DimensionPixelSizeRes(R.dimen.main_container_merge_bottom)
    int bottomMerge;
    @Pref
    GlobalVar_ globalVar;

    private Handler handerNotify = null;

    private long exitTime = 0;
    private boolean mKeyboardUp;
    private boolean isResume = false;

    public static void setNeedWarnEmailNoValidLogin() {
        sNeedWarnEmailNoValidLogin = true;
    }

    public static void setNeedWarnEmailNoValidRegister() {
        sNeedWarnEmailNoValidRegister = true;
    }

    private void setListenerToRootView() {
        final View rootView = getWindow().getDecorView().findViewById(R.id.frameLayout);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // getActionBarHeight() + getStatusBarHeight() + bottomBar();
            final int headerHeight = GlobalCommon.dpToPx(150);
            int rootViewHeight = rootView.getRootView().getHeight();
            int rootHeight = rootView.getHeight();
            int heightDiff = rootViewHeight - rootHeight;
            if (heightDiff > headerHeight) {
                if (!mKeyboardUp) {
                    mKeyboardUp = true;
                    showBottomBar(!mKeyboardUp);
                }
            } else {
                if (mKeyboardUp) {
                    mKeyboardUp = false;
                    setBottomBar();
                }
            }

        });
    }

    private void setBottomBar() {
        bottomBar.postDelayed(() -> {
            showBottomBar(!mKeyboardUp);
        }, 300);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MarketingHelp.showMarketing(this);

        warnMailNoValidLogin();
        warnMailNoValidRegister();

        CodingPush.INSTANCE.bindGK(this, AccountInfo.loadAccount(this).global_key);

        startExtraServiceDelay();
        EventBus.getDefault().register(this);

        requestPermission();

        WXPay.getInstance().regToWeixin(this);
    }

    @UiThread(delay = 2000)
    void requestPermission() {
        if (!isResume) {
            return;
        }

        requestPermissionReal();
    }

    @UiThread(delay = 3000)
    void startExtraServiceDelay() {
        if (MainActivity.this.isFinishing()) {
            return;
        }

        startExtraService();
    }

    @Override
    public void onResume() {
        super.onResume();
        isResume = true;

        handerNotify = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "receiver handler message");
                super.handleMessage(msg);
                updateNotifyFromService();

                handerNotify.sendEmptyMessageDelayed(0, 5 * 1000);
            }
        };

        handerNotify.sendEmptyMessage(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        isResume = false;

        if (handerNotify != null) {
            handerNotify.removeMessages(0);
            handerNotify = null;
        }
    }

    @SuppressLint("CheckResult")
    private void requestPermissionReal() {
        RxPermissions permissions = new RxPermissions(this);
        permissions.requestEach(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(permission -> {
                    if (permission.granted) {
                        startPushService();
                    }
                });
    }

    protected void startExtraService() {
//         检查客户端的网络状况
        startNetworkCheckService();
    }

    protected void startPushService() {
        runOtherPushServer();
    }

    private void runOtherPushServer() {
        CodingPush.INSTANCE.onCreate(this, AccountInfo.loadAccount(this).global_key);
    }

    private void startNetworkCheckService() {
        Intent intent = new Intent(this, CheckNetworkIntentService.class);
        String extra = Global.getExtraString(this);
        intent.putExtra("PARAM_APP", extra);

        intent.putExtra("PARAM_GK", GlobalData.sUserObject.global_key);
        String sid = MyAsyncHttpClient.getCookie(this, Global.HOST);
        intent.putExtra("PARAM_COOKIE", sid);

        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateNotifyFromService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CodingPush.INSTANCE.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void warnMailNoValidLogin() {
        if (sNeedWarnEmailNoValidLogin) {
            sNeedWarnEmailNoValidLogin = false;

            String emailString = GlobalData.sUserObject.email;
            boolean emailValid = GlobalData.sUserObject.isEmailValidation();
            if (!emailString.isEmpty() && !emailValid) {
                new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                        .setTitle("激活邮件")
                        .setMessage(R.string.alert_activity_email2)
                        .setPositiveButton("重发激活邮件", (dialog, which) -> {
                            Login.resendActivityEmail(MainActivity.this);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        }
    }

    private void warnMailNoValidRegister() {
        if (sNeedWarnEmailNoValidRegister) {
            sNeedWarnEmailNoValidRegister = false;

            new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setTitle("提示")
                    .setMessage(R.string.alert_activity_email)
                    .setPositiveButton("确定", null)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        CodingPush.INSTANCE.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @AfterViews
    final void initMainActivity() {
        setActionBarTitle("");

        Global.display(this);

        setListenerToRootView();

        bottomBar.setOnTabSelectListener(tabId -> switchTab(tabId));

    }

    protected void switchTab(int tabId) {
        isOpenDrawerLayout(tabId == R.id.tabTask);
        updateNotifyFromService();
        switch (tabId) {
            case R.id.tabProject:
                switchProject();
                break;

            case R.id.tabTask:
                switchFragment(MainTaskFragment_.FragmentBuilder_.class);
                break;

            case R.id.tabMaopao:
                switchFragment(MainMaopaoFragment_.FragmentBuilder_.class);
                break;

            case R.id.tabMessage:
                switchFragment(UsersListFragment_.FragmentBuilder_.class);
                break;

            case R.id.tabMy:
                switchSetting();
                break;
        }
    }

    protected void switchSetting() {
        switchFragment(MainSettingFragment_.FragmentBuilder_.class);
    }

    protected void switchProject() {
        switchFragment(MainProjectFragment.FragmentBuilder_.class);
    }

    final protected void switchFragment(Class<?> cls) {
        String tag = cls.getName();
        Fragment showFragment = getSupportFragmentManager().findFragmentByTag(tag);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (showFragment == null) {
            try {
                showFragment = (Fragment) ((FragmentBuilder) cls.newInstance()).build();
                fragmentTransaction.add(R.id.container, showFragment, tag);
            } catch (Exception e) {
                Global.errorLog(e);
            }
        } else {
            fragmentTransaction.show(showFragment);
        }

        List<Fragment> allFragments = getSupportFragmentManager().getFragments();
        if (allFragments != null) {
            for (Fragment item : allFragments) {
                if (item != showFragment) {
                    fragmentTransaction.hide(item);
                }
            }
        }

        fragmentTransaction.commit();

    }

    // 判断是否打开DrawerLayout
    private void isOpenDrawerLayout(boolean isOpen) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer == null) return;
        if (isOpen) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            exitApp();
        }
    }

    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            showButtomToast(R.string.exit_app);
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBottomBarNotify(EventNotifyBottomBar notify) {
        updateNotify();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBottomBar(EventShowBottom showBottom) {
        showBottomBar(showBottom.showBottom);
    }

    @SuppressLint("MissingPermission")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventPushToken(EventPushToken pushToken) {
        if (TextUtils.isEmpty(pushToken.getType()) || TextUtils.isEmpty(pushToken.getToken())) {
            return;
        }

        Map<String, String> map = new HashMap<>();
        map.put("push", pushToken.getType());
        map.put("token", pushToken.getToken());
        try {
            map.put("deviceBrand", Build.BRAND);
            map.put("deviceType", Build.MODEL);
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            map.put("imei", tm.getDeviceId());
        } catch (Exception e) {
            Global.errorLog(e);
        }

        Network.getRetrofit(this)
                .registerPush(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpObserver(this) {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        globalVar.edit()
                                .pushType()
                                .put(pushToken.getType())
                                .pushToken()
                                .put(pushToken.getToken())
                                .apply();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
//                        super.onFail(errorCode, error);
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventUnbindPushToken(EventUnbindToken unbind) {
        String type = globalVar.pushType().get();
        String token = globalVar.pushToken().get();
        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(token)) {
            return;
        }

        Map<String, String> map = new HashMap<>();
        map.put("push", type);
        map.put("token", token);

        Network.getRetrofit(this)
                .unRegisterPush(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseHttpObserver(this) {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        globalVar.edit()
                                .pushType()
                                .remove()
                                .pushToken()
                                .remove()
                                .apply();
                    }

                    @Override
                    public void onFail(int errorCode, @NonNull String error) {
//                        super.onFail(errorCode, error);
                    }
                });
    }

    private void showBottomBar(boolean show) {
        bottomBar.setVisibility(show ? View.VISIBLE : View.GONE);

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
        lp.bottomMargin = show ? bottomMerge : 0;
        container.setLayoutParams(lp);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventLoginOut(EventMessage eventMessage) {
        if (eventMessage.type == EventMessage.Type.loginOut) {
            finish();
        }
    }

    public void updateNotifyFromService() {
        UnreadNotify.update(this);
    }

    public void updateNotify() {
        Unread unread = GlobalData.sUnread;
        bottomBar.getTabWithId(R.id.tabProject).setBadgeCount(unread.getProjectCount() > 0 ? 0 : -1);
        int notifyCount = unread.getNotifyCount();
        if (notifyCount <= 0) {
            notifyCount = -1;
        }
        bottomBar.getTabWithId(R.id.tabMessage).setBadgeCount(notifyCount);
    }
}
