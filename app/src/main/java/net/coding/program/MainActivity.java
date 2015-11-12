package net.coding.program;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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

import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;

import net.coding.program.common.LoginBackground;
import net.coding.program.common.htmltext.URLSpanNoUnderline;
import net.coding.program.common.ui.BaseActivity;
import net.coding.program.login.ZhongQiuGuideActivity;
import net.coding.program.maopao.MaopaoListFragment;
import net.coding.program.maopao.MaopaoListFragment_;
import net.coding.program.message.UsersListFragment_;
import net.coding.program.model.AccountInfo;
import net.coding.program.project.ProjectFragment;
import net.coding.program.project.ProjectFragment_;
import net.coding.program.project.init.InitProUtils;
import net.coding.program.setting.SettingFragment_;
import net.coding.program.task.TaskFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity
        implements NavigationDrawerFragment_.NavigationDrawerCallbacks {

    public static final String TAG = "MainActivity";
    public static final String BroadcastPushStyle = "BroadcastPushStyle";
    NavigationDrawerFragment_ mNavigationDrawerFragment;
    String mTitle;
    @Extra
    String mPushUrl;
    @StringArrayRes
    String drawer_title[];
    @StringArrayRes
    String maopao_action_types[];
    @ViewById
    ViewGroup drawer_layout;

    boolean mFirstEnter = true;
    BroadcastReceiver mUpdatePushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotifyService();
        }
    };
    int mSelectPos = 0;
    MySpinnerAdapter mSpinnerAdapter;
    private View actionbarCustom;
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
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mUpdatePushReceiver);
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
    void init() {
        Intent intent = new Intent(this, UpdateService.class);
        intent.putExtra(UpdateService.EXTRA_BACKGROUND, true);
        intent.putExtra(UpdateService.EXTRA_WIFI, true);
        intent.putExtra(UpdateService.EXTRA_DEL_OLD_APK, true);
        startService(intent);

        mSpinnerAdapter = new MySpinnerAdapter(getLayoutInflater(), maopao_action_types);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setCustomView(R.layout.actionbar_custom_spinner);
        actionbarCustom = supportActionBar.getCustomView();
        Spinner spinner = (Spinner) supportActionBar.getCustomView().findViewById(R.id.spinner);
        spinner.setAdapter(mSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                Log.d("", ft == null ? "is null" : "is good");
                ft.replace(R.id.container, fragment, strings[position]);
                ft.commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mNavigationDrawerFragment = (NavigationDrawerFragment_)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = drawer_title[0];

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if (mFirstEnter) {
            onNavigationDrawerItemSelected(0);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        mSelectPos = position;
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new ProjectFragment_();
                break;
            case 1:
                fragment = new TaskFragment_();
                break;
            case 2:
                // 进入冒泡页面，单独处理
                break;

            case 3:
                fragment = new UsersListFragment_();
                break;

            case 4:
                fragment = new SettingFragment_();
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }

        if (position == 2) {
            ActionBar actionBar = getSupportActionBar();
            Spinner spinner;
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionbarCustom);
            spinner = (Spinner) actionbarCustom.findViewById(R.id.spinner);
            List<Fragment> fragments = getSupportFragmentManager().getFragments();

            boolean containFragment = false;
            for (Fragment item : fragments) {
                if (item instanceof MaopaoListFragment) {
                    containFragment = true;
                    break;
                }
            }

            if (!containFragment) {
                int pos = spinner.getSelectedItemPosition();
                spinner.getOnItemSelectedListener().onItemSelected(null, null, pos, pos);
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
        ActionBar actionBar = getSupportActionBar();
        if (mSelectPos != 2) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
//            actionBar.setIcon(R.drawable.ic_lancher);
        } else {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionbarCustom);
            actionBar.setTitle("");
//             Spinner   spinner = (Spinner) actionbarCustom.findViewById(R.id.spinner);
//            spinner.setSelection(1);
//            spinner.setSelection(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {

            restoreActionBar();
            return true;
        }
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

    class MySpinnerAdapter extends BaseAdapter {

        final int spinnerIcons[] = new int[]{
                R.drawable.ic_spinner_maopao_time,
                R.drawable.ic_spinner_maopao_friend,
                R.drawable.ic_spinner_maopao_hot,
        };
        int checkPos = 0;
        private LayoutInflater inflater;
        private String[] project_activity_action_list;

        public MySpinnerAdapter(LayoutInflater inflater, String[] titles) {
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

}
