package net.coding.program;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.roughike.bottombar.BottomBar;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;

import net.coding.program.common.Global;
import net.coding.program.common.LoginBackground;
import net.coding.program.common.Unread;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.common.network.MyAsyncHttpClient;
import net.coding.program.common.network.util.Login;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.event.EventMessage;
import net.coding.program.event.EventNotifyBottomBar;
import net.coding.program.event.EventShowBottom;
import net.coding.program.login.MarketingHelp;
import net.coding.program.login.ZhongQiuGuideActivity;
import net.coding.program.message.UsersListFragment_;
import net.coding.program.model.AccountInfo;
import net.coding.program.project.MainProjectFragment_;
import net.coding.program.project.ProjectFragment;
import net.coding.program.project.init.InitProUtils;
import net.coding.program.setting.MainSettingFragment_;
import net.coding.program.task.MainTaskFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.api.builder.FragmentBuilder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import network.coding.net.checknetwork.CheckNetworkIntentService;

@EActivity(R.layout.activity_main_parent)
public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";
    public static final String BroadcastPushStyle = "BroadcastPushStyle";

    @Extra
    String mPushUrl;
    @StringArrayRes
    String drawer_title[];
    @StringArrayRes
    String maopao_action_types[];
    @ViewById
    BottomBar bottomBar;
//    @ViewById
//    AppBarLayout appbar;
//    @ViewById
//    View actionBarCompShadow;
//    TextView toolbarTitle;
//    TextView toolbarProjectTitle;
//private Spinner toolbarMaopaoTitle;

    private static boolean sNeedWarnEmailNoValidLogin = false;

    public static void setNeedWarnEmailNoValidLogin() {
        sNeedWarnEmailNoValidLogin = true;
    }

    private static boolean sNeedWarnEmailNoValidRegister = false;

    public static void setNeedWarnEmailNoValidRegister() {
        sNeedWarnEmailNoValidRegister = true;
    }

    boolean mFirstEnter = true;
    BroadcastReceiver mUpdatePushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotifyService();
        }
    };
    int mSelectPos = 0;
    MaopaoTypeAdapter mSpinnerAdapter;
    private long exitTime = 0;

    private boolean mKeyboardUp;

    private void setListenerToRootView() {
        final View rootView = getWindow().getDecorView().findViewById(R.id.drawer_layout);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int headerHeight = Global.dpToPx(100);// getActionBarHeight() + getStatusBarHeight();
                int rootViewHeight = rootView.getRootView().getHeight();
                int rootHeight = rootView.getHeight();
                int heightDiff = rootViewHeight - rootHeight;
                if (heightDiff > headerHeight) {
                    if (!mKeyboardUp) {
                        mKeyboardUp = true;
                    }
                } else {
                    mKeyboardUp = false;
                }

                bottomBar.setVisibility(mKeyboardUp ? View.GONE : View.VISIBLE);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZhongQiuGuideActivity.showHolidayGuide(this);

        IntentFilter intentFilter = new IntentFilter(BroadcastPushStyle);
        registerReceiver(mUpdatePushReceiver, intentFilter);

//        XGPushConfig.enableDebug(this, true);
        // qq push
        updateNotifyService();
        pushInXiaomi();
        startNetworkCheckService();

        LoginBackground loginBackground = new LoginBackground(this);
        loginBackground.update();

        mFirstEnter = (savedInstanceState == null);

        if (savedInstanceState != null) {
            mSelectPos = savedInstanceState.getInt("pos", R.id.tabProject);
        }

        if (mPushUrl != null) {
            URLSpanNoUnderline.openActivityByUri(this, mPushUrl, true);
            mPushUrl = null;
            getIntent().getExtras().remove("mPushUrl");
        }

        MarketingHelp.showMarketing(this);

        warnMailNoValidLogin();
        warnMailNoValidRegister();

        EventBus.getDefault().register(this);

    }

    private void startNetworkCheckService() {
        Intent intent = new Intent(this, CheckNetworkIntentService.class);
        String extra = Global.getExtraString(this);
        intent.putExtra("PARAM_APP", extra);

        intent.putExtra("PARAM_GK", MyApp.sUserObject.global_key);
        String sid = MyAsyncHttpClient.getCookie(this, Global.HOST);
        intent.putExtra("PARAM_COOKIE", sid);

        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateNotifyFromService();
    }

    private void warnMailNoValidLogin() {
        if (sNeedWarnEmailNoValidLogin) {
            sNeedWarnEmailNoValidLogin = false;

            String emailString = MyApp.sUserObject.email;
            boolean emailValid = MyApp.sUserObject.isEmailValidation();
            if (!emailString.isEmpty() && !emailValid) {
                new AlertDialog.Builder(this)
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

            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage(R.string.alert_activity_email)
                    .setPositiveButton("确定", null)
                    .show();
        }
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mUpdatePushReceiver);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    // 信鸽文档推荐调用，防止在小米手机上收不到推送
    private void pushInXiaomi() {
        Context context = getApplicationContext();
        Intent service = new Intent(context, XGPushService.class);
        context.startService(service);
    }

    private void updateNotifyService() {
        boolean needPush = AccountInfo.getNeedPush(this);

        if (needPush) {
            String globalKey = MyApp.sUserObject.global_key;
            XGPushManager.registerPush(this, globalKey);
        } else {
            XGPushManager.registerPush(this, "*");
        }
    }

    @AfterViews
    final void initMainActivity() {
        Intent intent = new Intent(this, UpdateService.class);
        intent.putExtra(UpdateService.EXTRA_BACKGROUND, true);
        intent.putExtra(UpdateService.EXTRA_WIFI, true);
        intent.putExtra(UpdateService.EXTRA_DEL_OLD_APK, true);
        startService(intent);

        mSpinnerAdapter = new MaopaoTypeAdapter(getLayoutInflater(), maopao_action_types);
        setActionBarTitle("");

//        setListenerToRootView();

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            actionBarCompShadow.setVisibility(View.VISIBLE);
//        } else {
//            actionBarCompShadow.setVisibility(View.GONE);
//        }

//        toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
//        toolbarProjectTitle = (TextView) findViewById(R.id.toolbarProjectTitle);
//        toolbarProjectTitle.setOnClickListener(clickProjectTitle);
//        toolbarMaopaoTitle = (Spinner) findViewById(R.id.toolbarMaopaoTitle);

//        toolbarMaopaoTitle.setAdapter(mSpinnerAdapter);
//        toolbarMaopaoTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            String[] strings = getResources().getStringArray(R.array.maopao_action_types);
//
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Fragment fragment;
//                Bundle bundle = new Bundle();
//                mSpinnerAdapter.setCheckPos(position);
//
//                switch (position) {
//                    case 1:
//                        fragment = new MaopaoListFragment_();
//                        bundle.putSerializable("mType", MaopaoListFragment.Type.friends);
//                        break;
//
//                    case 2:
//                        fragment = new MaopaoListFragment_();
//                        bundle.putSerializable("mType", MaopaoListFragment.Type.hot);
//                        break;
//
//                    case 0:
//                    default:
//                        fragment = new MaopaoListFragment_();
//                        bundle.putSerializable("mType", MaopaoListFragment.Type.time);
//
//                        break;
//                }
//
//                fragment.setArguments(bundle);
//
//                FragmentManager fm = getSupportFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//                Log.d(TAG, ft == null ? "is null" : "is good");
//                ft.replace(R.id.container, fragment, strings[position]);
//                ft.commit();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });


        if (mFirstEnter) {
            // todo 打开第一个页面
//            onNavigationDrawerItemSelected(0);
        }

        bottomBar.setOnTabSelectListener(tabId -> switchTab(tabId));
    }

    protected void switchTab(int tabId) {
        taskOper(tabId);
        updateNotifyFromService();
        switch (tabId) {
            case R.id.tabProject:
                switchProject();
                break;

            case R.id.tabTask:
                switchFragment(MainTaskFragment_.FragmentBuilder_.class);
                break;

            case R.id.tabMaopao:// 进入冒泡页面，单独处理
                // todo
//                List<Fragment> fragments = getSupportFragmentManager().getFragments();
//                boolean containFragment = false;
//                for (Fragment item : fragments) {
//                    if (item instanceof MaopaoListFragment) {
//                        containFragment = true;
//                        break;
//                    }
//                }
//
//                if (!containFragment) {
//                    int pos = toolbarMaopaoTitle.getSelectedItemPosition();
//                    toolbarMaopaoTitle.getOnItemSelectedListener().onItemSelected(null, null, pos, pos);
//                }
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
        switchFragment(MainProjectFragment_.FragmentBuilder_.class);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pos", mSelectPos);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectPos = savedInstanceState.getInt("pos", 0);
    }

    //当项目设置里删除项目后，重新跳转到主界面，并刷新ProjectFragment
    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getStringExtra("action");
        if (!TextUtils.isEmpty(action) && action.equals(InitProUtils.FLAG_REFRESH)) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment item : fragments) {
                if (item instanceof ProjectFragment) {
                    if (item.isAdded()) {
                        ((ProjectFragment) item).onRefresh();
                    }
                    break;
                }
            }
        }
        super.onNewIntent(intent);
    }

    /**
     * 任务列表特殊处理
     * 1.drawerLayout 手势
     *
     * @param position
     */
    protected void taskOper(int position) {
        isOpenDrawerLayout(position == R.id.tabTask);
    }

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

    class MaopaoTypeAdapter extends BaseAdapter {

        final int spinnerIcons[] = new int[]{
                R.drawable.ic_spinner_maopao_time,
                R.drawable.ic_spinner_maopao_friend,
                R.drawable.ic_spinner_maopao_hot,
        };

        int checkPos = 0;
        private LayoutInflater inflater;
        private String[] project_activity_action_list;

        public MaopaoTypeAdapter(LayoutInflater inflater, String[] titles) {
            this.inflater = inflater;
            this.project_activity_action_list = titles;
        }

        public void setCheckPos(int pos) {
            checkPos = pos;
        }

        @Override
        public int getCount() {
            return spinnerIcons.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_layout_head, parent, false);
            }

            ((TextView) convertView).setText(project_activity_action_list[position]);

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.spinner_layout_item, parent, false);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(project_activity_action_list[position]);

            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageResource(spinnerIcons[position]);

            if (checkPos == position) {
                convertView.setBackgroundColor(getResources().getColor(R.color.divide));
            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
            return convertView;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBottomBarNotify(EventNotifyBottomBar notify) {
        updateNotify();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBottomBar(EventShowBottom showBottom) {
        if (showBottom.showBottom) {
            bottomBar.setVisibility(View.VISIBLE);
        } else {
            bottomBar.setVisibility(View.GONE);
        }
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
        Unread unread = MyApp.sUnread;
        bottomBar.getTabWithId(R.id.tabProject).setBadgeCount(unread.getProjectCount() > 0 ? 0 : -1);
        int notifyCount = unread.getNotifyCount();
        if (notifyCount <= 0) {
            notifyCount = -1;
        }
        bottomBar.getTabWithId(R.id.tabMessage).setBadgeCount(notifyCount);
    }
}
