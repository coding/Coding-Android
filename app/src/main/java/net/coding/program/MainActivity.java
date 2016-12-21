package net.coding.program;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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
import net.coding.program.common.ui.GlobalUnit;
import net.coding.program.event.EventFilter;
import net.coding.program.event.EventMessage;
import net.coding.program.event.EventNotifyBottomBar;
import net.coding.program.event.EventPosition;
import net.coding.program.event.EventShowBottom;
import net.coding.program.login.MarketingHelp;
import net.coding.program.login.ZhongQiuGuideActivity;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.maopao.MaopaoListFragment_;
import net.coding.program.message.UsersListFragment_;
import net.coding.program.model.AccountInfo;
import net.coding.program.project.ProjectFragment;
import net.coding.program.project.ProjectFragment_;
import net.coding.program.project.init.InitProUtils;
import net.coding.program.setting.MainSettingFragment_;
import net.coding.program.task.TaskFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import java.util.List;

import de.greenrobot.event.EventBus;
import network.coding.net.checknetwork.CheckNetworkIntentService;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";
    public static final String BroadcastPushStyle = "BroadcastPushStyle";

    String mTitle;

    @Extra
    String mPushUrl;
    @StringArrayRes
    String drawer_title[];
    @StringArrayRes
    String maopao_action_types[];
    @ViewById
    BottomBar bottomBar;
    @ViewById
    AppBarLayout appbar;
    @ViewById
    View actionBarCompShadow;

    TextView toolbarTitle;
    TextView toolbarProjectTitle;

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
    private Spinner toolbarMaopaoTitle;
    private long exitTime = 0;

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
            mSelectPos = savedInstanceState.getInt("pos", 0);
            mTitle = savedInstanceState.getString("mTitle");
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
        intent.putExtra(CheckNetworkIntentService.PARAM_APP, extra);

        intent.putExtra(CheckNetworkIntentService.PARAM_GK, MyApp.sUserObject.global_key);
        String sid = MyAsyncHttpClient.getCookie(this, Global.HOST);
        intent.putExtra(CheckNetworkIntentService.PARAM_COOKIE, sid);

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setActionBarTitle("");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            actionBarCompShadow.setVisibility(View.VISIBLE);
        } else {
            actionBarCompShadow.setVisibility(View.GONE);
        }

        toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
        toolbarProjectTitle = (TextView) findViewById(R.id.toolbarProjectTitle);
        toolbarProjectTitle.setOnClickListener(clickProjectTitle);
        toolbarMaopaoTitle = (Spinner) findViewById(R.id.toolbarMaopaoTitle);

        toolbarMaopaoTitle.setAdapter(mSpinnerAdapter);
        toolbarMaopaoTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String[] strings = getResources().getStringArray(R.array.maopao_action_types);

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Fragment fragment;
                Bundle bundle = new Bundle();
                mSpinnerAdapter.setCheckPos(position);

                switch (position) {
                    case 1:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", MaopaoListFragment.Type.friends);
                        break;

                    case 2:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", MaopaoListFragment.Type.hot);
                        break;

                    case 0:
                    default:
                        fragment = new MaopaoListFragment_();
                        bundle.putSerializable("mType", MaopaoListFragment.Type.time);

                        break;
                }

                fragment.setArguments(bundle);

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Log.d(TAG, ft == null ? "is null" : "is good");
                ft.replace(R.id.container, fragment, strings[position]);
                ft.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        mTitle = drawer_title[0];

        if (mFirstEnter) {
            onNavigationDrawerItemSelected(0);
        }

        bottomBar.setOnTabSelectListener(tabId -> {
            int[] tabs = new int[]{
                    R.id.tabProject,
                    R.id.tabTask,
                    R.id.tabMaopao,
                    R.id.tabMessage,
                    R.id.tabMy
            };

            for (int i = 0; i < tabs.length; ++i) {
                if (tabs[i] == tabId) {
                    onNavigationDrawerItemSelected(i);
                }
            }
        });
    }

    public void hideActionBarShadow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (actionBarCompShadow != null) {
                actionBarCompShadow.setVisibility(View.GONE);
            }
        } else {
            if (appbar != null) {
                ViewCompat.setElevation(appbar, 0);
            }
        }
    }

    // todo 切换保存状态
    public void onNavigationDrawerItemSelected(int position) {
        mSelectPos = position;
        Fragment fragment = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            actionBarCompShadow.setVisibility(View.VISIBLE);
        } else {
            ViewCompat.setElevation(appbar, GlobalUnit.ACTIONBAR_SHADOW);
        }

        updateNotifyFromService();

        switch (position) {
            case 0://防止重复加载数据
//                fragment = new ProjectFragment_();
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                boolean containFragment = false;
                if (fragments != null) {
                    for (Fragment item : fragments) {
                        if (item instanceof ProjectFragment_) {
                            containFragment = true;
                            break;
                        }
                    }
                }

                if (!containFragment) {
                    fragment = new ProjectFragment_();
                }
                toolbarProjectTitle.setText("全部项目");
                break;
            case 1:
//                bottomBar.getTabWithId(R.id.tabProject).setBadgeCount(20);
                fragment = new TaskFragment_();
                break;
            case 2:
                // 进入冒泡页面，单独处理
                break;

            case 3:
//                bottomBar.getTabWithId(R.id.tabProject).setBadgeCount(300);
                fragment = new UsersListFragment_();
                break;

            case 4:
//                bottomBar.getTabWithId(R.id.tabProject).setBadgeCount(0);
                fragment = new MainSettingFragment_();
                break;
        }

        if (position == 2) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            boolean containFragment = false;
            for (Fragment item : fragments) {
                if (item instanceof MaopaoListFragment) {
                    containFragment = true;
                    break;
                }
            }

            if (!containFragment) {
                int pos = toolbarMaopaoTitle.getSelectedItemPosition();
                toolbarMaopaoTitle.getOnItemSelectedListener().onItemSelected(null, null, pos, pos);
            }

            visibleTitle(toolbarMaopaoTitle);
        } else if (position == 0) {
            visibleTitle(toolbarProjectTitle);
        } else {
            toolbarTitle.setVisibility(View.VISIBLE);
            visibleTitle(toolbarTitle);
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }
    }

    void visibleTitle(View title) {
        View[] titles = {
                toolbarTitle,
                toolbarProjectTitle,
                toolbarMaopaoTitle,
        };

        for (View item : titles) {
            if (title == item) {
                item.setVisibility(View.VISIBLE);
            } else {
                item.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pos", mSelectPos);
//        outState.putSerializable("mPushOpened", mPushOpened);
        outState.putString("mTitle", mTitle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSelectPos = savedInstanceState.getInt("pos", 0);
        mTitle = savedInstanceState.getString("mTitle");
        restoreActionBar();
    }

    public void restoreActionBar() {
        mTitle = drawer_title[mSelectPos];
        if (mSelectPos == 0) {
            visibleTitle(toolbarProjectTitle);
        } else if (mSelectPos == 2) {
            visibleTitle(toolbarMaopaoTitle);
        } else {
            visibleTitle(toolbarTitle);
            toolbarTitle.setText(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        restoreActionBar();
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onBackPressed() {
        exitApp();
    }

    private void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            showButtomToast("再按一次退出Coding");
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    private View.OnClickListener clickProjectTitle = v -> {
        EventBus.getDefault().post(new EventFilter());
    };

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

    public void onEventMainThread(Object object) {
        if (object instanceof EventNotifyBottomBar) {
            updateNotify();
        } else if (object instanceof EventShowBottom) {
            EventShowBottom showBottom = (EventShowBottom) object;
            if (showBottom.showBottom) {
                bottomBar.setVisibility(View.VISIBLE);
            } else {
                bottomBar.setVisibility(View.GONE);
            }
        } else if (object instanceof EventPosition) {
            EventPosition eventPosition = (EventPosition) object;
            toolbarProjectTitle.setText(eventPosition.title);
        } else if (object instanceof EventMessage) {
            EventMessage eventMessage = (EventMessage) object;
            if (eventMessage.type == EventMessage.Type.loginOut) {
                finish();
            }
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
